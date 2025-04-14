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

    // O WebDriver injetado é de escopo "prototype" (definido em WebDriverConfig)
    private final WebDriver webDriver;

    public SeleniumScraper(WebDriver webDriver) {
        this.webDriver = webDriver;
    }

    /**
     * Faz o scraping da página de listagem de produtos e extrai as informações de cada produto.
     *
     * @param maxProducts número máximo de produtos a extrair
     * @return lista de ProductEntity
     */
    public List<ProductEntity> scrapeListing(int maxProducts) {
        log.info("Iniciando scraping da listagem...");
        List<ProductEntity> products = new ArrayList<>();
        try {
            webDriver.get("https://www.amazon.com.br/s?k=Displayport+KVM+Switch+2+monitores+2");
            WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("div[data-asin].s-result-item")));
            List<WebElement> items = webDriver.findElements(By.cssSelector("div[data-asin].s-result-item"));

            int count = 0;
            for (WebElement item : items) {
                if (count >= maxProducts) break;
                try {
                    // Tenta pegar o link do produto usando o seletor principal
                    WebElement link = null;
                    try {
                        link = item.findElement(By.cssSelector("a.a-link-normal"));
                    } catch (Exception e) {
                        log.warn("Seletor primário não retornou elementos, tentando seletor alternativo. Erro: {}", e.getMessage());
                        // Se desejar, adicione um seletor alternativo aqui, por exemplo:
                        link = item.findElement(By.cssSelector("a.s-link-style"));
                    }
                    String productUrl = link.getAttribute("href");
                    productUrl = cleanProductUrl(productUrl);
                    // Extrai os dados do produto com método separado
                    ProductEntity product = scrapeProduct(productUrl);
                    if (product != null) {
                        products.add(product);
                        count++;
                    }
                } catch (Exception e) {
                    log.warn("Falha ao extrair um produto da listagem: {}", e);
                }
            }
        } catch (Exception e) {
            log.error("Erro ao executar scraping da listagem: {}", e);
        } finally {
            webDriver.quit();
        }
        return products;
    }

    /**
     * Cria uma nova instância do WebDriver para acessar a página do produto e extrair os dados.
     *
     * @param url URL do produto
     * @return ProductEntity com os dados extraídos ou null, se ocorrer erro
     */
    private ProductEntity scrapeProduct(String url) {
        WebDriver driver = null;
        try {
            driver = WebDriverManagerUtil.getDriver();
            driver.get(url);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("productTitle")));

            ProductEntity product = new ProductEntity();
            product.setUrl(url);
            product.setTitle(driver.findElement(By.id("productTitle")).getText().trim());
            try {
                product.setPrice(driver.findElement(By.id("priceblock_ourprice")).getText().trim());
            } catch (Exception ex) {
                log.warn("Preço não encontrado para URL {}: {}", url, ex.toString());
                product.setPrice("Indisponível");
            }
            // Tenta extrair o ASIN do elemento de detalhes
            try {
                WebElement detailDiv = driver.findElement(By.id("detailBullets_feature_div"));
                List<WebElement> list = detailDiv.findElements(By.tagName("li"));
                for (WebElement li : list) {
                    if (li.getText().contains("ASIN")) {
                        String[] parts = li.getText().split(":");
                        if (parts.length > 1) {
                            product.setAsin(parts[1].trim());
                            break;
                        }
                    }
                }
            } catch (Exception ex) {
                log.warn("ASIN não extraído para URL {}: {}", url, ex.toString());
                product.setAsin("Indisponível");
            }
            product.setExecutedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            return product;
        } catch (Exception e) {
            log.error("Erro ao extrair produto da URL {}: {}", url, e);
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
