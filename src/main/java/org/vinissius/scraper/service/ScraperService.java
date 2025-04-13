package org.vinissius.scraper.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.vinissius.scraper.entity.ProductEntity;
import org.vinissius.scraper.repository.ProductRepository;

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
     * Faz o scraping da listagem, retorna e persiste no banco.
     * Se falhar, tenta um backoff simples.
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
        // Se chegou aqui, falhou definitivamente
        return new ArrayList<>();
    }
}
