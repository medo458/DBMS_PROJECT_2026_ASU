package com.asu.databaseproject.service;

import com.asu.databaseproject.entity.CopyStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Bonus Task 2 demo: run this class's main() (after the main app has seeded the data)
 * to update and delete existing records through RecordManager, printing every
 * affected row before and after.
 */
public class UpdateDeleteDemo {

    private static final String PERSISTENCE_UNIT = "com.asu_databaseproject_jar_1.0-SNAPSHOTPU";

    public static void main(String[] args) {
        try (EntityManagerFactory emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT)) {
            run(emf);
        }
    }

    public static void run(EntityManagerFactory emf) {
        RecordManager manager = new RecordManager(emf);

        // Look up the seeded records by their natural keys
        int bookId = single(emf, "SELECT b.id FROM Book b WHERE b.isbn = '9780451524935'", Integer.class);
        UUID userId = single(emf, "SELECT u.id FROM User u WHERE u.lastName = 'Hassan'", UUID.class);
        Integer damagedCopyId = first(emf, "SELECT c.id FROM Copy c WHERE c.status = 'damaged'", Integer.class);
        UUID reservationId = first(emf, "SELECT r.id FROM Reservation r WHERE r.status = 'cancelled'", UUID.class);
        Integer categoryId = first(emf, "SELECT c.id FROM Category c WHERE c.name = 'Dystopian'", Integer.class);

        System.out.println("\n================ UPDATE 1: book title ================");
        printRows(emf, "SELECT b.id, b.title FROM Book b WHERE b.id = ?1", bookId);
        System.out.println("updateBookTitle -> " + manager.updateBookTitle(bookId, "Nineteen Eighty-Four"));
        printRows(emf, "SELECT b.id, b.title FROM Book b WHERE b.id = ?1", bookId);

        System.out.println("\n================ UPDATE 2: user email ================");
        printRows(emf, "SELECT u.firstName, u.email FROM User u WHERE u.id = ?1", userId);
        System.out.println("updateUserEmail -> " + manager.updateUserEmail(userId, "ahmed.hassan@library.eg"));
        printRows(emf, "SELECT u.firstName, u.email FROM User u WHERE u.id = ?1", userId);

        System.out.println("\n================ UPDATE 3: copy status (trigger updates book counters) ================");
        if (damagedCopyId == null) {
            System.out.println("No damaged copy left (demo already ran)");
        } else {
            printRows(emf, "SELECT c.id, c.status, c.book.availableCopies FROM Copy c WHERE c.id = ?1", damagedCopyId);
            System.out.println("updateCopyStatus -> " + manager.updateCopyStatus(damagedCopyId, CopyStatus.available));
            printRows(emf, "SELECT c.id, c.status, c.book.availableCopies FROM Copy c WHERE c.id = ?1", damagedCopyId);
        }

        System.out.println("\n================ UPDATE 4: bulk JPQL update of copy prices ================");
        printRows(emf, "SELECT c.id, c.rentalPrice FROM Copy c WHERE c.book.id = ?1 ORDER BY c.id", bookId);
        System.out.println("bulkUpdateCopyPrices -> " + manager.bulkUpdateCopyPrices(bookId, new BigDecimal("19.50")) + " row(s)");
        printRows(emf, "SELECT c.id, c.rentalPrice FROM Copy c WHERE c.book.id = ?1 ORDER BY c.id", bookId);

        System.out.println("\n================ DELETE 1: reservation ================");
        printRows(emf, "SELECT r.id, r.status FROM Reservation r ORDER BY r.reservedAt");
        System.out.println("deleteReservation -> "
                + (reservationId != null && manager.deleteReservation(reservationId)));
        printRows(emf, "SELECT r.id, r.status FROM Reservation r ORDER BY r.reservedAt");

        System.out.println("\n================ DELETE 2: category (its book links cascade) ================");
        printRows(emf, "SELECT c.name FROM Category c ORDER BY c.name");
        printRows(emf, "SELECT COUNT(bc) FROM BookCategory bc");
        System.out.println("deleteCategory -> " + (categoryId != null && manager.deleteCategory(categoryId)));
        printRows(emf, "SELECT c.name FROM Category c ORDER BY c.name");
        printRows(emf, "SELECT COUNT(bc) FROM BookCategory bc");
    }

    // ---------------- helpers ----------------

    private static <T> T single(EntityManagerFactory emf, String jpql, Class<T> type) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(jpql, type).getSingleResult();
        }
    }

    private static <T> T first(EntityManagerFactory emf, String jpql, Class<T> type) {
        try (EntityManager em = emf.createEntityManager()) {
            List<T> rows = em.createQuery(jpql, type).setMaxResults(1).getResultList();
            return rows.isEmpty() ? null : rows.get(0);
        }
    }

    /** Runs a JPQL query (optionally with one ?1 parameter) and prints each row. */
    private static void printRows(EntityManagerFactory emf, String jpql, Object... params) {
        try (EntityManager em = emf.createEntityManager()) {
            var query = em.createQuery(jpql);
            for (int i = 0; i < params.length; i++) {
                query.setParameter(i + 1, params[i]);
            }
            List<?> rows = query.getResultList();
            if (rows.isEmpty()) {
                System.out.println("   (no rows)");
            }
            for (Object row : rows) {
                System.out.println("   " + (row instanceof Object[] cols ? java.util.Arrays.toString(cols) : row));
            }
        }
    }
}
