# 📚 Library Management System


## ✨ Features

- **User management**: members, librarians, and admins with role-based access
- **Subscriptions**: track subscription start and end dates per user
- **Book catalog**: ISBN, publisher, description, page count, authors, and categories
- **Physical copies**: per-copy language, rental price, taxes, and status
- **Loans**: borrow/return tracking with due dates
- **Reservations**: reserve books that are currently unavailable
- **Data integrity**: foreign keys, unique and check constraints, and indexes on key columns



## 🗄️ Database Design

### Entity Relationship Diagram

```mermaid
erDiagram
    USERS ||--o{ LOANS : "borrows"
    USERS ||--o{ RESERVATIONS : "reserves"
    PUBLISHER ||--o{ BOOKS : "publishes"
    COPIES ||--o{ LOANS : "is_borrowed"
    BOOKS ||--o{ RESERVATIONS : "is_reserved"
    BOOKS ||--o{ COPIES : "has"
    BOOKS ||--o{ BOOK_AUTHORS : "written_by"
    AUTHORS ||--o{ BOOK_AUTHORS : "writes"
    BOOKS ||--o{ BOOK_CATEGORIES : "belongs_to"
    CATEGORIES ||--o{ BOOK_CATEGORIES : "contains"

    USERS {
        UUID id PK
        varchar first_name
        varchar last_name
        date subscription_start
        date subscription_end
        date date_of_birth
        varchar email UK
        varchar password_hash
        enum role
        timestamptz created_at
    }

    BOOKS {
        int id PK
        varchar isbn UK
        varchar title
        text description
        int publisher_id FK
        int publication_year
        int pages
        timestamptz created_at
    }

    COPIES {
        int id PK
        int book_id FK
        varchar language
        numeric(8,2) rental_price
        numeric(8,2) taxes
        enum status
    }

    AUTHORS {
        int id PK
        varchar first_name
        varchar last_name
        text biography
    }

    CATEGORIES {
        int id PK
        varchar name UK
        text description
    }

    BOOK_AUTHORS {
        int book_id PK,FK
        int author_id PK,FK
    }

    BOOK_CATEGORIES {
        int book_id PK,FK
        int category_id PK,FK
    }

    LOANS {
        UUID id PK
        UUID user_id FK
        enum type
        int copy_id FK
        date borrow_date
        date due_date
        date return_date
        numeric(8,2) fine_amount
        numeric(8,2) rental_price
        numeric(8,2) subscription_price
        enum status
        timestamptz created_at
    }

    RESERVATIONS {
        UUID id PK
        UUID user_id FK
        int book_id FK
        timestamptz reserved_at
        enum status
        timestamptz created_at
    }

    PUBLISHER {
        int id PK
        varchar name
        int building_no
        varchar street_name
        varchar province
        int postal_code
        varchar country
    }
```

### Tables

| Table | Purpose |
|-------|---------|
| `users` | Members, librarians, and admins (`role`: member / librarian / admin) |
| `books` | Catalog entries (title, ISBN, publisher, pages, `status`: active / damaged / lost / retired) |
| `copies` | Individual physical copies of a book (language, price, taxes, status) |
| `authors` | Author details and biography |
| `categories` | Genres / categories |
| `book_authors` | Junction table: books ↔ authors (M:N) |
| `book_categories` | Junction table: books ↔ categories (M:N) |
| `loans` | Borrowing transactions (`status`: borrowed / returned / overdue)|
| `reservations` | Book reservations (`status`: active / cancelled / fulfilled) |
| `publisher` | Publisher details and address |

### Relationships

- **USERS to LOANS (1:N)**: A user can borrow multiple books over time, but each loan record belongs to exactly one user.
- **USERS to RESERVATIONS (1:N)**: A user can make multiple reservations, but each reservation belongs to one user.
- **PUBLISHER to BOOKS (1:N)**: A publisher can publish multiple books, while each book is associated with one publisher.
- **BOOKS to COPIES (1:N)**: A book title in the catalog can have multiple physical inventory copies.
- **BOOKS to LOANS (1:N)**: A book can be borrowed multiple times across different loan transactions.
- **BOOKS to RESERVATIONS (1:N)**: A book can be placed on hold through multiple reservations.
- **BOOKS to AUTHORS (M:N via BOOK_AUTHORS)**: A book can be written by multiple authors, and an author can write multiple books.
- **BOOKS to CATEGORIES (M:N via BOOK_CATEGORIES)**: A book can belong to multiple categories/genres, and a category contains multiple books.

### Constraints

- **Primary keys**: users, loans and reservations use UUIDs; all other tables use auto-generated integer identity columns. Junction tables use composite keys.
- **Foreign keys**: `ON DELETE CASCADE` or `RESTRICT` depending on business rules
- **Unique**: `users.email`, `books.isbn`, `categories.name`
- **Checks**:
  - `loans.due_date >= loans.borrow_date`
  - `loans.fine_amount >= 0 AND loans.rental_price >= 0 AND loans.subscription_price >= 0`
  - `users.subscription_end >= users.subscription_start`

### Indexes

```sql
-- Search
CREATE INDEX idx_books_title ON books(title);
CREATE INDEX idx_authors_name ON authors(last_name, first_name);
CREATE INDEX idx_publisher_name ON publisher(name);

-- Foreign keys & Junction tables
CREATE INDEX idx_copies_book_id ON copies(book_id);
CREATE INDEX idx_book_authors_author_id ON book_authors(author_id);
CREATE INDEX idx_book_categories_category_id ON book_categories(category_id);
CREATE INDEX idx_loans_user_id ON loans(user_id);
CREATE INDEX idx_loans_copy_id ON loans(copy_id);
CREATE INDEX idx_reservations_user_id ON reservations(user_id);
CREATE INDEX idx_reservations_book_id ON reservations(book_id);

-- Status & Date filtering
CREATE INDEX idx_copies_status ON copies(status);
CREATE INDEX idx_loans_status ON loans(status);
CREATE INDEX idx_loans_due_date ON loans(due_date);
CREATE INDEX idx_reservations_status ON reservations(status);
```



### Normalization

The schema follows **Third Normal Form (3NF)**: atomic attributes (1NF),
no partial dependencies (2NF),
no transitive dependencies (3NF),
and junction tables to resolve many-to-many relationships.

