package com.smartbook.controller;

import java.security.Principal;
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

@Controller
public class BookController {
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        if (principal != null) {
            User user = userRepository.findByUsername(principal.getName()).orElse(null);
            model.addAttribute("user", user);
        }
        
        List<Book> books = bookRepository.findAll();
        model.addAttribute("books", books);
        return "dashboard";
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