package org.vinissius.scraper.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openqa.selenium.WebDriver;
import org.vinissius.scraper.driver.SeleniumDriverManager;
import org.vinissius.scraper.db.DatabaseManager;
import org.vinissius.scraper.model.Product;
import org.vinissius.scraper.service.Scraper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class AmazonScraper {

    private static final Logger log = LoggerFactory.getLogger(AmazonScraper.class);

    private static final String LISTING_URL = "https://www.amazon.com.br/s?k=Displayport+KVM+Switch+2+monitores+2";
    private static final int MAX_PRODUCTS = 10;
    private static final int THREAD_POOL_SIZE = 4;

    public static void main(String[] args) {
        log.info("=== Iniciando aplicação de scraping ===");

        // 1) Inicializa o banco de dados
        DatabaseManager dbManager = new DatabaseManager("products_info.db");
        dbManager.initDatabase();
        log.info("Banco de dados 'products_info.db' foi inicializado ou validado.");

        // 2) Cria o Scraper e obtém as URLs da listagem
        Scraper scraper = new Scraper();
        WebDriver driver = SeleniumDriverManager.getDriver();

        log.info("Buscando página de listagem: {}", LISTING_URL);
        List<String> productUrls = scraper.getProductUrls(driver, LISTING_URL, MAX_PRODUCTS);
        driver.quit();

        log.info("{} URLs de produtos encontrados.", productUrls.size());

        // 3) Faz scraping em paralelo das URLs coletadas
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        List<Future<Product>> futures = new ArrayList<>();
        for (String url : productUrls) {
            futures.add(executor.submit(() -> {
                WebDriver prodDriver = SeleniumDriverManager.getDriver();
                Product product = scraper.getProductInfo(prodDriver, url);
                prodDriver.quit();
                return product;
            }));
        }
        executor.shutdown();

        List<Product> products = new ArrayList<>();
        for (Future<Product> future : futures) {
            try {
                Product p = future.get();
                if (p != null) {
                    products.add(p);
                } else {
                    log.warn("Produto não foi extraído (null retornado).");
                }
            } catch (Exception e) {
                log.error("Erro ao processar produto em thread pool.", e);
            }
        }

        log.info("Total de produtos extraídos com sucesso: {}", products.size());

        // 4) Salva no banco de dados
        dbManager.saveProducts(products);
        log.info("Produtos salvos no banco de dados com sucesso.");

        // 5) Exporta para JSON
        try {
            Files.createDirectories(Paths.get("outputs"));
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String jsonFilename = "outputs/products_info_" + timestamp + ".json";

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonOutput = gson.toJson(products);

            Path outputFile = Paths.get(jsonFilename);
            Files.write(outputFile, jsonOutput.getBytes(StandardCharsets.UTF_8));

            log.info("Dados exportados para JSON: {}", jsonFilename);
        } catch (Exception e) {
            log.error("Falha ao salvar JSON.", e);
        }

        log.info("=== Extração concluída. ===");
    }
}
