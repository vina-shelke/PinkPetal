package com.pinkpetal.periodtracker.controllers;

import com.pinkpetal.periodtracker.services.ChatbotService;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ChatbotController {

    @Autowired
    private ChatbotService chatbotService;

    @GetMapping("/chatbot")
    public String showChatbot(HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        return "chatbot";
    }

    @PostMapping("/api/chatbot/message")
    @ResponseBody
    public Map<String, String> getChatbotResponse(
            @RequestParam("message") String message,
            HttpSession session) {
        if (session.getAttribute("user") == null) {
            return Map.of("response", "Session expired. Please log in again.");
        }
        
        String response = chatbotService.getResponse(message);
        return Map.of("response", response);
    }
}
