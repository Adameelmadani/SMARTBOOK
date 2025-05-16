package com.smartbook.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "book_progress",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"user_id", "book_id"})
       })
@Data
@NoArgsConstructor
public class BookProgress {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;
    
    private Integer currentPage = 0;
    
    private Integer totalPages = 0;
    
    private LocalDateTime lastReadAt = LocalDateTime.now();
    
    @Transient
    public double getProgressPercentage() {
        if (totalPages == 0) return 0;
        return (double) currentPage / totalPages * 100;
    }
}