package com.asu.databaseproject.seed;

import com.asu.databaseproject.entity.Author;
import com.asu.databaseproject.entity.Book;
import com.asu.databaseproject.entity.Category;
import com.asu.databaseproject.entity.Copy;
import com.asu.databaseproject.entity.CopyStatus;
import com.asu.databaseproject.entity.Loan;
import com.asu.databaseproject.entity.LoanStatus;
import com.asu.databaseproject.entity.LoanType;
import com.asu.databaseproject.entity.Publisher;
import com.asu.databaseproject.entity.Reservation;
import com.asu.databaseproject.entity.ReservationStatus;
import com.asu.databaseproject.entity.User;
import com.asu.databaseproject.entity.UserRole;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Inserts sample rows (at least two per table) through Hibernate.
 * Runs only when the database is empty, so restarting the app is safe.
 */
public final class DataSeeder {

    private DataSeeder() {
    }

    public static void seed(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        try {
            long existingUsers = em.createQuery("SELECT COUNT(u) FROM User u", Long.class).getSingleResult();
            if (existingUsers > 0) {
                System.out.println("Seeder: data already present, skipping");
                return;
            }

            em.getTransaction().begin();

            // 1. Independent tables
            Publisher shorouk = publisher("Dar El Shorouk", "8", "Sebawayh El Masry St", "Cairo", "11371", "Egypt");
            Publisher penguin = publisher("Penguin Random House", "1745", "Broadway", "New York", "10019", "USA");
            em.persist(shorouk);
            em.persist(penguin);

            Author mahfouz = author("Naguib", "Mahfouz", "Egyptian novelist, Nobel Prize in Literature 1988.");
            Author orwell = author("George", "Orwell", "English novelist and essayist.");
            em.persist(mahfouz);
            em.persist(orwell);

            Category novel = category("Novel", "Long-form literary fiction");
            Category dystopian = category("Dystopian", "Fiction set in oppressive future societies");
            em.persist(novel);
            em.persist(dystopian);

            User ahmed = user("Ahmed", "Hassan", "ahmed.hassan@example.com", UserRole.member,
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), LocalDate.of(2003, 5, 14));
            User sara = user("Sara", "Ali", "sara.ali@example.com", UserRole.librarian,
                    null, null, LocalDate.of(1995, 9, 2));
            em.persist(ahmed);
            em.persist(sara);

            // 2. Books (need publishers)
            Book palaceWalk = book("9780385264662", "Palace Walk", "First part of the Cairo Trilogy.", shorouk, 1956, 498);
            Book nineteen84 = book("9780451524935", "1984", "A totalitarian future under Big Brother.", penguin, 1949, 328);
            em.persist(palaceWalk);
            em.persist(nineteen84);

            // 3. Junction tables (books and authors/categories must already have IDs)
            palaceWalk.addAuthor(mahfouz);
            nineteen84.addAuthor(orwell);
            palaceWalk.addCategory(novel);
            nineteen84.addCategory(novel);
            nineteen84.addCategory(dystopian);

            // 4. Copies (the trigger updates books.total_copies / available_copies)
            Copy pwArabic = copy(palaceWalk, "Arabic", "15.00", "2.10", CopyStatus.rented);
            Copy pwEnglish = copy(palaceWalk, "English", "20.00", "2.80", CopyStatus.available);
            Copy n84First = copy(nineteen84, "English", "18.00", "2.52", CopyStatus.available);
            Copy n84Second = copy(nineteen84, "English", "18.00", "2.52", CopyStatus.damaged);
            em.persist(pwArabic);
            em.persist(pwEnglish);
            em.persist(n84First);
            em.persist(n84Second);

            // 5. Loans: one still out (its copy is 'rented'), one already returned
            Loan open = loan(ahmed, pwArabic, LoanType.borrowing,
                    LocalDate.of(2026, 11, 20), LocalDate.of(2026, 12, 4), null, LoanStatus.borrowed, "15.00", "0.00");
            Loan closed = loan(ahmed, n84First, LoanType.subscription,
                    LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 15), LocalDate.of(2026, 10, 14),
                    LoanStatus.returned, "0.00", "50.00");
            em.persist(open);
            em.persist(closed);

            // 6. Reservations
            Reservation active = reservation(sara, palaceWalk, ReservationStatus.active);
            Reservation cancelled = reservation(ahmed, nineteen84, ReservationStatus.cancelled);
            em.persist(active);
            em.persist(cancelled);

            em.getTransaction().commit();
            System.out.println("Seeder: inserted sample data into all 10 tables");
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    private static Publisher publisher(String name, String buildingNo, String street,
                                       String province, String postalCode, String country) {
        Publisher p = new Publisher();
        p.setName(name);
        p.setBuildingNo(buildingNo);
        p.setStreetName(street);
        p.setProvince(province);
        p.setPostalCode(postalCode);
        p.setCountry(country);
        return p;
    }

    private static Author author(String first, String last, String bio) {
        Author a = new Author();
        a.setFirstName(first);
        a.setLastName(last);
        a.setBiography(bio);
        return a;
    }

    private static Category category(String name, String description) {
        Category c = new Category();
        c.setName(name);
        c.setDescription(description);
        return c;
    }

    private static User user(String first, String last, String email, UserRole role,
                             LocalDate subStart, LocalDate subEnd, LocalDate birth) {
        User u = new User();
        u.setFirstName(first);
        u.setLastName(last);
        u.setEmail(email);
        u.setPasswordHash("demo-hash-not-a-real-password");
        u.setRole(role);
        u.setSubscriptionStart(subStart);
        u.setSubscriptionEnd(subEnd);
        u.setDateOfBirth(birth);
        return u;
    }

    private static Book book(String isbn, String title, String description,
                             Publisher publisher, int year, int pages) {
        Book b = new Book();
        b.setIsbn(isbn);
        b.setTitle(title);
        b.setDescription(description);
        b.setPublisher(publisher);
        b.setPublicationYear(year);
        b.setPages(pages);
        return b;
    }

    private static Copy copy(Book book, String language, String price, String taxes, CopyStatus status) {
        Copy c = new Copy();
        c.setBook(book);
        c.setLanguage(language);
        c.setRentalPrice(new BigDecimal(price));
        c.setTaxes(new BigDecimal(taxes));
        c.setStatus(status);
        return c;
    }

    private static Loan loan(User user, Copy copy, LoanType type, LocalDate borrowed, LocalDate due,
                             LocalDate returned, LoanStatus status, String rentalPrice, String subscriptionPrice) {
        Loan l = new Loan();
        l.setUser(user);
        l.setCopy(copy);
        l.setType(type);
        l.setBorrowDate(borrowed);
        l.setDueDate(due);
        l.setReturnDate(returned);
        l.setStatus(status);
        l.setRentalPrice(new BigDecimal(rentalPrice));
        l.setSubscriptionPrice(new BigDecimal(subscriptionPrice));
        return l;
    }

    private static Reservation reservation(User user, Book book, ReservationStatus status) {
        Reservation r = new Reservation();
        r.setUser(user);
        r.setBook(book);
        r.setStatus(status);
        return r;
    }
}
