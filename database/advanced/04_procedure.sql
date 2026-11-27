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
