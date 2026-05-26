package com.pinkpetal.periodtracker.controllers;

import com.pinkpetal.periodtracker.models.Cycle;
import com.pinkpetal.periodtracker.models.Reminder;
import com.pinkpetal.periodtracker.models.Symptom;
import com.pinkpetal.periodtracker.models.User;
import com.pinkpetal.periodtracker.repositories.CycleRepository;
import com.pinkpetal.periodtracker.repositories.ReminderRepository;
import com.pinkpetal.periodtracker.repositories.SymptomRepository;
import com.pinkpetal.periodtracker.repositories.UserRepository;
import com.pinkpetal.periodtracker.models.*;
import com.pinkpetal.periodtracker.repositories.*;
import com.pinkpetal.periodtracker.services.CareNotificationService;
import com.pinkpetal.periodtracker.services.TrackingService;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DashboardController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SymptomRepository symptomRepository;

    @Autowired
    private ReminderRepository reminderRepository;

    @Autowired
    private CycleRepository cycleRepository;

    @Autowired
    private TrackingService trackingService;

    @Autowired
    private MoodLogRepository moodLogRepository;

    @Autowired
    private UserPreferenceRepository userPreferenceRepository;

    @Autowired
    private NotificationHistoryRepository notificationHistoryRepository;

    @Autowired
    private CareNotificationService careNotificationService;

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        String username = (String) session.getAttribute("user");
        if (username == null) {
            return "redirect:/login";
        }

        Optional<User> optUser = userRepository.findById(username);
        if (!optUser.isPresent()) {
            return "redirect:/logout";
        }
        User user = optUser.get();
        model.addAttribute("user", user);

        // Fetch cycle history records
        List<Cycle> cycles = cycleRepository.findByUserIdOrderByPeriodStartDateDesc(username);
        model.addAttribute("cycles", cycles);

        // Calculate average cycle length and irregularity status
        int avgCycleLength = trackingService.calculateAverageCycleLength(cycles, user.getCycleLength() != null ? user.getCycleLength() : 28);
        boolean isIrregular = trackingService.detectIrregularity(cycles);
        LocalDate latestPeriodDate = trackingService.getLatestPeriodDate(cycles, user.getLastPeriodDate());

        model.addAttribute("avgCycleLength", avgCycleLength);
        model.addAttribute("isIrregular", isIrregular);
        model.addAttribute("latestPeriodDate", latestPeriodDate);

        // Core Predictions using calculated metrics
        if (latestPeriodDate != null) {
            TrackingService.CycleForecast forecast = trackingService.calculateForecast(latestPeriodDate, avgCycleLength);
            model.addAttribute("forecast", forecast);

            List<LocalDate> nextPeriods = trackingService.getHistoryPredictions(latestPeriodDate, avgCycleLength, 6);
            model.addAttribute("nextPeriods", nextPeriods);
        }

        // Fetch user symptoms and reminders
        List<Symptom> symptoms = symptomRepository.findByUserIdOrderByDateDesc(username);
        model.addAttribute("symptoms", symptoms);

        List<Reminder> reminders = reminderRepository.findByUserId(username);
        model.addAttribute("reminders", reminders);

        // --- 🌸 Smart Care & Comfort Reminder System additions ---
        // 1. Fetch or initialize UserPreferences
        UserPreference prefs = userPreferenceRepository.findById(username)
                .orElseGet(() -> {
                    UserPreference p = new UserPreference(username);
                    return userPreferenceRepository.save(p);
                });
        model.addAttribute("prefs", prefs);

        // 2. Fetch today's logged mood
        LocalDate today = LocalDate.now();
        List<MoodLog> moodTodayList = moodLogRepository.findByUserIdAndDate(username, today);
        String moodToday = "";
        String moodNotesToday = "";
        if (!moodTodayList.isEmpty()) {
            moodToday = moodTodayList.get(0).getMood();
            moodNotesToday = moodTodayList.get(0).getNotes();
        }
        model.addAttribute("moodToday", moodToday);
        model.addAttribute("moodNotesToday", moodNotesToday);

        // 3. Fetch today's logged symptoms list
        List<Symptom> symptomsTodayList = symptomRepository.findByUserIdAndDate(username, today);
        List<String> symptomsList = new ArrayList<>();
        if (!symptomsTodayList.isEmpty()) {
            String syms = symptomsTodayList.get(0).getSymptoms();
            if (syms != null && !syms.trim().isEmpty()) {
                for (String s : syms.split(",")) {
                    symptomsList.add(s.trim());
                }
            }
        }
        model.addAttribute("symptomsList", symptomsList);

        // 4. Calculate dynamic cycle phase
        TrackingService.CycleForecast forecast = null;
        if (latestPeriodDate != null) {
            forecast = trackingService.calculateForecast(latestPeriodDate, avgCycleLength);
        }
        String cyclePhase = calculateCyclePhase(today, latestPeriodDate, forecast != null ? forecast.getNextPeriodDate() : null, forecast != null ? forecast.getOvulationDate() : null);
        model.addAttribute("cyclePhase", cyclePhase);

        // 5. Generate / Retrieve today's smart care notifications
        LocalDateTime startOfDay = today.atStartOfDay();
        List<NotificationHistory> todayNotifs = notificationHistoryRepository.findByUserIdAndDateAfterOrderByDateDesc(username, startOfDay);
        if (todayNotifs.isEmpty()) {
            List<NotificationHistory> newNotifs = careNotificationService.generateDailyNotifications(username, cyclePhase, symptomsList, moodToday, prefs);
            if (!newNotifs.isEmpty()) {
                notificationHistoryRepository.saveAll(newNotifs);
                todayNotifs = newNotifs;
            }
        }
        model.addAttribute("smartNotifications", todayNotifs);

        return "dashboard";
    }

    @PostMapping("/dashboard/add-cycle-record")
    public String addCycleRecord(
            @RequestParam("periodStartDate") String startDateStr,
            @RequestParam("cycleLength") Integer cycleLength,
            @RequestParam(value = "symptoms", required = false) String symptoms,
            HttpSession session) {
        String username = (String) session.getAttribute("user");
        if (username == null) {
            return "redirect:/login";
        }

        if (startDateStr != null && !startDateStr.isEmpty() && cycleLength != null) {
            LocalDate startDate = LocalDate.parse(startDateStr);
            Cycle cycle = new Cycle(username, startDate, cycleLength, symptoms == null ? "" : symptoms);
            cycleRepository.save(cycle);

            // Synchronize User profile lastPeriodDate & cycleLength
            Optional<User> optUser = userRepository.findById(username);
            if (optUser.isPresent()) {
                User user = optUser.get();
                List<Cycle> cycles = cycleRepository.findByUserIdOrderByPeriodStartDateDesc(username);
                int avgLength = trackingService.calculateAverageCycleLength(cycles, cycleLength);
                LocalDate latestPeriod = trackingService.getLatestPeriodDate(cycles, startDate);
                user.setLastPeriodDate(latestPeriod);
                user.setCycleLength(avgLength);
                userRepository.save(user);
            }
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/dashboard/delete-cycle-record")
    public String deleteCycleRecord(
            @RequestParam("id") Integer id,
            HttpSession session) {
        String username = (String) session.getAttribute("user");
        if (username == null) {
            return "redirect:/login";
        }

        Optional<Cycle> optCycle = cycleRepository.findById(id);
        if (optCycle.isPresent() && optCycle.get().getUserId().equals(username)) {
            cycleRepository.delete(optCycle.get());

            // Synchronize User profile
            Optional<User> optUser = userRepository.findById(username);
            if (optUser.isPresent()) {
                User user = optUser.get();
                List<Cycle> cycles = cycleRepository.findByUserIdOrderByPeriodStartDateDesc(username);
                if (cycles.isEmpty()) {
                    user.setLastPeriodDate(null);
                    user.setCycleLength(28);
                } else {
                    int avgLength = trackingService.calculateAverageCycleLength(cycles, 28);
                    LocalDate latestPeriod = trackingService.getLatestPeriodDate(cycles, null);
                    user.setLastPeriodDate(latestPeriod);
                    user.setCycleLength(avgLength);
                }
                userRepository.save(user);
            }
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/dashboard/update-cycle")
    public String updateCycle(
            @RequestParam("cycleLength") Integer cycleLength,
            @RequestParam("lastPeriodDate") String lastPeriodDateStr,
            HttpSession session) {
        String username = (String) session.getAttribute("user");
        if (username == null) {
            return "redirect:/login";
        }

        Optional<User> optUser = userRepository.findById(username);
        if (optUser.isPresent()) {
            User user = optUser.get();
            user.setCycleLength(cycleLength);
            if (lastPeriodDateStr != null && !lastPeriodDateStr.isEmpty()) {
                user.setLastPeriodDate(LocalDate.parse(lastPeriodDateStr));
            }
            userRepository.save(user);
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/dashboard/log-symptom")
    public String logSymptom(
            @RequestParam("date") String dateStr,
            @RequestParam("symptoms") String symptomsList,
            HttpSession session) {
        String username = (String) session.getAttribute("user");
        if (username == null) {
            return "redirect:/login";
        }

        if (dateStr != null && !dateStr.isEmpty() && symptomsList != null && !symptomsList.trim().isEmpty()) {
            Symptom symptom = new Symptom(username, LocalDate.parse(dateStr), symptomsList);
            symptomRepository.save(symptom);
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/dashboard/add-reminder")
    public String addReminder(
            @RequestParam("type") String type,
            @RequestParam("time") String time,
            @RequestParam(value = "notes", required = false) String notes,
            @RequestParam(value = "repeatSchedule", required = false, defaultValue = "Once") String repeatSchedule,
            @RequestParam(value = "soundEnabled", required = false, defaultValue = "true") boolean soundEnabled,
            @RequestParam(value = "vibrationEnabled", required = false, defaultValue = "true") boolean vibrationEnabled,
            @RequestParam(value = "intervalMinutes", required = false, defaultValue = "0") Integer intervalMinutes,
            HttpSession session) {
        String username = (String) session.getAttribute("user");
        if (username == null) {
            return "redirect:/login";
        }

        if (type != null && !type.isEmpty() && time != null && !time.isEmpty()) {
            Reminder reminder = new Reminder(username, type, time, "Active", 
                    notes == null ? "" : notes, repeatSchedule, soundEnabled, vibrationEnabled, intervalMinutes, false);
            reminderRepository.save(reminder);
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/dashboard/snooze-reminder")
    public String snoozeReminder(@RequestParam("id") Integer id, HttpSession session) {
        String username = (String) session.getAttribute("user");
        if (username == null) {
            return "redirect:/login";
        }
        Optional<Reminder> optReminder = reminderRepository.findById(id);
        if (optReminder.isPresent() && optReminder.get().getUserId().equals(username)) {
            Reminder r = optReminder.get();
            r.setSnoozed(true);
            r.setTime("Snoozed (10 mins)");
            reminderRepository.save(r);
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/dashboard/toggle-reminder")
    public String toggleReminder(@RequestParam("id") Integer id, HttpSession session) {
        String username = (String) session.getAttribute("user");
        if (username == null) {
            return "redirect:/login";
        }
        Optional<Reminder> optReminder = reminderRepository.findById(id);
        if (optReminder.isPresent() && optReminder.get().getUserId().equals(username)) {
            Reminder r = optReminder.get();
            if ("Active".equalsIgnoreCase(r.getStatus())) {
                r.setStatus("Disabled");
            } else {
                r.setStatus("Active");
            }
            reminderRepository.save(r);
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/dashboard/log-mood")
    public String logMood(
            @RequestParam("mood") String mood,
            @RequestParam(value = "notes", required = false) String notes,
            HttpSession session) {
        String username = (String) session.getAttribute("user");
        if (username == null) {
            return "redirect:/login";
        }
        if (mood != null && !mood.trim().isEmpty()) {
            LocalDate today = LocalDate.now();
            List<MoodLog> existing = moodLogRepository.findByUserIdAndDate(username, today);
            moodLogRepository.deleteAll(existing);

            MoodLog moodLog = new MoodLog(username, today, mood, notes == null ? "" : notes);
            moodLogRepository.save(moodLog);

            // Force regeneration of notifications due to mood update
            LocalDateTime startOfDay = today.atStartOfDay();
            List<NotificationHistory> todayNotifs = notificationHistoryRepository.findByUserIdAndDateAfterOrderByDateDesc(username, startOfDay);
            notificationHistoryRepository.deleteAll(todayNotifs);
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/dashboard/update-preferences")
    public String updatePreferences(
            @RequestParam("reminderFrequency") String reminderFrequency,
            @RequestParam("smartNotifIntensity") String smartNotifIntensity,
            @RequestParam("quietHoursStart") String quietHoursStart,
            @RequestParam("quietHoursEnd") String quietHoursEnd,
            @RequestParam(value = "soundEnabled", required = false, defaultValue = "false") boolean soundEnabled,
            @RequestParam(value = "vibrationEnabled", required = false, defaultValue = "false") boolean vibrationEnabled,
            @RequestParam(value = "moodSupportEnabled", required = false, defaultValue = "false") boolean moodSupportEnabled,
            @RequestParam(value = "wellnessSuggestionsEnabled", required = false, defaultValue = "false") boolean wellnessSuggestionsEnabled,
            HttpSession session) {
        String username = (String) session.getAttribute("user");
        if (username == null) {
            return "redirect:/login";
        }

        UserPreference prefs = userPreferenceRepository.findById(username)
                .orElse(new UserPreference(username));
        prefs.setReminderFrequency(reminderFrequency);
        prefs.setSmartNotifIntensity(smartNotifIntensity);
        prefs.setQuietHoursStart(quietHoursStart);
        prefs.setQuietHoursEnd(quietHoursEnd);
        prefs.setSoundEnabled(soundEnabled);
        prefs.setVibrationEnabled(vibrationEnabled);
        prefs.setMoodSupportEnabled(moodSupportEnabled);
        prefs.setWellnessSuggestionsEnabled(wellnessSuggestionsEnabled);

        userPreferenceRepository.save(prefs);

        // Force regeneration of notifications due to settings change
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        List<NotificationHistory> todayNotifs = notificationHistoryRepository.findByUserIdAndDateAfterOrderByDateDesc(username, startOfDay);
        notificationHistoryRepository.deleteAll(todayNotifs);

        return "redirect:/dashboard";
    }

    private String calculateCyclePhase(LocalDate today, LocalDate latestPeriod, LocalDate nextPeriod, LocalDate ovulationDate) {
        if (latestPeriod == null || nextPeriod == null) {
            return "Follicular";
        }
        if (!today.isBefore(latestPeriod) && today.isBefore(latestPeriod.plusDays(5))) {
            return "Period";
        }
        if (!today.isBefore(nextPeriod.minusDays(7)) && today.isBefore(nextPeriod)) {
            return "PMS";
        }
        if (ovulationDate != null && !today.isBefore(ovulationDate.minusDays(2)) && today.isBefore(ovulationDate.plusDays(2))) {
            return "Ovulation";
        }
        return "Follicular";
    }

    @PostMapping("/dashboard/delete-reminder")
    public String deleteReminder(
            @RequestParam("id") Integer id,
            HttpSession session) {
        String username = (String) session.getAttribute("user");
        if (username == null) {
            return "redirect:/login";
        }

        Optional<Reminder> optReminder = reminderRepository.findById(id);
        if (optReminder.isPresent() && optReminder.get().getUserId().equals(username)) {
            reminderRepository.delete(optReminder.get());
        }
        return "redirect:/dashboard";
    }
}
