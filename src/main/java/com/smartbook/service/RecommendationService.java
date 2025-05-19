package com.smartbook.service;

import com.smartbook.model.Book;
import com.smartbook.model.Rating;
import com.smartbook.model.User;
import com.smartbook.repository.BookRepository;
import com.smartbook.repository.RatingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {
    
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private RatingRepository ratingRepository;
    
    /**
     * Get book recommendations for a user using collaborative filtering
     * @param user The user to get recommendations for
     * @param limit Maximum number of recommendations to return
     * @return List of recommended books
     */
    public List<Book> getRecommendationsForUser(User user, int limit) {
        try {
            // If user is null or user has no ratings, return top rated books
            if (user == null || user.getRatings() == null || user.getRatings().isEmpty()) {
                return getTopRatedBooks(limit);
            }
            
            // Get all users who have rated books
            List<User> allUsers = ratingRepository.findDistinctUsers();
            
            // If not enough users for meaningful recommendations, return top rated books
            if (allUsers.size() < 2) {
                return getTopRatedBooks(limit);
            }
            
            // Find similar users (users with similar tastes)
            Map<User, Double> userSimilarities = new HashMap<>();
            for (User otherUser : allUsers) {
                if (otherUser != null && otherUser.getId() != null && !otherUser.getId().equals(user.getId())) {
                    double similarity = calculateSimilarity(user, otherUser);
                    userSimilarities.put(otherUser, similarity);
                }
            }
            
            // Get books the user hasn't read yet
            List<Book> allBooks = bookRepository.findAll();
            Set<Long> readBookIds = user.getRatings().stream()
                    .filter(r -> r.getBook() != null)
                    .map(rating -> rating.getBook().getId())
                    .collect(Collectors.toSet());
            
            List<Book> unreadBooks = allBooks.stream()
                    .filter(book -> !readBookIds.contains(book.getId()))
                    .collect(Collectors.toList());
            
            // If user has read all books, return top rated books
            if (unreadBooks.isEmpty()) {
                return getTopRatedBooks(limit);
            }
            
            // Calculate recommendation scores for unread books
            Map<Book, Double> bookScores = new HashMap<>();
            for (Book book : unreadBooks) {
                double score = 0.0;
                double totalSimilarity = 0.0;
                
                for (Map.Entry<User, Double> entry : userSimilarities.entrySet()) {
                    User otherUser = entry.getKey();
                    Double similarity = entry.getValue();
                    
                    if (otherUser.getRatings() != null) {
                        // Find if the other user has rated this book
                        Optional<Rating> otherRating = otherUser.getRatings().stream()
                                .filter(r -> r.getBook() != null && r.getBook().getId().equals(book.getId()))
                                .findFirst();
                        
                        if (otherRating.isPresent()) {
                            score += similarity * otherRating.get().getRating();
                            totalSimilarity += similarity;
                        }
                    }
                }
                
                // Normalize score by total similarity if we have data
                if (totalSimilarity > 0) {
                    bookScores.put(book, score / totalSimilarity);
                }
            }
            
            // If no scores could be calculated, return top rated books
            if (bookScores.isEmpty()) {
                return getTopRatedBooks(limit);
            }
            
            // Sort books by score and return top recommendations
            return bookScores.entrySet().stream()
                    .sorted(Map.Entry.<Book, Double>comparingByValue().reversed())
                    .limit(limit)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // Log the exception
            System.err.println("Error generating recommendations: " + e.getMessage());
            e.printStackTrace();
            // Fallback to top rated books
            return getTopRatedBooks(limit);
        }
    }
    
    /**
     * Returns top rated books as a fallback recommendation
     */
    private List<Book> getTopRatedBooks(int limit) {
        try {
            return bookRepository.findTop3ByOrderByAverageRatingDesc();
        } catch (Exception e) {
            // If that fails, just return the first few books
            System.err.println("Error getting top rated books: " + e.getMessage());
            List<Book> books = bookRepository.findAll();
            return books.stream().limit(limit).collect(Collectors.toList());
        }
    }
    
    /**
     * Calculate similarity between two users based on their ratings
     * This uses Pearson correlation coefficient
     */
    private double calculateSimilarity(User user1, User user2) {
        try {
            // Ensure ratings collections are available
            if (user1.getRatings() == null || user2.getRatings() == null) {
                return 0.0;
            }
            
            // Find books both users have rated
            Set<Long> user1BookIds = user1.getRatings().stream()
                    .filter(r -> r.getBook() != null)
                    .map(rating -> rating.getBook().getId())
                    .collect(Collectors.toSet());
            
            if (user1BookIds.isEmpty()) {
                return 0.0;
            }
            
            List<Rating> commonRatings = user2.getRatings().stream()
                    .filter(r -> r.getBook() != null && user1BookIds.contains(r.getBook().getId()))
                    .collect(Collectors.toList());
            
            // If no common ratings, return 0 similarity
            if (commonRatings.isEmpty()) {
                return 0.0;
            }
            
            // Calculate averages
            double user1Avg = user1.getRatings().stream()
                    .mapToInt(Rating::getRating)
                    .average()
                    .orElse(0.0);
            
            double user2Avg = user2.getRatings().stream()
                    .mapToInt(Rating::getRating)
                    .average()
                    .orElse(0.0);
            
            double numerator = 0.0;
            double denominator1 = 0.0;
            double denominator2 = 0.0;
            
            // Calculate Pearson correlation
            for (Rating user2Rating : commonRatings) {
                Optional<Rating> user1Rating = user1.getRatings().stream()
                        .filter(r -> r.getBook() != null && r.getBook().getId().equals(user2Rating.getBook().getId()))
                        .findFirst();
                
                if (user1Rating.isPresent()) {
                    double user1Dev = user1Rating.get().getRating() - user1Avg;
                    double user2Dev = user2Rating.getRating() - user2Avg;
                    
                    numerator += user1Dev * user2Dev;
                    denominator1 += Math.pow(user1Dev, 2);
                    denominator2 += Math.pow(user2Dev, 2);
                }
            }
            
            // Avoid division by zero
            if (denominator1 <= 0 || denominator2 <= 0) {
                return 0.0;
            }
            
            return numerator / (Math.sqrt(denominator1) * Math.sqrt(denominator2));
        } catch (Exception e) {
            // Log the exception
            System.err.println("Error calculating similarity: " + e.getMessage());
            return 0.0;
        }
    }
}