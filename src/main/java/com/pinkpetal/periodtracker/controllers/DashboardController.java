package com.pinkpetal.periodtracker.controllers;

import com.pinkpetal.periodtracker.models.Cycle;
import com.pinkpetal.periodtracker.models.Reminder;
import com.pinkpetal.periodtracker.models.Symptom;
import com.pinkpetal.periodtracker.models.User;
import com.pinkpetal.periodtracker.repositories.CycleRepository;
import com.pinkpetal.periodtracker.repositories.ReminderRepository;
import com.pinkpetal.periodtracker.repositories.SymptomRepository;
import com.pinkpetal.periodtracker.repositories.UserRepository;
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
            HttpSession session) {
        String username = (String) session.getAttribute("user");
        if (username == null) {
            return "redirect:/login";
        }

        if (type != null && !type.isEmpty() && time != null && !time.isEmpty()) {
            Reminder reminder = new Reminder(username, type, time, "Active");
            reminderRepository.save(reminder);
        }
        return "redirect:/dashboard";
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
