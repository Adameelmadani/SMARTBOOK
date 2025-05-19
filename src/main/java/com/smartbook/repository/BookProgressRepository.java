
package com.smartbook.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.smartbook.model.BookProgress;

@Repository
public interface BookProgressRepository extends JpaRepository<BookProgress, Long> {
    // You can add custom query methods here if needed
}