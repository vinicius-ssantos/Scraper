package org.vinissius.scraper_spring.service;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.vinissius.scraper_spring.entity.ProductEntity;

import java.util.ArrayList;
import java.util.List;

@Component
public class SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(SeleniumScraper.class);

    private final WebDriver driver;

    /**
     * Se a classe estiver em scope padrão (singleton), mas o bean do WebDriver
     * for prototype, cada injeção ainda vai gerar 1 driver novo. Entretanto,
     * há alguns detalhes de proxy do Spring. Em geral, funciona para uso simples.
     */
    @Autowired
    public SeleniumScraper(WebDriver driver) {
        this.driver = driver;
    }

    public List<ProductEntity> scrapeListing(int maxProducts) {
        log.info("Iniciando scraping da listagem...");

        // Exemplo de scraping simplificado:
        try {
            // 1) Acessa a página de listagem
            driver.get("https://www.amazon.com.br/s?k=Displayport+KVM+Switch+2+monitores+2");

            // 2) Espera e extrai as URLs (código real depende do seu HTML)
            // ...
            // 3) Para cada URL, extrair info e popular ProductEntity
            // ...
            List<ProductEntity> products = new ArrayList<>();

            // Aqui, retorne os items efetivamente extraídos
            // Ex.: products.add(...)

            return products;
        } finally {
            driver.quit(); // ao final, fechamos o driver
        }
    }
}
