package com.journi.challenge.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.journi.challenge.CurrencyConverter;
import com.journi.challenge.models.Purchase;
import com.journi.challenge.models.PurchaseRequest;
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
import java.time.format.DateTimeFormatter;

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


//    @Test
//    public void testPurchaseStatistics() {
//        LocalDateTime now = LocalDateTime.now();
//        LocalDateTime firstDate = now.minusDays(20);
//        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE.withZone(ZoneId.of("UTC"));
//        // Inside window purchases
//        purchasesRepository.save(new Purchase("1", firstDate, Collections.emptyList(), "", 10.0));
//        purchasesRepository.save(new Purchase("1", firstDate.plusDays(1), Collections.emptyList(), "", 10.0));
//        purchasesRepository.save(new Purchase("1", firstDate.plusDays(2), Collections.emptyList(), "", 10.0));
//        purchasesRepository.save(new Purchase("1", firstDate.plusDays(3), Collections.emptyList(), "", 10.0));
//        purchasesRepository.save(new Purchase("1", firstDate.plusDays(4), Collections.emptyList(), "", 10.0));
//        purchasesRepository.save(new Purchase("1", firstDate.plusDays(5), Collections.emptyList(), "", 10.0));
//        purchasesRepository.save(new Purchase("1", firstDate.plusDays(6), Collections.emptyList(), "", 10.0));
//        purchasesRepository.save(new Purchase("1", firstDate.plusDays(7), Collections.emptyList(), "", 10.0));
//        purchasesRepository.save(new Purchase("1", firstDate.plusDays(8), Collections.emptyList(), "", 10.0));
//        purchasesRepository.save(new Purchase("1", firstDate.plusDays(9), Collections.emptyList(), "", 10.0));
//
//        // Outside window purchases
//        purchasesRepository.save(new Purchase("1", now.minusDays(31), Collections.emptyList(), "", 10.0));
//        purchasesRepository.save(new Purchase("1", now.minusDays(31), Collections.emptyList(), "", 10.0));
//        purchasesRepository.save(new Purchase("1", now.minusDays(32), Collections.emptyList(), "", 10.0));
//        purchasesRepository.save(new Purchase("1", now.minusDays(33), Collections.emptyList(), "", 10.0));
//        purchasesRepository.save(new Purchase("1", now.minusDays(34), Collections.emptyList(), "", 10.0));
//        purchasesRepository.save(new Purchase("1", now.minusDays(35), Collections.emptyList(), "", 10.0));
//
//        PurchaseStats purchaseStats = purchasesController.getStats();
//        assertEquals(formatter.format(firstDate), purchaseStats.getFrom());
//        assertEquals(formatter.format(firstDate.plusDays(9)), purchaseStats.getTo());
//        assertEquals(10, purchaseStats.getCountPurchases());
//        assertEquals(100.0, purchaseStats.getTotalAmount());
//        assertEquals(10.0, purchaseStats.getAvgAmount());
//        assertEquals(10.0, purchaseStats.getMinAmount());
//        assertEquals(10.0, purchaseStats.getMaxAmount());
//    }
}
