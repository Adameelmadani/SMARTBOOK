package com.smartbook.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.smartbook.repository.BookRepository;
import com.smartbook.repository.RatingRepository;
import com.smartbook.repository.UserRepository;
import com.smartbook.model.Book;
import com.smartbook.model.Rating;
import com.smartbook.model.User;
import com.smartbook.service.RecommendationService;

@Controller
public class BookController {
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RatingRepository ratingRepository;
    
    @Autowired
    private RecommendationService recommendationService;
    
    private User getCurrentUser(Principal principal) {
        if (principal == null) {
            return null;
        }
        return userRepository.findByUsername(principal.getName()).orElse(null);
    }
    
    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        try {
            User user = getCurrentUser(principal);
            model.addAttribute("user", user);
            
            // Get recommended books for the user
            List<Book> recommendedBooks = recommendationService.getRecommendationsForUser(user, 3);
            model.addAttribute("recommendedBooks", recommendedBooks);
            
            // Get all books for the "All Books" section
            List<Book> books = bookRepository.findAll();
            model.addAttribute("books", books);
            
            return "dashboard";
        } catch (Exception e) {
            // Log the error
            System.err.println("Error loading dashboard: " + e.getMessage());
            e.printStackTrace();
            
            // Add an error message to display
            model.addAttribute("errorMessage", "Something went wrong. Please try again later.");
            
            // Still get basic books to display
            try {
                List<Book> books = bookRepository.findAll();
                model.addAttribute("books", books);
                model.addAttribute("recommendedBooks", Collections.emptyList());
            } catch (Exception ex) {
                model.addAttribute("books", Collections.emptyList());
                model.addAttribute("recommendedBooks", Collections.emptyList());
            }
            
            return "dashboard";
        }
    }
    
    @GetMapping("/search")
    public String search(@RequestParam(required = false) String q, Model model, Principal principal) {
        try {
            User user = getCurrentUser(principal);
            model.addAttribute("user", user);
            model.addAttribute("query", q);
            
            List<Book> results = new ArrayList<>();
            if (q != null && !q.trim().isEmpty()) {
                String searchTerm = q.trim();
                results = bookRepository.findByTitleContainingIgnoreCase(searchTerm);
                results.addAll(bookRepository.findByAuthorContainingIgnoreCase(searchTerm));
                results.addAll(bookRepository.findByGenreContainingIgnoreCase(searchTerm));
                
                // Remove duplicates by ID
                results = results.stream()
                    .collect(Collectors.collectingAndThen(
                        Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(Book::getId))),
                        ArrayList::new
                    ));
            }
            
            model.addAttribute("books", results);
            return "discover";
        } catch (Exception e) {
            System.err.println("Search error: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("user", getCurrentUser(principal));
            model.addAttribute("query", q);
            model.addAttribute("books", Collections.emptyList());
            model.addAttribute("errorMessage", "Search failed. Please try again.");
            return "discover";
        }
    }

    @GetMapping("/discover")
    public String discover(Model model, Principal principal) {
        try {
            User user = getCurrentUser(principal);
            model.addAttribute("user", user);
            model.addAttribute("books", bookRepository.findAll());
            return "discover";
        } catch (Exception e) {
            System.err.println("Discover error: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("user", getCurrentUser(principal));
            model.addAttribute("books", Collections.emptyList());
            model.addAttribute("errorMessage", "Failed to load books. Please try again.");
            return "discover";
        }
    }

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/signin";
        }

        User user = getCurrentUser(principal);
        model.addAttribute("user", user);
        return "profile";
    }
    
    @GetMapping("/book/{id}")
    public String viewBook(@PathVariable Long id, Model model, Principal principal) {
        Book book = bookRepository.findById(id).orElse(null);
        if (book == null) {
            return "redirect:/dashboard";
        }
        
        User user = getCurrentUser(principal);
        
        model.addAttribute("book", book);
        model.addAttribute("user", user);
        return "book-detail";
    }

    @PostMapping("/book/{id}/review")
    public String submitReview(@PathVariable Long id,
                               @RequestParam Integer rating,
                               @RequestParam String review,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/signin";
        }

        Book book = bookRepository.findById(id).orElse(null);
        User user = getCurrentUser(principal);

        if (book == null || user == null) {
            redirectAttributes.addFlashAttribute("reviewError", "Unable to save your review.");
            return "redirect:/book/" + id;
        }

        Rating userRating = ratingRepository.findByUserAndBook(user, book).orElseGet(Rating::new);
        userRating.setUser(user);
        userRating.setBook(book);
        userRating.setRating(rating);
        userRating.setReview(review);
        ratingRepository.save(userRating);
        
        List<Rating> bookRatings = ratingRepository.findByBook(book);
        if (!bookRatings.isEmpty()) {
            double sum = 0.0;
            for (Rating r : bookRatings) {
                sum += r.getRating();
            }
            book.setStoredAverageRating(sum / bookRatings.size());
            bookRepository.save(book);
        }

        redirectAttributes.addFlashAttribute("reviewSuccess", "Your review was saved.");
        return "redirect:/book/" + id;
    }
}