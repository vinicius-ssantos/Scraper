package org.vinissius.scraper_spring.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.vinissius.scraper_spring.entity.ProductEntity;
import org.vinissius.scraper_spring.repository.ProductRepository;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ScraperService {

    private static final Logger log = LoggerFactory.getLogger(ScraperService.class);

    private final ProductRepository productRepository;
    private final SeleniumScraper seleniumScraper;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public ScraperService(ProductRepository productRepository, SeleniumScraper seleniumScraper) {
        this.productRepository = productRepository;
        this.seleniumScraper = seleniumScraper;
    }

    public List<ProductEntity> scrapeAll(int max) {
        log.info("Iniciando scraping de até {} produtos...", max);

        List<ProductEntity> results = seleniumScraper.scrapeAll(max);

        log.info("Total de produtos extraídos: {}", results.size());

        productRepository.saveAll(results);
        saveAsJson(results);

        return results;
    }



    /**
     * Gera o arquivo "products_info_{timestamp}.json" na pasta 'outputs'.
     */
    private void saveAsJson(List<ProductEntity> results) {
        // Marca de tempo
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        // Cria pasta outputs se não existir
        java.io.File outDir = new java.io.File("outputs");
        if (!outDir.exists()) {
            outDir.mkdirs();
        }
        String filename = String.format("outputs/products_info_%s.json", timestamp);

        try (FileWriter writer = new FileWriter(filename)) {
            gson.toJson(results, writer);
            log.info("Arquivo JSON gerado em: {}", filename);
        } catch (IOException e) {
            log.error("Falha ao salvar JSON: {}", e.getMessage());
        }
    }
}
