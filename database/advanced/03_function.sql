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
