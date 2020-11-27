package com.journi.challenge.repositories;

import com.journi.challenge.models.Purchase;
import com.journi.challenge.models.PurchaseStats;

import javax.inject.Named;
import javax.inject.Singleton;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

class DayStatistics {
    private double min;
    private double max;
    private double total;
    private long count;

    DayStatistics() {
        this.min = Double.POSITIVE_INFINITY;
        this.max = Double.NEGATIVE_INFINITY;
        this.total = 0.0;
        this.count = 0;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}

@Named
@Singleton
public class PurchasesRepository {

    public static final int DAYS_FOR_STATS = 30;

    private final Map<Instant, DayStatistics> perDayStats = new HashMap<>();
    private final List<Purchase> allPurchases = new ArrayList<>();

    public List<Purchase> list() {
        return allPurchases;
    }

    public void save(Purchase purchase) {
        final Instant purchaseDay = purchase.getTimestamp().truncatedTo(ChronoUnit.DAYS);
        DayStatistics dayStats = perDayStats.getOrDefault(purchaseDay, new DayStatistics());
        dayStats.setCount(dayStats.getCount() + 1);
        dayStats.setMax(Math.max(dayStats.getMax(), purchase.getTotalValue()));
        dayStats.setMin(Math.min(dayStats.getMin(), purchase.getTotalValue()));
        dayStats.setTotal(dayStats.getTotal() + purchase.getTotalValue());
        perDayStats.put(purchaseDay, dayStats);
        allPurchases.add(purchase);
    }

    public PurchaseStats getLast30DaysStats() {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE.withZone(ZoneId.of("UTC"));

        Instant end = Instant.now().truncatedTo(ChronoUnit.DAYS);
        Instant start = end.minus(DAYS_FOR_STATS, ChronoUnit.DAYS);
        long countPurchases = 0;
        double totalAmountPurchases = 0;
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;

        for(Instant day = start; day.isBefore(end); day = day.plus(1, ChronoUnit.DAYS)) {
            DayStatistics dayStats = perDayStats.getOrDefault(day, new DayStatistics());
            countPurchases += dayStats.getCount();
            totalAmountPurchases += dayStats.getTotal();
            min = Math.min(min, dayStats.getMin());
            max = Math.max(max, dayStats.getMax());
        }

        return new PurchaseStats(
                formatter.format(start),
                formatter.format(end),
                countPurchases,
                totalAmountPurchases,
                totalAmountPurchases / countPurchases,
                min,
                max
        );
    }
}
