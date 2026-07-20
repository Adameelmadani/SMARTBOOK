
package com.smartbook.repository;

import com.smartbook.model.Rating;
import com.smartbook.model.Book;
import com.smartbook.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    @Query("SELECT DISTINCT r.user FROM Rating r")
    List<User> findDistinctUsers();

    Optional<Rating> findByUserAndBook(User user, Book book);
    List<Rating> findByBook(Book book);
}