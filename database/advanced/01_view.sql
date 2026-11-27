-- ---------------------------------------------------------------------
-- 1. VIEW: one readable row per book.
--    Hides the joins across books, publisher, authors and categories
--    (including both many-to-many junction tables).
-- ---------------------------------------------------------------------
CREATE VIEW v_book_details AS
SELECT b.id                                                         AS book_id,
       b.isbn,
       b.title,
       b.publication_year,
       p.name                                                       AS publisher,
       STRING_AGG(DISTINCT a.first_name || ' ' || a.last_name, ', ') AS authors,
       STRING_AGG(DISTINCT c.name, ', ')                            AS categories,
       b.total_copies,
       b.available_copies
FROM books b
LEFT JOIN publisher       p  ON p.id = b.publisher_id
LEFT JOIN book_authors    ba ON ba.book_id = b.id
LEFT JOIN authors         a  ON a.id = ba.author_id
LEFT JOIN book_categories bc ON bc.book_id = b.id
LEFT JOIN categories      c  ON c.id = bc.category_id
GROUP BY b.id, p.name;
