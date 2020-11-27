package com.journi.challenge;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyConverterTest {

    private CurrencyConverter currencyConverter = new CurrencyConverter();

    @Test
    void findCurrencyCodeForSupportedCountry() {
        assertEquals(Optional.of("EUR"), currencyConverter.getCurrencyForCountryCode("AT"));
        assertEquals(Optional.of("EUR"), currencyConverter.getCurrencyForCountryCode("DE"));
        assertEquals(Optional.of("EUR"), currencyConverter.getCurrencyForCountryCode("FR"));
        assertEquals(Optional.of("BRL"), currencyConverter.getCurrencyForCountryCode("BR"));
        assertEquals(Optional.of("GBP"), currencyConverter.getCurrencyForCountryCode("GB"));
    }

    @Test
    void findCurrencyCodeForNonSupportedCountry() {
        assertEquals(Optional.empty(), currencyConverter.getCurrencyForCountryCode("CH"));
        assertEquals(Optional.empty(), currencyConverter.getCurrencyForCountryCode("CL"));
        assertEquals(Optional.empty(), currencyConverter.getCurrencyForCountryCode("AR"));
        assertEquals(Optional.empty(), currencyConverter.getCurrencyForCountryCode("FI"));
    }

    @Test
    void convertEurValueToSupportedCurrency() {
        assertEquals(Optional.of(25.0), currencyConverter.convertEurToCurrency("EUR", 25.0));
        assertEquals(Optional.of(25.0 * 5.1480), currencyConverter.convertEurToCurrency("BRL", 25.0));
    }

    @Test
    void convertSupportedCurrencyToEurValue() {
        assertEquals(Optional.of(25.0), currencyConverter.convertCurrencyToEur("EUR", 25.0));
        assertEquals(Optional.of(25.0 / 5.1480), currencyConverter.convertCurrencyToEur("BRL", 25.0));
    }
}