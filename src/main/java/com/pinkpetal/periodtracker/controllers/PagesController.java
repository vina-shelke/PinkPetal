package com.pinkpetal.periodtracker.controllers;

import com.pinkpetal.periodtracker.models.*;
import com.pinkpetal.periodtracker.repositories.*;
import com.pinkpetal.periodtracker.services.TrackingService;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PagesController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private SymptomRepository symptomRepository;

    @Autowired
    private CycleRepository cycleRepository;

    @Autowired
    private TrackingService trackingService;

    @GetMapping("/")
    public String showLandingPage(HttpSession session) {
        if (session.getAttribute("user") != null) {
            return "redirect:/dashboard";
        }
        if (session.getAttribute("admin") != null) {
            return "redirect:/admin";
        }
        return "landing";
    }

    @GetMapping("/selfcare")
    public String showSelfCare(HttpSession session) {
        if (session.getAttribute("user") == null && session.getAttribute("admin") == null) {
            return "redirect:/login";
        }
        return "selfcare";
    }

    @GetMapping("/education")
    public String showEducation(HttpSession session) {
        if (session.getAttribute("user") == null && session.getAttribute("admin") == null) {
            return "redirect:/login";
        }
        return "education";
    }

    @GetMapping("/compassion")
    public String showCompassion(HttpSession session) {
        if (session.getAttribute("user") == null && session.getAttribute("admin") == null) {
            return "redirect:/login";
        }
        return "compassion";
    }

    @GetMapping("/admin")
    public String showAdminPanel(
            @RequestParam(value = "searchQuery", required = false) String searchQuery,
            HttpSession session,
            Model model) {
        if (session.getAttribute("admin") == null) {
            return "redirect:/login";
        }

        List<User> users;
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            String query = searchQuery.trim().toLowerCase();
            users = userRepository.findAll();
            users.removeIf(u -> "admin".equalsIgnoreCase(u.getUserId()));
            users.removeIf(u -> !u.getUserId().toLowerCase().contains(query) &&
                               (u.getName() == null || !u.getName().toLowerCase().contains(query)));
            model.addAttribute("searchQuery", searchQuery);
        } else {
            users = userRepository.findAll();
            users.removeIf(u -> "admin".equalsIgnoreCase(u.getUserId()));
        }
        model.addAttribute("users", users);

        List<Post> posts = postRepository.findAllByOrderByDateDesc();
        model.addAttribute("posts", posts);

        List<Comment> comments = commentRepository.findAll();
        model.addAttribute("comments", comments);

        // --- Calculate Chart Metrics ---
        // 1. Monthly Registrations (March, April, May)
        int marchReg = 0, aprilReg = 0, mayReg = 0;
        List<User> allUsers = userRepository.findAll();
        for (User u : allUsers) {
            if ("admin".equalsIgnoreCase(u.getUserId())) continue;
            if (u.getRegistrationDate() != null) {
                int month = u.getRegistrationDate().getMonthValue();
                if (month == 3) marchReg++;
                else if (month == 4) aprilReg++;
                else if (month == 5) mayReg++;
            }
        }
        model.addAttribute("regMarch", marchReg);
        model.addAttribute("regApril", aprilReg);
        model.addAttribute("regMay", mayReg);

        // 2. Symptoms Frequency
        int crampsCount = 0, bloatingCount = 0, headacheCount = 0, moodCount = 0;
        int fatigueCount = 0, acneCount = 0, cravingsCount = 0, backacheCount = 0;

        List<Symptom> allSymptoms = symptomRepository.findAll();
        for (Symptom s : allSymptoms) {
            String text = s.getSymptoms().toLowerCase();
            if (text.contains("cramps")) crampsCount++;
            if (text.contains("bloating")) bloatingCount++;
            if (text.contains("headache")) headacheCount++;
            if (text.contains("mood")) moodCount++;
            if (text.contains("fatigue")) fatigueCount++;
            if (text.contains("acne")) acneCount++;
            if (text.contains("craving") || text.contains("food")) cravingsCount++;
            if (text.contains("backache")) backacheCount++;
        }

        // Also add from Cycles table symptoms if logged there
        List<Cycle> allCycles = cycleRepository.findAll();
        for (Cycle c : allCycles) {
            if (c.getSymptoms() != null) {
                String text = c.getSymptoms().toLowerCase();
                if (text.contains("cramps")) crampsCount++;
                if (text.contains("bloating")) bloatingCount++;
                if (text.contains("headache")) headacheCount++;
                if (text.contains("mood")) moodCount++;
                if (text.contains("fatigue")) fatigueCount++;
                if (text.contains("acne")) acneCount++;
                if (text.contains("craving") || text.contains("food")) cravingsCount++;
                if (text.contains("backache")) backacheCount++;
            }
        }

        model.addAttribute("symptomCramps", crampsCount);
        model.addAttribute("symptomBloating", bloatingCount);
        model.addAttribute("symptomHeadache", headacheCount);
        model.addAttribute("symptomMood", moodCount);
        model.addAttribute("symptomFatigue", fatigueCount);
        model.addAttribute("symptomAcne", acneCount);
        model.addAttribute("symptomCravings", cravingsCount);
        model.addAttribute("symptomBackache", backacheCount);

        // 3. Cycle Regularity
        int regularCount = 0;
        int irregularCount = 0;
        for (User u : allUsers) {
            if ("admin".equalsIgnoreCase(u.getUserId())) continue;
            List<Cycle> userCycles = cycleRepository.findByUserIdOrderByPeriodStartDateDesc(u.getUserId());
            boolean isIrregular = trackingService.detectIrregularity(userCycles);
            if (isIrregular) {
                irregularCount++;
            } else {
                regularCount++;
            }
        }
        model.addAttribute("regularCount", regularCount);
        model.addAttribute("irregularCount", irregularCount);

        return "admin";
    }

    @PostMapping("/admin/delete-user")
    public String deleteUser(@RequestParam("userId") String userId, HttpSession session) {
        if (session.getAttribute("admin") == null) {
            return "redirect:/login";
        }
        if (!"admin".equalsIgnoreCase(userId)) {
            userRepository.deleteById(userId);
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/delete-post")
    public String deletePost(@RequestParam("postId") Integer postId, HttpSession session) {
        if (session.getAttribute("admin") == null) {
            return "redirect:/login";
        }
        postRepository.deleteById(postId);
        return "redirect:/admin";
    }

    @PostMapping("/admin/edit-post")
    public String editPost(
            @RequestParam("postId") Integer postId,
            @RequestParam("content") String content,
            HttpSession session) {
        if (session.getAttribute("admin") == null) {
            return "redirect:/login";
        }
        Optional<Post> optPost = postRepository.findById(postId);
        if (optPost.isPresent()) {
            Post post = optPost.get();
            post.setContent(content);
            postRepository.save(post);
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/delete-comment")
    public String deleteComment(@RequestParam("commentId") Integer commentId, HttpSession session) {
        if (session.getAttribute("admin") == null) {
            return "redirect:/login";
        }
        commentRepository.deleteById(commentId);
        return "redirect:/admin";
    }
}
