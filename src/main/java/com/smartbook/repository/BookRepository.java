package com.smartbook.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.smartbook.model.Book;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByTitleContainingIgnoreCase(String title);
    List<Book> findByAuthorContainingIgnoreCase(String author);
    List<Book> findByGenreContainingIgnoreCase(String genre);
    // Find top 3 books by average rating
    List<Book> findTop3ByOrderByAverageRatingDesc();
}