package com.asu.databaseproject.service;

import com.asu.databaseproject.entity.Book;
import com.asu.databaseproject.entity.Category;
import com.asu.databaseproject.entity.Copy;
import com.asu.databaseproject.entity.CopyStatus;
import com.asu.databaseproject.entity.Reservation;
import com.asu.databaseproject.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.UUID;
import java.util.function.Function;

/**
 * Bonus Task 2: updates and deletes existing records with the JPA EntityManager.
 *
 * Every method runs in its own transaction and returns true when a row was
 * changed, or false when no record with that id exists.
 */
public class RecordManager {

    private final EntityManagerFactory emf;

    public RecordManager(EntityManagerFactory emf) {
        this.emf = emf;
    }

    // ------------------------------------------------------------------
    // UPDATE operations
    // ------------------------------------------------------------------

    /** find() loads a managed entity; changing a field is enough, Hibernate issues the UPDATE on commit. */
    public boolean updateBookTitle(int bookId, String newTitle) {
        return inTransaction(em -> {
            Book book = em.find(Book.class, bookId);
            if (book == null) {
                return false;
            }
            book.setTitle(newTitle);
            return true;
        });
    }

    public boolean updateUserEmail(UUID userId, String newEmail) {
        return inTransaction(em -> {
            User user = em.find(User.class, userId);
            if (user == null) {
                return false;
            }
            user.setEmail(newEmail);
            return true;
        });
    }

    /** The copies trigger recalculates books.total_copies / available_copies after this update. */
    public boolean updateCopyStatus(int copyId, CopyStatus newStatus) {
        return inTransaction(em -> {
            Copy copy = em.find(Copy.class, copyId);
            if (copy == null) {
                return false;
            }
            copy.setStatus(newStatus);
            return true;
        });
    }

    /** Bulk update with a JPQL statement instead of loading entities; returns the number of rows changed. */
    public int bulkUpdateCopyPrices(int bookId, java.math.BigDecimal newPrice) {
        return inTransaction(em -> em.createQuery(
                        "UPDATE Copy c SET c.rentalPrice = :price WHERE c.book.id = :bookId")
                .setParameter("price", newPrice)
                .setParameter("bookId", bookId)
                .executeUpdate());
    }

    // ------------------------------------------------------------------
    // DELETE operations
    // ------------------------------------------------------------------

    /** remove() deletes a managed entity; the DELETE is executed on commit. */
    public boolean deleteReservation(UUID reservationId) {
        return inTransaction(em -> {
            Reservation reservation = em.find(Reservation.class, reservationId);
            if (reservation == null) {
                return false;
            }
            em.remove(reservation);
            return true;
        });
    }

    /** Its book_categories rows are removed by ON DELETE CASCADE in the database. */
    public boolean deleteCategory(int categoryId) {
        return inTransaction(em -> {
            Category category = em.find(Category.class, categoryId);
            if (category == null) {
                return false;
            }
            em.remove(category);
            return true;
        });
    }

    // ------------------------------------------------------------------

    /** Opens an EntityManager, runs the work in a transaction, rolls back on any error. */
    private <T> T inTransaction(Function<EntityManager, T> work) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            T result = work.apply(em);
            em.getTransaction().commit();
            return result;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}
