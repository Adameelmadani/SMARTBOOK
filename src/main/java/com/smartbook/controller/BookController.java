package com.smartbook.controller;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.smartbook.repository.BookRepository;
import com.smartbook.repository.UserRepository;
import com.smartbook.model.Book;
import com.smartbook.model.User;
import com.smartbook.service.RecommendationService;

@Controller
public class BookController {
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RecommendationService recommendationService;
    
    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        try {
            User user = null;
            if (principal != null) {
                user = userRepository.findByUsername(principal.getName()).orElse(null);
                model.addAttribute("user", user);
            }
            
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
    
    @GetMapping("/book/{id}")
    public String viewBook(@PathVariable Long id, Model model, Principal principal) {
        Book book = bookRepository.findById(id).orElse(null);
        if (book == null) {
            return "redirect:/dashboard";
        }
        
        User user = null;
        if (principal != null) {
            user = userRepository.findByUsername(principal.getName()).orElse(null);
        }
        
        model.addAttribute("book", book);
        model.addAttribute("user", user);
        return "book-detail";
    }
}