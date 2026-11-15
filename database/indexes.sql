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