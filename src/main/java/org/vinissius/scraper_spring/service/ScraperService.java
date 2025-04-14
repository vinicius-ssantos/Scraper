package org.vinissius.scraper_spring.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.vinissius.scraper_spring.entity.ProductEntity;
import org.vinissius.scraper_spring.repository.ProductRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ScraperService {

    private static final Logger log = LoggerFactory.getLogger(ScraperService.class);

    private final ProductRepository productRepository;
    private final SeleniumScraper seleniumScraper;

    public ScraperService(ProductRepository productRepository, SeleniumScraper seleniumScraper) {
        this.productRepository = productRepository;
        this.seleniumScraper = seleniumScraper;
    }

    /**
     * Faz o scraping da listagem, persiste no banco e retorna a lista de produtos.
     * Em caso de falha, tenta novamente (até 3 vezes).
     */
    public List<ProductEntity> scrapeAll(int maxProducts) {
        int attempt = 0;
        while (attempt < 3) {
            try {
                List<ProductEntity> results = seleniumScraper.scrapeListing(maxProducts);
                if (!results.isEmpty()) {
                    productRepository.saveAll(results);
                    log.info("Produtos salvos no banco ({} items).", results.size());
                } else {
                    log.warn("Nenhum produto extraído. Tentar fallback ou continuar?");
                }
                return results;
            } catch (Exception e) {
                log.error("Erro no scraping. Attempt = {}. Retrying...", attempt, e);
                attempt++;
                try {
                    TimeUnit.SECONDS.sleep(5L * attempt);
                } catch (InterruptedException ignored) {}
            }
        }
        return new ArrayList<>();
    }
}
