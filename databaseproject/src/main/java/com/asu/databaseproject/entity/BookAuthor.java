package com.asu.databaseproject.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.util.Objects;

/** Bridge table book_authors between books and authors. */
@Entity
@Table(name = "book_authors")
public class BookAuthor {

    @EmbeddedId
    private BookAuthorId id = new BookAuthorId();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("bookId")
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("authorId")
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;

    public BookAuthor() {
    }

    public BookAuthor(Book book, Author author) {
        this.book = book;
        this.author = author;
        this.id = new BookAuthorId(book.getId(), author.getId());
    }

    public BookAuthorId getId() {
        return id;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BookAuthor other)) return false;
        return Objects.equals(book, other.book) && Objects.equals(author, other.author);
    }

    @Override
    public int hashCode() {
        return BookAuthor.class.hashCode();
    }
}
