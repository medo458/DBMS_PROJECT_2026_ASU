-- =====================================================================
-- V3: Advanced database objects
--   1. View               v_book_details
--   2. Materialized view  mv_book_loan_stats
--   3. Function           fn_user_active_loans(user_id) -> INT
--   4. Stored procedure   sp_return_loan(loan_id, fine_per_day)
-- (Indexes were added in V2__add_indexes.sql)
-- =====================================================================


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


-- ---------------------------------------------------------------------
-- 3. FUNCTION (returns a value): number of loans a user has not returned yet.
--    Usage: SELECT fn_user_active_loans('<user uuid>');
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_user_active_loans(p_user_id UUID)
RETURNS INT
LANGUAGE plpgsql
STABLE
AS $$
DECLARE
    v_count INT;
BEGIN
    SELECT COUNT(*) INTO v_count
    FROM loans
    WHERE user_id = p_user_id
      AND status <> 'returned';

    RETURN v_count;
END;
$$;


-- ---------------------------------------------------------------------
-- 4. STORED PROCEDURE: return a borrowed copy.
--    - marks the loan as returned today
--    - charges p_fine_per_day for every day past the due date
--    - puts the copy back to 'available' (the copies trigger then
--      updates books.available_copies automatically)
--    Usage: CALL sp_return_loan('<loan uuid>');        -- default fine 5.00/day
--           CALL sp_return_loan('<loan uuid>', 10.00);
-- ---------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE sp_return_loan(p_loan_id UUID, p_fine_per_day NUMERIC DEFAULT 5.00)
LANGUAGE plpgsql
AS $$
DECLARE
    v_loan      loans%ROWTYPE;
    v_late_days INT;
BEGIN
    SELECT * INTO v_loan FROM loans WHERE id = p_loan_id FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Loan % does not exist', p_loan_id;
    END IF;

    IF v_loan.status = 'returned' THEN
        RAISE EXCEPTION 'Loan % is already returned', p_loan_id;
    END IF;

    v_late_days := GREATEST(CURRENT_DATE - v_loan.due_date, 0);

    UPDATE loans
    SET return_date = CURRENT_DATE,
        status      = 'returned',
        fine_amount = v_late_days * p_fine_per_day
    WHERE id = p_loan_id;

    UPDATE copies
    SET status = 'available'
    WHERE id = v_loan.copy_id;

    RAISE NOTICE 'Loan % returned, % day(s) late, fine = %',
        p_loan_id, v_late_days, v_late_days * p_fine_per_day;
END;
$$;
