package com.smartbook.config;

import com.smartbook.model.*;
import com.smartbook.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
@Profile("dev") // Only run in development environment
public class DataSeeder {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private BookProgressRepository bookProgressRepository;
    
    @Autowired
    private RatingRepository ratingRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    @DependsOn("databaseInitializer") // Make sure roles are created first
    public CommandLineRunner seedDatabase() {
        return args -> {
            // Skip seeding if data already exists
            if (userRepository.count() > 0) {
                System.out.println("Database already has data, skipping seeding");
                return;
            }
            
            System.out.println("Seeding database with sample data...");
            
            // Create users
            List<User> users = createUsers();
            
            // Create books
            List<Book> books = createBooks();
            
            // Create book progress and ratings
            createBookProgressAndRatings(users, books);
            
            System.out.println("Database seeding completed!");
        };
    }
    
    private List<User> createUsers() {
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@example.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        Set<Role> adminRoles = new HashSet<>();
        adminRoles.add(userRole);
        adminRoles.add(adminRole);
        admin.setRoles(adminRoles);
        
        User user1 = new User();
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        user1.setPassword(passwordEncoder.encode("password"));
        Set<Role> user1Roles = new HashSet<>();
        user1Roles.add(userRole);
        user1.setRoles(user1Roles);
        
        User user2 = new User();
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setPassword(passwordEncoder.encode("password"));
        Set<Role> user2Roles = new HashSet<>();
        user2Roles.add(userRole);
        user2.setRoles(user2Roles);
        
        return userRepository.saveAll(Arrays.asList(admin, user1, user2));
    }
    
    private List<Book> createBooks() {
        Book book1 = new Book();
        book1.setTitle("The Great Gatsby");
        book1.setAuthor("F. Scott Fitzgerald");
        book1.setGenre("Classic");
        book1.setDescription("A story of wealth, love, and tragedy in the Roaring Twenties.");
        book1.setFilePath("/books/greatgatsby.pdf");
        book1.setCoverImagePath("/images/covers/greatgatsby.jpg");
        
        Book book2 = new Book();
        book2.setTitle("To Kill a Mockingbird");
        book2.setAuthor("Harper Lee");
        book2.setGenre("Classic");
        book2.setDescription("A story about racial injustice and moral growth in the American South.");
        book2.setFilePath("/books/tokillamockingbird.pdf");
        book2.setCoverImagePath("/images/covers/tokillamockingbird.jpg");
        
        Book book3 = new Book();
        book3.setTitle("1984");
        book3.setAuthor("George Orwell");
        book3.setGenre("Dystopian");
        book3.setDescription("A dystopian novel about totalitarianism and surveillance.");
        book3.setFilePath("/books/1984.pdf");
        book3.setCoverImagePath("/images/covers/1984.jpg");
        
        Book book4 = new Book();
        book4.setTitle("The Hobbit");
        book4.setAuthor("J.R.R. Tolkien");
        book4.setGenre("Fantasy");
        book4.setDescription("A fantasy adventure about Bilbo Baggins' journey to the Lonely Mountain.");
        book4.setFilePath("/books/hobbit.pdf");
        book4.setCoverImagePath("/images/covers/hobbit.jpg");
        
        return bookRepository.saveAll(Arrays.asList(book1, book2, book3, book4));
    }
    
    private void createBookProgressAndRatings(List<User> users, List<Book> books) {
        User admin = users.get(0);
        User user1 = users.get(1);
        User user2 = users.get(2);
        
        Book book1 = books.get(0); // Great Gatsby
        Book book2 = books.get(1); // To Kill a Mockingbird
        Book book3 = books.get(2); // 1984
        Book book4 = books.get(3); // The Hobbit
        
        // Admin progress and ratings
        BookProgress adminProgress1 = new BookProgress();
        adminProgress1.setUser(admin);
        adminProgress1.setBook(book1);
        adminProgress1.setCurrentPage(180);
        adminProgress1.setTotalPages(250);
        adminProgress1.setLastReadAt(LocalDateTime.now().minusDays(3));
        bookProgressRepository.save(adminProgress1);
        
        Rating adminRating1 = new Rating();
        adminRating1.setUser(admin);
        adminRating1.setBook(book1);
        adminRating1.setRating(5); // Changed from 4.5 to 5
        ratingRepository.save(adminRating1);
        
        // User1 progress and ratings
        BookProgress user1Progress1 = new BookProgress();
        user1Progress1.setUser(user1);
        user1Progress1.setBook(book1);
        user1Progress1.setCurrentPage(250);
        user1Progress1.setTotalPages(250);
        user1Progress1.setLastReadAt(LocalDateTime.now().minusDays(10));
        bookProgressRepository.save(user1Progress1);
        
        Rating user1Rating1 = new Rating();
        user1Rating1.setUser(user1);
        user1Rating1.setBook(book1);
        user1Rating1.setRating(5); // Changed from 5.0 to 5
        ratingRepository.save(user1Rating1);
        
        BookProgress user1Progress2 = new BookProgress();
        user1Progress2.setUser(user1);
        user1Progress2.setBook(book2);
        user1Progress2.setCurrentPage(150);
        user1Progress2.setTotalPages(300);
        user1Progress2.setLastReadAt(LocalDateTime.now().minusDays(1));
        bookProgressRepository.save(user1Progress2);
        
        // User2 progress and ratings
        BookProgress user2Progress1 = new BookProgress();
        user2Progress1.setUser(user2);
        user2Progress1.setBook(book3);
        user2Progress1.setCurrentPage(120);
        user2Progress1.setTotalPages(328);
        user2Progress1.setLastReadAt(LocalDateTime.now().minusHours(12));
        bookProgressRepository.save(user2Progress1);
        
        Rating user2Rating1 = new Rating();
        user2Rating1.setUser(user2);
        user2Rating1.setBook(book3);
        user2Rating1.setRating(4); // Changed from 4.0 to 4
        ratingRepository.save(user2Rating1);
        
        BookProgress user2Progress2 = new BookProgress();
        user2Progress2.setUser(user2);
        user2Progress2.setBook(book4);
        user2Progress2.setCurrentPage(280);
        user2Progress2.setTotalPages(320);
        user2Progress2.setLastReadAt(LocalDateTime.now().minusWeeks(1));
        bookProgressRepository.save(user2Progress2);
        
        Rating user2Rating2 = new Rating();
        user2Rating2.setUser(user2);
        user2Rating2.setBook(book4);
        user2Rating2.setRating(5); // Changed from 4.8 to 5
        ratingRepository.save(user2Rating2);
    }
}
