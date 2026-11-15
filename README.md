# 📚 Library Management System


## ✨ Features

- **User management**: members, librarians, and admins with role-based access
- **Subscriptions**: track subscription start and end dates per user
- **Book catalog**: ISBN, publisher, description, page count, authors, and categories
- **Inventory tracking**: total and available copies tracked per book, kept in sync automatically by a trigger
- **Physical copies**: per-copy language, rental price, taxes, and status
- **Loans**: borrow/return tracking with due dates
- **Reservations**: reserve books that are currently unavailable
- **Data integrity**: foreign keys, unique and check constraints, business-rule indexes (no double loans or duplicate reservations), and indexes on key columns



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
        int total_copies
        int available_copies
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
    }

    PUBLISHER {
        int id PK
        varchar name
        varchar building_no
        varchar street_name
        varchar province
        varchar postal_code
        varchar country
    }
```

### Tables

| Table | Purpose |
|-------|---------|
| `users` | Members, librarians, and admins (`role`: member / librarian / admin) |
| `books` | Catalog entries (title, ISBN, publisher, pages, `total_copies`, `available_copies` maintained by trigger) |
| `copies` | Individual physical copies of a book (language, price, taxes, `status`: available / damaged / lost / retired / rented) |
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
- **COPIES to LOANS (1:N)**: A physical copy can be borrowed multiple times over time (never by two active loans at once). A book's loans are reached through its copies.
- **BOOKS to RESERVATIONS (1:N)**: A book can be placed on hold through multiple reservations.
- **BOOKS to AUTHORS (M:N via BOOK_AUTHORS)**: A book can be written by multiple authors, and an author can write multiple books.
- **BOOKS to CATEGORIES (M:N via BOOK_CATEGORIES)**: A book can belong to multiple categories/genres, and a category contains multiple books.

### Constraints

- **Primary keys**: users, loans and reservations use UUIDs; all other tables use auto-generated integer identity columns. Junction tables use composite keys.
- **Foreign keys**:
  - `RESTRICT` for `loans.user_id` and `loans.copy_id`, so loan history is never lost silently
  - `SET NULL` for `books.publisher_id` (nullable), so books remain in the catalog if a publisher is deleted
  - `CASCADE` for `copies`, `book_authors`, `book_categories`, and `reservations`
- **Unique**: `users.email`, `books.isbn`, `categories.name`, `publisher.name`
- **Partial unique indexes** (business rules):
  - `uq_loans_active_copy`: a copy can have only one unreturned loan at a time
  - `uq_reservations_active`: a user can have only one active reservation per book
- **Checks**:
  - `loans.due_date >= loans.borrow_date`
  - `loans.fine_amount >= 0 AND loans.rental_price >= 0 AND loans.subscription_price >= 0`
  - `users.subscription_end >= users.subscription_start`
  - `books.total_copies >= 0 AND books.available_copies >= 0 AND books.available_copies <= books.total_copies`
  - `books.pages > 0` and `books.publication_year BETWEEN 1000 AND 2100` (when not NULL)
  - `copies.rental_price >= 0 AND copies.taxes >= 0`
  - `loans.status = 'returned'` if and only if `loans.return_date` is not NULL

### Indexes

```sql
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX idx_books_title ON books(title);
CREATE INDEX idx_books_title_trgm ON books USING GIN (title gin_trgm_ops);
CREATE INDEX idx_authors_name ON authors(last_name, first_name);

CREATE INDEX idx_copies_book_status ON copies(book_id, status);
CREATE INDEX idx_book_authors_author_id ON book_authors(author_id);
CREATE INDEX idx_book_categories_category_id ON book_categories(category_id);
CREATE INDEX idx_loans_user_status ON loans(user_id, status);
CREATE INDEX idx_loans_copy_id ON loans(copy_id);
CREATE INDEX idx_reservations_user_id ON reservations(user_id);
CREATE INDEX idx_reservations_book_status ON reservations(book_id, status);

CREATE INDEX idx_loans_due_date_open ON loans(due_date) WHERE return_date IS NULL;

CREATE UNIQUE INDEX uq_loans_active_copy ON loans(copy_id) WHERE status <> 'returned';
CREATE UNIQUE INDEX uq_reservations_active ON reservations(user_id, book_id) WHERE status = 'active';
```



### Normalization

The schema follows **Third Normal Form (3NF)**: atomic attributes (1NF),
no partial dependencies (2NF),
no transitive dependencies (3NF),
and junction tables to resolve many-to-many relationships.

> **Note:** `books.total_copies` and `books.available_copies` are intentionally denormalized counters (derivable from `copies`) kept for fast availability lookups. They are maintained automatically by the trigger `trg_copies_sync_book_counts`, so application code should not update them directly.

### Triggers & Notes

- **`trg_copies_sync_book_counts`** runs after every insert, update (of `book_id` / `status`), or delete on `copies` and recalculates the counters of the affected book(s):
  - `total_copies` = copies that are not `lost` or `retired`
  - `available_copies` = copies with status `available`
- **Overdue loans**: `loan_status` includes `overdue`, but it is not set automatically. Either update it with a periodic job, or derive it in queries with `status = 'borrowed' AND due_date < CURRENT_DATE`.
- **Search**: `idx_books_title_trgm` (requires the `pg_trgm` extension) speeds up `LIKE '%term%'` / `ILIKE` searches on book titles.
