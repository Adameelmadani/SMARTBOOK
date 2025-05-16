package com.smartbook.controller;

import java.util.HashSet;
import java.util.Set;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.smartbook.repository.RoleRepository;
import com.smartbook.repository.UserRepository;
import com.smartbook.model.ERole;
import com.smartbook.model.Role;
import com.smartbook.model.User;

@Controller
public class AuthController {
    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @GetMapping("/signin")
    public String showLoginForm() {
        return "signin";
    }

    @GetMapping("/signup")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "signup";
    }

    @PostMapping("/signup")
    public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult bindingResult, Model model) {
        try {
            if (bindingResult.hasErrors()) {
                return "signup";
            }

            if (userRepository.existsByUsername(user.getUsername())) {
                model.addAttribute("usernameError", "Username is already taken!");
                return "signup";
            }

            if (userRepository.existsByEmail(user.getEmail())) {
                model.addAttribute("emailError", "Email is already in use!");
                return "signup";
            }

            // Create new user's account
            User newUser = new User(user.getUsername(), user.getEmail(), encoder.encode(user.getPassword()));

            Set<Role> roles = new HashSet<>();
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
            newUser.setRoles(roles);
            userRepository.save(newUser);

            return "redirect:/signin?registered";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "An error occurred: " + e.getMessage());
            return "signup";
        }
    }
}