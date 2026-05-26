package com.pinkpetal.periodtracker.services;

import com.pinkpetal.periodtracker.models.NotificationHistory;
import com.pinkpetal.periodtracker.models.UserPreference;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.springframework.stereotype.Service;

@Service
public class CareNotificationService {

    private static final Random random = new Random();

    // 1. General Emotional Support
    private static final String[] EMOTIONAL_MESSAGES = {
        "Be gentle with yourself today 🤍",
        "Rest is productive too 🌙",
        "Slow days are okay 💖",
        "You are doing enough today 🌸",
        "It's okay if all you did today was breathe 🌷",
        "You are worthy of care and kindness today 💕"
    };

    // 2. Hydration
    private static final String[] HYDRATION_MESSAGES = {
        "Hydration check 💧 Your body needs extra water today.",
        "A warm cup of herbal tea can feel so comforting right now ☕",
        "Take a slow sip of water. Let's stay hydrated together 💧"
    };

    // 3. Hygiene
    private static final String[] HYGIENE_MESSAGES = {
        "Care check-in: freshen up if you need to feel renewed ✨",
        "Wear your most comfortable clothes today. Comfort is priority 💖",
        "A warm shower can help soothe your body and clear your mind 🌸"
    };

    // 4. Comfort Suggestions
    private static final String[] COMFORT_MESSAGES = {
        "A heating pad on your lower back or tummy may ease tension today 🌷",
        "Try some light cat-cow stretches or child's pose to relax your muscles 🧘‍♀️",
        "Give yourself permission to pause and curl up with a cozy blanket 🌸"
    };

    // 5. Rest & Sleep
    private static final String[] REST_MESSAGES = {
        "Your body is working hard in the background. Rest is key 🤍",
        "Try winding down 30 minutes earlier tonight. Sweet dreams 🌙",
        "It is perfectly fine to cancel plans and rest your body today ✨"
    };

    // 6. Wellness & Nutrition
    private static final String[] NUTRITION_MESSAGES = {
        "Iron-rich foods like spinach or lentils can help replenish your energy 🥗",
        "Nourish your body with something warm and wholesome today 🍲",
        "Make sure to eat regular, comforting meals. You deserve to feel fueled 💖"
    };

    public List<NotificationHistory> generateDailyNotifications(
            String userId, 
            String cyclePhase, 
            List<String> symptomsToday, 
            String moodToday, 
            UserPreference prefs) {
        
        List<NotificationHistory> pool = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // 1. Mood-based support (High Priority)
        if (prefs.isMoodSupportEnabled() && moodToday != null && !moodToday.isEmpty()) {
            String m = moodToday.toLowerCase();
            if (m.contains("sad") || m.contains("emotional")) {
                pool.add(new NotificationHistory(userId, now.minusMinutes(120), "Today feels heavy, and that's completely okay 🤍 You are not alone.", "Mood"));
                pool.add(new NotificationHistory(userId, now.minusMinutes(40), "It is okay to cry or just want to be quiet. Be gentle with your heart today 🌸", "Mood"));
            } else if (m.contains("tired") || m.contains("low energy")) {
                pool.add(new NotificationHistory(userId, now.minusMinutes(100), "Slow, restful days are allowed. Listen to your body and rest 🌸", "Mood"));
                pool.add(new NotificationHistory(userId, now.minusMinutes(20), "Your energy levels are low. Take it easy and skip the non-essential tasks today ✨", "Mood"));
            } else if (m.contains("anxious")) {
                pool.add(new NotificationHistory(userId, now.minusMinutes(80), "Pause for a moment, place a hand on your heart, and breathe slowly 💕", "Mood"));
                pool.add(new NotificationHistory(userId, now.minusMinutes(10), "Inhale calm, exhale stress. You are safe, and this moment will pass 🌸", "Mood"));
            } else if (m.contains("irritated") || m.contains("angry")) {
                pool.add(new NotificationHistory(userId, now.minusMinutes(90), "Take a small, quiet break just for yourself. You deserve space ✨", "Mood"));
                pool.add(new NotificationHistory(userId, now.minusMinutes(30), "Irritability is normal when hormones shift. Give yourself some grace today 🤍", "Mood"));
            } else {
                pool.add(new NotificationHistory(userId, now.minusMinutes(60), "Sending you a little spark of warmth to carry through your day 💖", "Mood"));
            }
        }

        // 2. Symptom-based wellness suggestions
        if (prefs.isWellnessSuggestionsEnabled() && symptomsToday != null && !symptomsToday.isEmpty()) {
            for (String symptom : symptomsToday) {
                String s = symptom.toLowerCase();
                if (s.contains("cramps")) {
                    pool.add(new NotificationHistory(userId, now.minusMinutes(110), "A warm heating pad and chamomile tea may help soothe your cramps today 🌷", "Symptom"));
                }
                if (s.contains("headache")) {
                    pool.add(new NotificationHistory(userId, now.minusMinutes(95), "Try resting your eyes in a dim room and sipping some cool water 💧", "Symptom"));
                }
                if (s.contains("backache") || s.contains("back pain")) {
                    pool.add(new NotificationHistory(userId, now.minusMinutes(85), "Gentle child's pose or lower back self-massage can relieve period back pain 🧘‍♀️", "Symptom"));
                }
                if (s.contains("fatigue")) {
                    pool.add(new NotificationHistory(userId, now.minusMinutes(75), "Your body is expending a lot of energy. A 20-minute power nap could feel amazing 🌙", "Symptom"));
                }
                if (s.contains("bloating")) {
                    pool.add(new NotificationHistory(userId, now.minusMinutes(65), "Peppermint ginger infusion works wonders for period bloating. Avoid carbonated drinks today ☕", "Symptom"));
                }
            }
        }

        // 3. Cycle Phase general suggestions
        if ("Period".equalsIgnoreCase(cyclePhase)) {
            pool.add(new NotificationHistory(userId, now.minusMinutes(150), getRandomMessage(HYGIENE_MESSAGES), "Hygiene"));
            pool.add(new NotificationHistory(userId, now.minusMinutes(130), getRandomMessage(COMFORT_MESSAGES), "Comfort"));
            pool.add(new NotificationHistory(userId, now.minusMinutes(70), getRandomMessage(REST_MESSAGES), "Rest"));
        } else if ("PMS".equalsIgnoreCase(cyclePhase)) {
            pool.add(new NotificationHistory(userId, now.minusMinutes(140), getRandomMessage(EMOTIONAL_MESSAGES), "Emotional"));
            pool.add(new NotificationHistory(userId, now.minusMinutes(100), getRandomMessage(REST_MESSAGES), "Rest"));
            pool.add(new NotificationHistory(userId, now.minusMinutes(50), getRandomMessage(NUTRITION_MESSAGES), "Nutrition"));
        } else if ("Ovulation".equalsIgnoreCase(cyclePhase)) {
            pool.add(new NotificationHistory(userId, now.minusMinutes(160), "Your energy and focus might feel heightened today. Embrace your glow! 💖", "Cycle"));
            pool.add(new NotificationHistory(userId, now.minusMinutes(120), getRandomMessage(HYDRATION_MESSAGES), "Hydration"));
        } else {
            pool.add(new NotificationHistory(userId, now.minusMinutes(180), getRandomMessage(EMOTIONAL_MESSAGES), "Emotional"));
            pool.add(new NotificationHistory(userId, now.minusMinutes(110), getRandomMessage(HYDRATION_MESSAGES), "Hydration"));
        }

        // Add standard fallbacks if pool is small
        if (pool.size() < 5) {
            pool.add(new NotificationHistory(userId, now.minusMinutes(200), getRandomMessage(EMOTIONAL_MESSAGES), "Emotional"));
            pool.add(new NotificationHistory(userId, now.minusMinutes(120), getRandomMessage(HYDRATION_MESSAGES), "Hydration"));
            pool.add(new NotificationHistory(userId, now.minusMinutes(60), getRandomMessage(NUTRITION_MESSAGES), "Nutrition"));
        }

        // Determine target count based on Preference Intensity
        int targetCount = 3; // Default 'Balanced'
        String intensity = prefs.getSmartNotifIntensity();
        if ("Minimal".equalsIgnoreCase(intensity)) {
            targetCount = 1;
        } else if ("Frequent Care".equalsIgnoreCase(intensity)) {
            targetCount = 5;
        }

        // Shuffle pool and choose the target count of notifications
        Collections.shuffle(pool);
        List<NotificationHistory> selected = new ArrayList<>();
        for (int i = 0; i < Math.min(targetCount, pool.size()); i++) {
            NotificationHistory item = pool.get(i);
            // Stagger the time stamps so they appear spread out during the day
            item.setDate(now.minusHours(targetCount - i - 1).minusMinutes(random.nextInt(45)));
            selected.add(item);
        }

        return selected;
    }

    private String getRandomMessage(String[] array) {
        return array[random.nextInt(array.length)];
    }
}
