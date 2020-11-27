package com.journi.challenge.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.journi.challenge.CurrencyConverter;
import com.journi.challenge.models.Purchase;
import com.journi.challenge.models.PurchaseRequest;
import com.journi.challenge.models.PurchaseStats;
import com.journi.challenge.repositories.PurchasesRepository;
import com.sun.tools.javac.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PurchasesControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private PurchasesController purchasesController;
    @Autowired
    private CurrencyConverter currencyConverter;
    @Autowired
    private PurchasesRepository purchasesRepository;
    final private ObjectMapper mapper = new ObjectMapper();

    private String getPurchaseJson(PurchaseRequest request) throws JsonProcessingException {
        return mapper.writeValueAsString(request);
    }

    private Instant parseInstant(String inp) {
        return  OffsetDateTime.parse(inp, DateTimeFormatter.ISO_DATE_TIME).toInstant();
    }

    @Test
    public void testPurchaseCurrencyCodeEUR() throws Exception {
        PurchaseRequest request = new PurchaseRequest("1", "customer 1", "2020-01-01T10:00:00+01:00", List.of("product1"), 25.34, "EUR");
        String body = getPurchaseJson(request);
        mockMvc.perform(post("/purchases")
                .contentType(MediaType.APPLICATION_JSON).content(body)
        ).andExpect(status().isOk());

        Purchase savedPurchase = purchasesRepository.list().get(purchasesRepository.list().size() - 1);
        assertEquals(request.getCustomerName(), savedPurchase.getCustomerName());
        assertEquals(request.getInvoiceNumber(), savedPurchase.getInvoiceNumber());
        assertEquals(parseInstant(request.getDateTime()), savedPurchase.getTimestamp());
        assertEquals(request.getAmount(), savedPurchase.getTotalValue());
    }

    @Test
    public void testPurchaseCurrencyCodeUSD() throws Exception {
        PurchaseRequest request = new PurchaseRequest("1", "customer 1", "2020-01-01T10:00:00+01:00", List.of("product1"), 25.34, "USD");
        String body = getPurchaseJson(request);
        mockMvc.perform(post("/purchases")
                .contentType(MediaType.APPLICATION_JSON).content(body)
        ).andExpect(status().isOk());

        Purchase savedPurchase = purchasesRepository.list().get(purchasesRepository.list().size() - 1);
        assertEquals(request.getCustomerName(), savedPurchase.getCustomerName());
        assertEquals(request.getInvoiceNumber(), savedPurchase.getInvoiceNumber());
        assertEquals(parseInstant(request.getDateTime()), savedPurchase.getTimestamp());
        assertEquals(currencyConverter.convertCurrencyToEur(request.getCurrencyCode(), request.getAmount()), savedPurchase.getTotalValue());
    }


    @Test
    public void testPurchaseStatistics() {
        Instant today = Instant.now().truncatedTo(ChronoUnit.DAYS);
        Instant firstDate = today.minus(30, ChronoUnit.DAYS);
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE.withZone(ZoneId.of("UTC"));
        // Inside window purchases
        purchasesRepository.save(new Purchase("1", firstDate.plusSeconds(3600), Collections.emptyList(), "", 10.0));
        purchasesRepository.save(new Purchase("1", firstDate.plus(1, ChronoUnit.DAYS), Collections.emptyList(), "", 10.0));
        purchasesRepository.save(new Purchase("1", firstDate.plus(2, ChronoUnit.DAYS), Collections.emptyList(), "", 16.0));
        purchasesRepository.save(new Purchase("1", firstDate.plus(3, ChronoUnit.DAYS), Collections.emptyList(), "", 10.0));
        purchasesRepository.save(new Purchase("1", firstDate.plus(4, ChronoUnit.DAYS), Collections.emptyList(), "", 13.0));
        purchasesRepository.save(new Purchase("1", firstDate.plus(5, ChronoUnit.DAYS), Collections.emptyList(), "", 10.0));
        purchasesRepository.save(new Purchase("1", firstDate.plus(6, ChronoUnit.DAYS), Collections.emptyList(), "", 11.0));
        purchasesRepository.save(new Purchase("1", firstDate.plus(7, ChronoUnit.DAYS), Collections.emptyList(), "", 10.0));
        purchasesRepository.save(new Purchase("1", firstDate.plus(8, ChronoUnit.DAYS), Collections.emptyList(), "", 9.0));
        purchasesRepository.save(new Purchase("1", firstDate.plus(9, ChronoUnit.DAYS), Collections.emptyList(), "", 10.0));

        // Outside window purchases
        purchasesRepository.save(new Purchase("1", today.minus(31, ChronoUnit.DAYS), Collections.emptyList(), "", 10.0));
        purchasesRepository.save(new Purchase("1", today.minus(31, ChronoUnit.DAYS), Collections.emptyList(), "", 10.0));
        purchasesRepository.save(new Purchase("1", today.minus(32, ChronoUnit.DAYS), Collections.emptyList(), "", 10.0));
        purchasesRepository.save(new Purchase("1", today.minus(33, ChronoUnit.DAYS), Collections.emptyList(), "", 10.0));
        purchasesRepository.save(new Purchase("1", today.minus(34, ChronoUnit.DAYS), Collections.emptyList(), "", 10.0));
        purchasesRepository.save(new Purchase("1", today.minus(35, ChronoUnit.DAYS), Collections.emptyList(), "", 10.0));

        PurchaseStats purchaseStats = purchasesController.getStats();
        assertEquals(formatter.format(firstDate), purchaseStats.getFrom());
        assertEquals(formatter.format(today), purchaseStats.getTo());
        assertEquals(10, purchaseStats.getCountPurchases());
        assertEquals(109.0, purchaseStats.getTotalAmount());
        assertEquals(10.9, purchaseStats.getAvgAmount());
        assertEquals(9.0, purchaseStats.getMinAmount());
        assertEquals(16.0, purchaseStats.getMaxAmount());
    }
}
