package com.smartbook.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "book_progress",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"user_id", "book_id"})
       })
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"user", "book"})
public class BookProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
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
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookProgress that = (BookProgress) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}