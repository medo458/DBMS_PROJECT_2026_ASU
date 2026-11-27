-- ---------------------------------------------------------------------
-- 2. MATERIALIZED VIEW: loan statistics per book (reporting).
--    The result is stored on disk; refresh it with:
--      REFRESH MATERIALIZED VIEW CONCURRENTLY mv_book_loan_stats;
-- ---------------------------------------------------------------------
CREATE MATERIALIZED VIEW mv_book_loan_stats AS
SELECT b.id                                              AS book_id,
       b.title,
       COUNT(l.id)                                       AS total_loans,
       COUNT(l.id) FILTER (WHERE l.status <> 'returned') AS open_loans,
       COALESCE(SUM(l.rental_price + l.subscription_price + l.fine_amount), 0)::NUMERIC(10,2)
                                                         AS total_revenue
FROM books b
LEFT JOIN copies c ON c.book_id = b.id
LEFT JOIN loans  l ON l.copy_id = c.id
GROUP BY b.id, b.title;

-- A unique index is required for REFRESH ... CONCURRENTLY (no read lock while refreshing)
CREATE UNIQUE INDEX uq_mv_book_loan_stats_book ON mv_book_loan_stats(book_id);
