package com.pinkpetal.periodtracker.controllers;

import com.pinkpetal.periodtracker.models.Admin;
import com.pinkpetal.periodtracker.models.User;
import com.pinkpetal.periodtracker.repositories.AdminRepository;
import com.pinkpetal.periodtracker.repositories.UserRepository;
import com.pinkpetal.periodtracker.utils.PasswordHasher;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRepository adminRepository;

    @GetMapping("/login")
    public String showLoginPage(HttpSession session) {
        if (session.getAttribute("user") != null) {
            return "redirect:/dashboard";
        }
        if (session.getAttribute("admin") != null) {
            return "redirect:/admin";
        }
        return "login";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam("username") String username,
                               @RequestParam("name") String name,
                               @RequestParam("password") String password,
                               Model model) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty() || name == null || name.trim().isEmpty()) {
            model.addAttribute("registerError", "Name, Username, and Password cannot be empty");
            return "login";
        }
        if (userRepository.existsById(username)) {
            model.addAttribute("registerError", "Username already exists");
            return "login";
        }

        String hashedPassword = PasswordHasher.hashPassword(password);
        User user = new User(username, name, hashedPassword, 28, null);
        userRepository.save(user);

        model.addAttribute("registerSuccess", "Registration successful! Please login.");
        return "login";
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam("username") String username,
                            @RequestParam("password") String password,
                            HttpSession session,
                            Model model) {
        if (username == null || password == null) {
            model.addAttribute("loginError", "Invalid inputs");
            return "login";
        }
        String hashedPassword = PasswordHasher.hashPassword(password);

        // 1. Check isolated admin logins first
        Optional<Admin> optAdmin = adminRepository.findById(username);
        if (optAdmin.isPresent() && optAdmin.get().getPassword().equals(hashedPassword)) {
            session.setAttribute("admin", optAdmin.get().getAdminId());
            return "redirect:/admin";
        }

        // 2. Check regular users login
        Optional<User> optUser = userRepository.findById(username);
        if (optUser.isPresent() && optUser.get().getPassword().equals(hashedPassword)) {
            session.setAttribute("user", optUser.get().getUserId());
            return "redirect:/dashboard";
        } else {
            model.addAttribute("loginError", "Invalid username or password");
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
