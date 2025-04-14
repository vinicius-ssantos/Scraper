package org.vinissius.scraper_legacy.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.vinissius.scraper_spring.entity.ProductEntity;
import org.vinissius.scraper_spring.repository.ProductRepository;
import org.vinissius.scraper_spring.service.ScraperService;
import org.vinissius.scraper_spring.service.SeleniumScraper;

import java.util.Collections;
import java.util.List;

class ScraperServiceTest {

    @Test
    void testScrapeAll() {
        // mock do repository e do seleniumScraper
        ProductRepository mockRepo = Mockito.mock(ProductRepository.class);
        SeleniumScraper mockSelenium = Mockito.mock(SeleniumScraper.class);

        // simula que o scraper retorna 1 item
        ProductEntity fakeProduct = new ProductEntity();
        fakeProduct.setTitle("Fake Title");
        Mockito.when(mockSelenium.scrapeListing(5)).thenReturn(Collections.singletonList(fakeProduct));

        ScraperService service = new ScraperService(mockRepo, mockSelenium);
        List<ProductEntity> result = service.scrapeAll(5);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("Fake Title", result.get(0).getTitle());
        // verifica se salvou no repo
        Mockito.verify(mockRepo).saveAll(Mockito.anyList());
    }
}
