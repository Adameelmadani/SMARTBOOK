package com.smartbook.service;

import com.smartbook.model.Book;
import com.smartbook.model.Rating;
import com.smartbook.model.User;
import com.smartbook.repository.BookRepository;
import com.smartbook.repository.RatingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
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
            if (user == null || user.getId() == null) {
                return getTopRatedBooks(limit);
            }
            
            // Get all ratings for books the user hasn't rated
            List<Rating> allRatings = ratingRepository.findAll();
            if (allRatings.isEmpty()) {
                return getTopRatedBooks(limit);
            }
            
            // Build user-item matrix
            Map<Long, Map<Long, Integer>> ratingsByUser = new HashMap<>();
            for (Rating rating : allRatings) {
                if (rating.getUser() == null || rating.getBook() == null || 
                    rating.getUser().getId() == null || rating.getBook().getId() == null) {
                    continue;
                }
                Long userId = rating.getUser().getId();
                ratingsByUser.computeIfAbsent(userId, key -> new HashMap<>())
                        .put(rating.getBook().getId(), rating.getRating());
            }
            
            Map<Long, Integer> currentUserRatings = ratingsByUser.get(user.getId());
            if (currentUserRatings == null || currentUserRatings.isEmpty()) {
                return getTopRatedBooks(limit);
            }

            List<Book> allBooks = bookRepository.findAll();
            Set<Long> readBookIds = currentUserRatings.keySet();
            
            List<Book> unreadBooks = allBooks.stream()
                    .filter(book -> !readBookIds.contains(book.getId()))
                    .collect(Collectors.toList());
            
            if (unreadBooks.isEmpty()) {
                return getTopRatedBooks(limit);
            }
            
            // Calculate similarity with other users
            Map<Long, Double> userSimilarities = new HashMap<>();
            for (Map.Entry<Long, Map<Long, Integer>> entry : ratingsByUser.entrySet()) {
                Long otherUserId = entry.getKey();
                if (!otherUserId.equals(user.getId())) {
                    double similarity = calculateSimilarity(currentUserRatings, entry.getValue());
                    if (similarity > 0.1) { // Only consider meaningful similarities
                        userSimilarities.put(otherUserId, similarity);
                    }
                }
            }

            // If no similar users, fallback to top rated
            if (userSimilarities.isEmpty()) {
                return getTopRatedBooks(limit);
            }

            // Score unread books based on similar users' ratings
            Map<Book, Double> bookScores = new HashMap<>();
            for (Book book : unreadBooks) {
                double score = 0.0;
                double totalSimilarity = 0.0;
                
                for (Map.Entry<Long, Double> entry : userSimilarities.entrySet()) {
                    Map<Long, Integer> otherRatings = ratingsByUser.get(entry.getKey());
                    Integer otherRating = otherRatings != null ? otherRatings.get(book.getId()) : null;

                    if (otherRating != null) {
                        double similarity = entry.getValue();
                        score += similarity * otherRating;
                        totalSimilarity += similarity;
                    }
                }

                if (totalSimilarity > 0) {
                    bookScores.put(book, score / totalSimilarity);
                }
            }

            if (bookScores.isEmpty()) {
                return getTopRatedBooks(limit);
            }

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
            return bookRepository.findAll().stream()
                    .filter(b -> b.getStoredAverageRating() != null && b.getStoredAverageRating() > 0)
                    .sorted(Comparator.comparingDouble(Book::getStoredAverageRating).reversed())
                    .limit(limit)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error getting top rated books: " + e.getMessage());
            List<Book> books = bookRepository.findAll();
            return books.stream().limit(limit).collect(Collectors.toList());
        }
    }

    private double calculateSimilarity(Map<Long, Integer> user1Ratings, Map<Long, Integer> user2Ratings) {
        Set<Long> commonBookIds = new HashSet<>(user1Ratings.keySet());
        commonBookIds.retainAll(user2Ratings.keySet());

        if (commonBookIds.size() < 2) {
            return 0.0; // Need at least 2 common ratings for meaningful similarity
        }

        double user1Avg = user1Ratings.values().stream().mapToInt(Integer::intValue).average().orElse(0.0);
        double user2Avg = user2Ratings.values().stream().mapToInt(Integer::intValue).average().orElse(0.0);

        double numerator = 0.0;
        double denominator1 = 0.0;
        double denominator2 = 0.0;

        for (Long bookId : commonBookIds) {
            double user1Dev = user1Ratings.get(bookId) - user1Avg;
            double user2Dev = user2Ratings.get(bookId) - user2Avg;
            numerator += user1Dev * user2Dev;
            denominator1 += Math.pow(user1Dev, 2);
            denominator2 += Math.pow(user2Dev, 2);
        }

        if (denominator1 <= 0 || denominator2 <= 0) {
            // Fallback to Cosine Similarity if variance is zero
            double dotProduct = 0.0;
            double norm1 = 0.0;
            double norm2 = 0.0;
            for (Long bookId : commonBookIds) {
                int r1 = user1Ratings.get(bookId);
                int r2 = user2Ratings.get(bookId);
                dotProduct += r1 * r2;
                norm1 += r1 * r1;
                norm2 += r2 * r2;
            }
            if (norm1 <= 0 || norm2 <= 0) {
                return 0.0;
            }
            return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
        }

        return numerator / (Math.sqrt(denominator1) * Math.sqrt(denominator2));
    }
}