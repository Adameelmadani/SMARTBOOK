package com.smartbook.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
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
    
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    private Set<BookProgress> bookProgresses = new HashSet<>();
    
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    private Set<Rating> ratings = new HashSet<>();
    
    // Calculate average rating
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
}