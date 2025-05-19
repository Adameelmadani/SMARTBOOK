package com.smartbook.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"bookProgresses", "ratings"})
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(max = 100)
    private String title;
    
    @NotBlank
    @Size(max = 100)
    private String author;
    
    @Size(max = 50)
    private String genre;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @NotBlank
    private String filePath;
    
    private String coverImagePath;
    
    private Double averageRating = 0.0;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    private Set<BookProgress> bookProgresses = new HashSet<>();
    
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    private Set<Rating> ratings = new HashSet<>();
    
    @Transient
    public Double getAverageRating() {
        if (ratings.isEmpty()) {
            return 0.0;
        }
        double sum = 0.0;
        for (Rating rating : ratings) {
            sum += rating.getRating();
        }
        return sum / ratings.size();
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return Objects.equals(id, book.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}