CREATE TYPE user_role AS ENUM ('member', 'librarian', 'admin');
CREATE TYPE copy_status AS ENUM ('available', 'damaged', 'lost', 'retired', 'rented');
CREATE TYPE loan_status AS ENUM ('borrowed', 'returned', 'overdue');
CREATE TYPE reservation_status AS ENUM ('active', 'cancelled', 'fulfilled');
CREATE TYPE loan_type AS ENUM ('subscription', 'borrowing');

CREATE TABLE publisher (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    building_no INT,
    street_name VARCHAR(255),
    province VARCHAR(50),
    postal_code INT,
    country VARCHAR(50)
);

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    subscription_start DATE,
    subscription_end DATE,
    date_of_birth DATE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(70) NOT NULL,
    role user_role NOT NULL DEFAULT 'member',
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_users_subscription CHECK (
        (subscription_start IS NULL AND subscription_end IS NULL) OR
        (subscription_start IS NOT NULL AND subscription_end IS NOT NULL AND subscription_end >= subscription_start)
    )
);

CREATE TABLE authors (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    biography TEXT
);

CREATE TABLE categories (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE books (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    isbn VARCHAR(20) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    publisher_id INT NOT NULL,
    publication_year INT,
    pages INT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_books_publisher FOREIGN KEY (publisher_id) REFERENCES publisher(id) ON DELETE RESTRICT
);

CREATE TABLE copies (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    book_id INT NOT NULL,
    language VARCHAR(50) NOT NULL,
    rental_price NUMERIC(8,2) NOT NULL DEFAULT 0,
    taxes NUMERIC(8,2) NOT NULL DEFAULT 0,
    status copy_status NOT NULL DEFAULT 'available',
    CONSTRAINT fk_copies_book FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE SET NULL
);

CREATE TABLE book_authors (
    book_id INT NOT NULL,
    author_id INT NOT NULL,
    PRIMARY KEY (book_id, author_id),
    CONSTRAINT fk_ba_book FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE SET NULL,
    CONSTRAINT fk_ba_author FOREIGN KEY (author_id) REFERENCES authors(id) ON DELETE SET NULL
);

CREATE TABLE book_categories (
    book_id INT NOT NULL,
    category_id INT NOT NULL,
    PRIMARY KEY (book_id, category_id),
    CONSTRAINT fk_bc_book FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE SET NULL,
    CONSTRAINT fk_bc_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
);

CREATE TABLE loans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    type loan_type NOT NULL,
    copy_id INT NOT NULL,
    borrow_date DATE NOT NULL DEFAULT CURRENT_DATE,
    due_date DATE NOT NULL,
    return_date DATE,
    fine_amount NUMERIC(8,2) NOT NULL DEFAULT 0.00,
    rental_price NUMERIC(8,2) NOT NULL DEFAULT 0.00,
    subscription_price NUMERIC(8,2) NOT NULL DEFAULT 0.00,
    status loan_status NOT NULL DEFAULT 'borrowed',
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_loans_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_loans_copy FOREIGN KEY (copy_id) REFERENCES copies(id) ON DELETE RESTRICT,
    CONSTRAINT chk_loans_dates CHECK (due_date >= borrow_date),
    CONSTRAINT chk_loans_return_date CHECK (return_date IS NULL OR return_date >= borrow_date),
    CONSTRAINT chk_loans_prices CHECK (fine_amount >= 0 AND rental_price >= 0 AND subscription_price >= 0)
);

CREATE TABLE reservations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    book_id INT NOT NULL,
    reserved_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status reservation_status NOT NULL DEFAULT 'active',
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reservations_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_reservations_book FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE SET NULL
);
