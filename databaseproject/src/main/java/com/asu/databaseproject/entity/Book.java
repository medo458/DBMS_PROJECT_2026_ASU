package com.asu.databaseproject.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "isbn", nullable = false, unique = true, length = 20)
    private String isbn;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** publisher_id is nullable; ON DELETE SET NULL in the database. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id")
    private Publisher publisher;

    @Column(name = "publication_year")
    private Integer publicationYear;

    @Column(name = "pages")
    private Integer pages;

    /**
     * Maintained by the trg_copies_sync_book_counts trigger, so it is read-only
     * from the application's side (the DB default of 0 applies on insert).
     */
    @Column(name = "total_copies", nullable = false, insertable = false, updatable = false)
    private int totalCopies;

    /** Maintained by the trg_copies_sync_book_counts trigger; read-only. */
    @Column(name = "available_copies", nullable = false, insertable = false, updatable = false)
    private int availableCopies;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    /** Rows of the book_authors bridge table for this book. */
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BookAuthor> bookAuthors = new HashSet<>();

    /** Rows of the book_categories bridge table for this book. */
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BookCategory> bookCategories = new HashSet<>();

    /** Inverse side of copies.book_id. */
    @OneToMany(mappedBy = "book")
    private List<Copy> copies = new ArrayList<>();

    public Book() {
    }

    public Integer getId() {
        return id;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    public Integer getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(Integer publicationYear) {
        this.publicationYear = publicationYear;
    }

    public Integer getPages() {
        return pages;
    }

    public void setPages(Integer pages) {
        this.pages = pages;
    }

    public int getTotalCopies() {
        return totalCopies;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public Set<BookAuthor> getBookAuthors() {
        return bookAuthors;
    }

    public void addAuthor(Author author) {
        BookAuthor link = new BookAuthor(this, author);
        bookAuthors.add(link);
        author.getBookAuthors().add(link);
    }

    public void removeAuthor(Author author) {
        bookAuthors.removeIf(link -> link.getAuthor().equals(author));
        author.getBookAuthors().removeIf(link -> link.getBook().equals(this));
    }

    public Set<BookCategory> getBookCategories() {
        return bookCategories;
    }

    public void addCategory(Category category) {
        BookCategory link = new BookCategory(this, category);
        bookCategories.add(link);
        category.getBookCategories().add(link);
    }

    public void removeCategory(Category category) {
        bookCategories.removeIf(link -> link.getCategory().equals(category));
        category.getBookCategories().removeIf(link -> link.getBook().equals(this));
    }

    public List<Copy> getCopies() {
        return copies;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book other)) return false;
        return id != null && Objects.equals(id, other.getId());
    }

    @Override
    public int hashCode() {
        return Book.class.hashCode();
    }
}
