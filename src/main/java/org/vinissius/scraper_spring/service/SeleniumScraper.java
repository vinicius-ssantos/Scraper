package org.vinissius.scraper_spring.service;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.vinissius.scraper_spring.entity.ProductEntity;
import org.vinissius.scraper_spring.util.DelayUtil;
import org.vinissius.scraper_spring.util.WebDriverManagerUtil;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SeleniumScraper {

    private static final Logger log = LoggerFactory.getLogger(SeleniumScraper.class);

    // O WebDriver principal, de escopo prototype
    private final WebDriver webDriver;

    public SeleniumScraper(WebDriver webDriver) {
        this.webDriver = webDriver;
    }

    /**
     * Faz o scraping da página de listagem de produtos e extrai as informações de cada produto.
     */
    public List<ProductEntity> scrapeListing(int maxProducts) {
        log.info("Iniciando scraping da listagem...");
        List<ProductEntity> products = new ArrayList<>();
        try {
            // Abre a listagem
            webDriver.get("https://www.amazon.com.br/s?k=Displayport+KVM+Switch+2+monitores+2");

            // Espera elementos na tela
            WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("div[data-asin].s-result-item")));

            // Coleta itens
            List<WebElement> items = webDriver.findElements(By.cssSelector("div[data-asin].s-result-item"));

            int count = 0;
            for (WebElement item : items) {
                if (count >= maxProducts) break;
                try {
                    // Extrai link
                    WebElement link = item.findElement(By.cssSelector("a.a-link-normal"));
                    String productUrl = link.getAttribute("href");
                    productUrl = cleanProductUrl(productUrl);

                    // Extrai dados do produto numa nova instância de WebDriver
                    ProductEntity product = scrapeProduct(productUrl);
                    if (product != null) {
                        products.add(product);
                        count++;
                    }

                    // Aplica um delay aleatório entre as requisições para simular comportamento humano
                    DelayUtil.sleepRandomDelay();
                } catch (Exception e) {
                    log.warn("Falha ao extrair um produto: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Erro ao executar scraping da listagem: {}", e.getMessage());
        } finally {
            // Fecha este driver (listagem)
            webDriver.quit();
        }
        return products;
    }

    /**
     * Abre uma *nova* instância do WebDriver para extrair dados de 1 produto.
     */
    private ProductEntity scrapeProduct(String url) {
        WebDriver driver = null;
        try {
            driver = WebDriverManagerUtil.createWebDriver();
            driver.get(url);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("productTitle")));

            ProductEntity product = new ProductEntity();
            product.setUrl(url);

            // Título
            product.setTitle(driver.findElement(By.id("productTitle")).getText().trim());

            // Preço (tenta o seletor "priceblock_ourprice", com fallback)
            try {
                product.setPrice(driver.findElement(By.id("priceblock_ourprice")).getText().trim());
            } catch (Exception ex) {
                product.setPrice("Indisponível");
            }

            // Extrai ASIN a partir da seção de detalhes
            try {
                WebElement detailDiv = driver.findElement(By.id("detailBullets_feature_div"));
                List<WebElement> list = detailDiv.findElements(By.tagName("li"));
                for (WebElement li : list) {
                    if (li.getText().contains("ASIN")) {
                        product.setAsin(li.getText().split(":")[1].trim());
                        break;
                    }
                }
            } catch (Exception ex) {
                product.setAsin("Indisponível");
            }

            // Timestamp da execução
            product.setExecutedAt(LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            return product;
        } catch (Exception e) {
            log.error("Erro ao extrair produto da URL {}: {}", url, e.getMessage());
            return null;
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    /**
     * Remove redirecionamentos e converte a URL para o formato padrão baseado no ASIN.
     */
    private String cleanProductUrl(String url) {
        if (url.contains("aax-us-iad.amazon.com") || url.toLowerCase().contains("redirect")) {
            Pattern pattern = Pattern.compile("/dp/([A-Z0-9]{10})");
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                String asin = matcher.group(1);
                return "https://www.amazon.com.br/dp/" + asin;
            }
        }
        return url;
    }
}
