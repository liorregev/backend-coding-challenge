package com.journi.challenge.repositories;

import com.journi.challenge.models.Purchase;
import com.journi.challenge.models.PurchaseStats;

import javax.inject.Named;
import javax.inject.Singleton;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Named
@Singleton
public class PurchasesRepository {

    private final List<Purchase> allPurchases = new ArrayList<>();

    public List<Purchase> list() {
        return allPurchases;
    }

    public void save(Purchase purchase) {
        allPurchases.add(purchase);
    }

    public PurchaseStats getLast30DaysStats() {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE.withZone(ZoneId.of("UTC"));

        Instant start = Instant.now().truncatedTo(ChronoUnit.DAYS).minus(30, ChronoUnit.DAYS);

        List<Purchase> recentPurchases = allPurchases
                .stream()
                .filter(p -> p.getTimestamp().isAfter(start))
                .sorted(Comparator.comparing(Purchase::getTimestamp))
                .collect(Collectors.toList());

        long countPurchases = recentPurchases.size();
        double totalAmountPurchases = recentPurchases.stream().mapToDouble(Purchase::getTotalValue).sum();
        return new PurchaseStats(
                formatter.format(recentPurchases.get(0).getTimestamp()),
                formatter.format(recentPurchases.get(recentPurchases.size() - 1).getTimestamp()),
                countPurchases,
                totalAmountPurchases,
                totalAmountPurchases / countPurchases,
                recentPurchases.stream().mapToDouble(Purchase::getTotalValue).min().orElse(0.0),
                recentPurchases.stream().mapToDouble(Purchase::getTotalValue).min().orElse(0.0)
        );
    }
}
