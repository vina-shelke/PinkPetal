package com.pinkpetal.periodtracker.services;

import org.springframework.stereotype.Service;

@Service
public class ChatbotService {

    public String getResponse(String message) {
        if (message == null || message.trim().isEmpty()) {
            return "Hello! How are you feeling today? I am here to support you. 💕";
        }
        
        String lowerMessage = message.toLowerCase();
        
        if (lowerMessage.contains("sad") || lowerMessage.contains("low") || lowerMessage.contains("depressed") || lowerMessage.contains("cry")) {
            return "It's okay to feel low or sad, sweetie. 💕 Hormonal shifts can heavily influence our emotions. Take a deep breath, grab some water, and remember that your feelings are valid. You are doing great.";
        } else if (lowerMessage.contains("angry") || lowerMessage.contains("mad") || lowerMessage.contains("frustrated") || lowerMessage.contains("irritated")) {
            return "It's completely normal to feel irritable or angry. 🌸 PMS changes can make our nervous system extra sensitive. Try to take a few slow, deep breaths, or step away for a cup of tea. Give yourself grace today.";
        } else if (lowerMessage.contains("tired") || lowerMessage.contains("exhausted") || lowerMessage.contains("fatigue") || lowerMessage.contains("sleepy")) {
            return "Feeling exhausted is your body's signal to slow down. 💤 Progesterone changes can drain your energy. Remember, rest is productive too! Lie down and let yourself recharge today.";
        } else if (lowerMessage.contains("pain") || lowerMessage.contains("cramp") || lowerMessage.contains("hurt") || lowerMessage.contains("ache")) {
            return "I'm so sorry to hear you are in pain. 🌸 Menstrual cramps can be tough. Try using a warm heating pad, sipping warm tea, or doing gentle stretches (like child's pose). Please rest, and consult a doctor if it gets severe.";
        } else if (lowerMessage.contains("stressed") || lowerMessage.contains("anxious") || lowerMessage.contains("stress") || lowerMessage.contains("worried")) {
            return "Take a deep breath. 🧘‍♀️ Let your shoulders drop. You don't have to handle everything right now. Try our breathing bubble exercise in the Self-Care zone to help calm your thoughts. You are strong and capable!";
        } else if (lowerMessage.contains("hello") || lowerMessage.contains("hi") || lowerMessage.contains("hey")) {
            return "Hello there! 💕 I'm your PinkPetal support chatbot. How are you feeling right now? (e.g., sad, tired, stressed, in pain, or just normal?)";
        } else {
            return "Thank you for sharing. 💕 Hormonal cycles affect both our mind and body in unique ways. Please remember to drink plenty of water, eat nourishing food, and speak to yourself with kindness today. You've got this!";
        }
    }
}
