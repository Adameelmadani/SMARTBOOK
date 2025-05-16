package com.smartbook.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartbook.repository.BookRepository;
import java.util.ArrayList;
import java.util.List;
import com.smartbook.model.Book;

@Controller
public class HomeController {
    
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
    
    @Autowired
    private BookRepository bookRepository;
    
    @GetMapping("/")
    public String home(Model model) {
        try {
            List<Book> allBooks = bookRepository.findAll();
            List<Book> featuredBooks = new ArrayList<>();
            
            if (!allBooks.isEmpty()) {
                featuredBooks = allBooks.subList(0, Math.min(4, allBooks.size()));
            }
            
            model.addAttribute("featuredBooks", featuredBooks);
            return "home";
        } catch (Exception e) {
            logger.error("Error loading home page: ", e);
            model.addAttribute("featuredBooks", new ArrayList<>());
            return "home";
        }
    }
    
    // Explicit mapping for /home to match security config
    @GetMapping("/home")
    public String homePage(Model model) {
        return home(model);
    }
}