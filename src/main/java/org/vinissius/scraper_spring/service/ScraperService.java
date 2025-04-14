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

    /**
     * Faz o scraping da listagem, persiste no banco e retorna a lista de produtos.
     * Em caso de falha, tenta novamente (até 3 vezes) com backoff exponencial simples.
     */
    public List<ProductEntity> scrapeAll(int maxProducts) {
        int attempt = 0;
        while (attempt < 3) {
            try {
                // 1) Tenta extrair
                List<ProductEntity> results = seleniumScraper.scrapeListing(maxProducts);

                // 2) Se extraiu, salva no banco
                if (!results.isEmpty()) {
                    productRepository.saveAll(results);
                    log.info("Produtos salvos no banco ({} items).", results.size());

                    // 3) Gera o arquivo JSON de saída
                    saveAsJson(results);
                } else {
                    log.warn("Nenhum produto extraído. Tentar fallback ou seguir?");
                }

                return results;
            } catch (Exception e) {
                attempt++;
                log.error("Erro no scraping. Attempt = {}. Retrying...", attempt, e);

                // Backoff exponencial simples: 5 * (2^(attempt-1))
                long delay = (long) (5 * Math.pow(2, attempt - 1));
                log.info("Aguardando {} segundos antes de nova tentativa...", delay);
                try {
                    TimeUnit.SECONDS.sleep(delay);
                } catch (InterruptedException ignored) {}
            }
        }
        return new ArrayList<>();
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
