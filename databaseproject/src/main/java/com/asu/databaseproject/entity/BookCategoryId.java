package com.asu.databaseproject.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/** Composite primary key of book_categories (book_id, category_id). */
@Embeddable
public class BookCategoryId implements Serializable {

    @Column(name = "book_id")
    private Integer bookId;

    @Column(name = "category_id")
    private Integer categoryId;

    public BookCategoryId() {
    }

    public BookCategoryId(Integer bookId, Integer categoryId) {
        this.bookId = bookId;
        this.categoryId = categoryId;
    }

    public Integer getBookId() {
        return bookId;
    }

    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BookCategoryId other)) return false;
        return Objects.equals(bookId, other.bookId) && Objects.equals(categoryId, other.categoryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookId, categoryId);
    }
}
