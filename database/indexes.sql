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
CREATE INDEX idx_loans_type ON loans(type);
CREATE INDEX idx_loans_due_date ON loans(due_date);
CREATE INDEX idx_reservations_status ON reservations(status);