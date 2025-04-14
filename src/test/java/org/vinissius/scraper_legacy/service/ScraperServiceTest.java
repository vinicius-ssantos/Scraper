package org.vinissius.scraper_spring.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.vinissius.scraper_spring.entity.ProductEntity;
import org.vinissius.scraper_spring.repository.ProductRepository;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ScraperServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SeleniumScraper seleniumScraper;

    @InjectMocks
    private ScraperService scraperService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testScrapeAll_success() {
        // Dado (arrange)
        ProductEntity product = new ProductEntity();
        product.setTitle("Mocked Product");
        when(seleniumScraper.scrapeListing(5)).thenReturn(Collections.singletonList(product));

        // Quando (act)
        List<ProductEntity> result = scraperService.scrapeAll(5);

        // Então (assert)
        assertEquals(1, result.size());
        verify(productRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testScrapeAll_failure() {
        // Dado
        when(seleniumScraper.scrapeListing(5)).thenThrow(new RuntimeException("Erro de teste"));

        // Quando
        List<ProductEntity> result = scraperService.scrapeAll(5);

        // Então
        // Esperamos que, depois de 3 tentativas, retorne lista vazia
        assertTrue(result.isEmpty());
        // E possivelmente ver se logs foram impressos, etc.
    }
}
