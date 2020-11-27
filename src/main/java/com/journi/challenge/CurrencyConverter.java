package com.journi.challenge;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Named;
import javax.inject.Singleton;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

@Named
@Singleton
public class CurrencyConverter {
    private final Map<String, String> supportedCountriesCurrency;
    private final Map<String, Double> currencyEurRate;

    public CurrencyConverter() {
        supportedCountriesCurrency = new HashMap<>();
        supportedCountriesCurrency.put("AT", "EUR");
        supportedCountriesCurrency.put("DE", "EUR");
        supportedCountriesCurrency.put("HU", "HUF");
        supportedCountriesCurrency.put("GB", "GBP");
        supportedCountriesCurrency.put("FR", "EUR");
        supportedCountriesCurrency.put("PT", "EUR");
        supportedCountriesCurrency.put("IE", "EUR");
        supportedCountriesCurrency.put("ES", "EUR");
        supportedCountriesCurrency.put("BR", "BRL");
        supportedCountriesCurrency.put("US", "USD");
        supportedCountriesCurrency.put("CA", "CAD");

        currencyEurRate = new HashMap<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            URL rates = getClass().getResource("/eur_rate.json");
            JsonNode ratesTree = mapper.readTree(rates);
            Iterator<JsonNode> currenciesIterator = ratesTree.findPath("currencies").elements();
            currenciesIterator.forEachRemaining(currency -> currencyEurRate.put(currency.get("currency").asText(), currency.get("rate").asDouble()));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public Optional<String> getCurrencyForCountryCode(String countryCode) {
        if(supportedCountriesCurrency.containsKey(countryCode.toUpperCase())) {
            return Optional.of(supportedCountriesCurrency.get(countryCode.toUpperCase()));
        }
        return Optional.empty();
    }

    public Optional<Double> convertEurToCurrency(String currencyCode, Double eurValue) {
        if(currencyEurRate.containsKey(currencyCode)) {
            return Optional.of(eurValue * currencyEurRate.get(currencyCode));
        }
        return Optional.empty();
    }

    public Optional<Double> convertCurrencyToEur(String currencyCode, Double currencyValue) {
        if(currencyEurRate.containsKey(currencyCode)) {
            return Optional.of(currencyValue / currencyEurRate.get(currencyCode));
        }
        return Optional.empty();
    }
}
