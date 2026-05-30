package com.pinkpetal.periodtracker.services;

import com.pinkpetal.periodtracker.models.Cycle;
import com.pinkpetal.periodtracker.models.User;
import com.pinkpetal.periodtracker.repositories.CycleRepository;
import com.pinkpetal.periodtracker.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TrackingService {

    @Autowired
    private CycleRepository cycleRepository;

    @Autowired
    private UserRepository userRepository;

    public static class CycleForecast {
        private LocalDate nextPeriodDate;
        private LocalDate ovulationDate;
        private LocalDate fertileStart;
        private LocalDate fertileEnd;
        private long daysUntilNextPeriod;
        private int confidenceScore;

        public LocalDate getNextPeriodDate() {
            return nextPeriodDate;
        }

        public void setNextPeriodDate(LocalDate nextPeriodDate) {
            this.nextPeriodDate = nextPeriodDate;
        }

        public LocalDate getRunningOvulationDate() {
            return ovulationDate;
        }

        public LocalDate getOvulationDate() {
            return ovulationDate;
        }

        public void setOvulationDate(LocalDate ovulationDate) {
            this.ovulationDate = ovulationDate;
        }

        public LocalDate getFertileStart() {
            return fertileStart;
        }

        public void setFertileStart(LocalDate fertileStart) {
            this.fertileStart = fertileStart;
        }

        public LocalDate getFertileEnd() {
            return fertileEnd;
        }

        public void setFertileEnd(LocalDate fertileEnd) {
            this.fertileEnd = fertileEnd;
        }

        public long getDaysUntilNextPeriod() {
            return daysUntilNextPeriod;
        }

        public void setDaysUntilNextPeriod(long daysUntilNextPeriod) {
            this.daysUntilNextPeriod = daysUntilNextPeriod;
        }

        public int getConfidenceScore() {
            return confidenceScore;
        }

        public void setConfidenceScore(int confidenceScore) {
            this.confidenceScore = confidenceScore;
        }
    }

    public CycleForecast calculateForecast(LocalDate lastPeriodDate, Integer cycleLength) {
        if (lastPeriodDate == null || cycleLength == null || cycleLength <= 0) {
            return null;
        }
        CycleForecast forecast = new CycleForecast();
        
        LocalDate nextPeriod = lastPeriodDate.plusDays(cycleLength);
        forecast.setNextPeriodDate(nextPeriod);

        LocalDate ovulation = nextPeriod.minusDays(14);
        forecast.setOvulationDate(ovulation);

        forecast.setFertileStart(ovulation.minusDays(5));
        forecast.setFertileEnd(ovulation.plusDays(1));

        long daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), nextPeriod);
        forecast.setDaysUntilNextPeriod(daysUntil);

        return forecast;
    }

    public List<LocalDate> getHistoryPredictions(LocalDate lastPeriodDate, Integer cycleLength, int count) {
        List<LocalDate> history = new ArrayList<>();
        if (lastPeriodDate == null || cycleLength == null || cycleLength <= 0) {
            return history;
        }
        LocalDate current = lastPeriodDate;
        for (int i = 0; i < count; i++) {
            history.add(current);
            current = current.plusDays(cycleLength);
        }
        return history;
    }

    public int calculateAverageCycleLength(List<Cycle> cycles, int defaultLength) {
        if (cycles == null || cycles.isEmpty()) {
            return defaultLength;
        }
        // Sort by start date desc to make sure we process latest first
        List<Cycle> sorted = new ArrayList<>(cycles);
        sorted.sort((c1, c2) -> c2.getPeriodStartDate().compareTo(c1.getPeriodStartDate()));

        if (sorted.size() >= 3) {
            double wVal = (0.5 * sorted.get(0).getCycleLength()) 
                        + (0.3 * sorted.get(1).getCycleLength()) 
                        + (0.2 * sorted.get(2).getCycleLength());
            return (int) Math.round(wVal);
        } else {
            // fewer than 3 cycles exist: use available cycles, fallback to average cycle length
            double sum = 0;
            for (Cycle c : sorted) {
                sum += c.getCycleLength();
            }
            return (int) Math.round(sum / sorted.size());
        }
    }

    public boolean detectIrregularity(List<Cycle> cycles) {
        if (cycles == null || cycles.size() < 2) {
            return false;
        }
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (Cycle c : cycles) {
            int len = c.getCycleLength();
            if (len < min) min = len;
            if (len > max) max = len;
        }
        int diff = max - min;
        return diff > 7; // More than 7 days = Irregular
    }

    public int calculateConfidenceScore(List<Cycle> cycles) {
        if (cycles == null || cycles.isEmpty()) {
            return 0;
        }
        int baseConfidence = 50;
        int size = cycles.size();
        if (size >= 1 && size <= 2) {
            baseConfidence = 50;
        } else if (size >= 3 && size <= 5) {
            baseConfidence = 70;
        } else if (size >= 6) {
            baseConfidence = 90;
        }

        // Reduce confidence if cycle variation is high
        if (size >= 2) {
            int min = Integer.MAX_VALUE;
            int max = Integer.MIN_VALUE;
            for (Cycle c : cycles) {
                int len = c.getCycleLength();
                if (len < min) min = len;
                if (len > max) max = len;
            }
            int diff = max - min;
            if (diff >= 4 && diff <= 7) {
                baseConfidence -= 10;
            } else if (diff > 7) {
                baseConfidence -= 20;
            }
        }
        return Math.max(0, Math.min(100, baseConfidence));
    }

    public LocalDate getLatestPeriodDate(List<Cycle> cycles, LocalDate defaultDate) {
        if (cycles == null || cycles.isEmpty()) {
            return defaultDate;
        }
        LocalDate latest = cycles.get(0).getPeriodStartDate();
        for (Cycle c : cycles) {
            if (c.getPeriodStartDate().isAfter(latest)) {
                latest = c.getPeriodStartDate();
            }
        }
        return latest;
    }

    public void recalculateAndStoreCycleLengths(String userId) {
        List<Cycle> cycles = cycleRepository.findByUserIdOrderByPeriodStartDateAsc(userId);
        if (cycles == null || cycles.isEmpty()) {
            Optional<User> optUser = userRepository.findById(userId);
            if (optUser.isPresent()) {
                User user = optUser.get();
                user.setLastPeriodDate(null);
                user.setCycleLength(28);
                userRepository.save(user);
            }
            return;
        }

        // Recalculate cycle lengths based on date differences
        for (int i = 1; i < cycles.size(); i++) {
            Cycle prev = cycles.get(i - 1);
            Cycle curr = cycles.get(i);
            long days = ChronoUnit.DAYS.between(prev.getPeriodStartDate(), curr.getPeriodStartDate());
            curr.setCycleLength((int) days);
            cycleRepository.save(curr);
        }

        // Sort descending to get latest dates
        List<Cycle> descCycles = cycleRepository.findByUserIdOrderByPeriodStartDateDesc(userId);
        int predictedLength = calculateAverageCycleLength(descCycles, 28);
        LocalDate latestPeriodDate = getLatestPeriodDate(descCycles, null);

        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isPresent()) {
            User user = optUser.get();
            user.setLastPeriodDate(latestPeriodDate);
            user.setCycleLength(predictedLength);
            userRepository.save(user);
        }
    }
}
