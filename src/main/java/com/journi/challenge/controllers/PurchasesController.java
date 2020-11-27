package com.journi.challenge.controllers;

import com.journi.challenge.CurrencyConverter;
import com.journi.challenge.models.Purchase;
import com.journi.challenge.models.PurchaseRequest;
import com.journi.challenge.models.PurchaseStats;
import com.journi.challenge.repositories.PurchasesRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@RestController
public class PurchasesController {

    @Inject
    private PurchasesRepository purchasesRepository;

    @Inject
    private CurrencyConverter currencyConverter;

    @GetMapping("/purchases/statistics")
    public PurchaseStats getStats() {
        return purchasesRepository.getLast30DaysStats();
    }

    @PostMapping("/purchases")
    public Purchase save(@RequestBody PurchaseRequest purchaseRequest) {
        Purchase newPurchase = new Purchase(
                purchaseRequest.getInvoiceNumber(),
                OffsetDateTime.parse(purchaseRequest.getDateTime(), DateTimeFormatter.ISO_DATE_TIME).toInstant(),
                purchaseRequest.getProductIds(),
                purchaseRequest.getCustomerName(),
                currencyConverter.convertCurrencyToEur(purchaseRequest.getCurrencyCode(), purchaseRequest.getAmount())
        );
        purchasesRepository.save(newPurchase);
        return newPurchase;
    }
}
