package com.asu.databaseproject.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.util.Objects;

/** Bridge table book_categories between books and categories. */
@Entity
@Table(name = "book_categories")
public class BookCategory {

    @EmbeddedId
    private BookCategoryId id = new BookCategoryId();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("bookId")
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("categoryId")
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    public BookCategory() {
    }

    public BookCategory(Book book, Category category) {
        this.book = book;
        this.category = category;
        this.id = new BookCategoryId(book.getId(), category.getId());
    }

    public BookCategoryId getId() {
        return id;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BookCategory other)) return false;
        return Objects.equals(book, other.book) && Objects.equals(category, other.category);
    }

    @Override
    public int hashCode() {
        return BookCategory.class.hashCode();
    }
}
