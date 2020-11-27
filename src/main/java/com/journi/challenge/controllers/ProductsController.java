package com.journi.challenge.controllers;

import com.journi.challenge.CurrencyConverter;
import com.journi.challenge.models.Product;
import com.journi.challenge.models.ProductListItem;
import com.journi.challenge.repositories.ProductsRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ProductsController {

    @Inject
    private ProductsRepository productsRepository;

    @Inject
    private CurrencyConverter currencyConverter;

    @GetMapping("/products")
    public List<ProductListItem> list(@RequestParam(name = "countryCode", defaultValue = "AT") String countryCode) {
        final String currencyCode = currencyConverter.getCurrencyForCountryCode(countryCode).orElse("EUR");
        return productsRepository.list()
                .stream()
                .map((Product product) -> new ProductListItem(
                        product.getId(),
                        product.getDescription(),
                        currencyConverter.convertEurToCurrency(currencyCode, product.getPrice()).orElseGet(product::getPrice),
                        currencyCode))
                .collect(Collectors.toList());
    }
}
