package com.pinkpetal.periodtracker.services;

import com.pinkpetal.periodtracker.models.Cycle;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class TrackingService {

    public static class CycleForecast {
        private LocalDate nextPeriodDate;
        private LocalDate ovulationDate;
        private LocalDate fertileStart;
        private LocalDate fertileEnd;
        private long daysUntilNextPeriod;

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
        double sum = 0;
        for (Cycle c : cycles) {
            sum += c.getCycleLength();
        }
        return (int) Math.round(sum / cycles.size());
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
        return (max - min) > 5;
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
}
