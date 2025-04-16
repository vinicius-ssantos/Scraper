package org.vinissius.scraper_spring.service;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.vinissius.scraper_spring.entity.ProductEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Serviço responsável por executar o scraping via Selenium WebDriver.
 */
@Service
public class SeleniumScraper {
    private static final Logger log = LoggerFactory.getLogger(SeleniumScraper.class);
    private final WebDriver driver;
    private final String amazonUrl;
    private final WebDriverWait wait;
    /**
     * Injeção de dependências via construtor:
     * @param driver    WebDriver (Bean definido em SeleniumConfig)
     * @param amazonUrl URL obtida do application.properties
     */
    public SeleniumScraper(WebDriver driver, @Value("${scraper.amazon.url}") String amazonUrl
    ) {

        this.driver = driver;
        this.amazonUrl = amazonUrl;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10)); // Timeout de 10 segundos

    }

    public List<ProductEntity> scrapeAll(int maxProducts) {
        driver.get(amazonUrl);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.s-main-slot")));

        List<WebElement> productElements = driver.findElements(By.cssSelector("div.s-main-slot > div[data-asin]:not([data-asin=''])"));

        ExecutorService executor = Executors.newFixedThreadPool(Math.min(4, maxProducts));
        List<Future<ProductEntity>> futures = new ArrayList<>();

        for (int i = 0; i < Math.min(maxProducts, productElements.size()); i++) {
            WebElement element = productElements.get(i);
            String asin = element.getAttribute("data-asin");
            String title = "";
            try {
                title = element.findElement(By.cssSelector("h2 span")).getText();
            } catch (Exception ignored) {}

            String finalTitle = title;
            futures.add(executor.submit(() -> scrapeProductByAsin(asin, finalTitle)));
        }

        executor.shutdown();


        List<ProductEntity> results = new ArrayList<>();

        for (Future<ProductEntity> f : futures) {
            try {
                ProductEntity product = f.get(30, TimeUnit.SECONDS);
                if (product != null) {
                    results.add(product);
                }
            } catch (Exception e) {
                log.warn("Erro ao processar produto: {}", e.getMessage());
            }
        }

        return results;
    }

    private ProductEntity scrapeProductByAsin(String asin, String title) {
        long start = System.currentTimeMillis();
        WebDriver localDriver = createIsolatedDriver();
        WebDriverWait wait = new WebDriverWait(localDriver, Duration.ofSeconds(10));

        try {
            String url = "https://www.amazon.com.br/dp/" + asin;
            localDriver.get(url);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ppd")));

            String price = tryGetText(localDriver, By.id("priceblock_ourprice"));
            if (price.isEmpty()) {
                price = tryGetText(localDriver, By.id("priceblock_dealprice"));
            }

            String rating = tryGetText(localDriver, By.cssSelector("span.a-icon-alt"));
            String reviewCount = tryGetText(localDriver, By.id("acrCustomerReviewText"));

            List<String> bulletPoints = new ArrayList<>();
            try {
                List<WebElement> bullets = localDriver.findElements(By.cssSelector("#feature-bullets ul li span"));
                bulletPoints = bullets.stream().map(WebElement::getText).collect(Collectors.toList());
            } catch (Exception ignored) {}

            String description = tryGetText(localDriver, By.id("productDescription"));

            List<String> images = new ArrayList<>();
            try {
                List<WebElement> imgs = localDriver.findElements(By.cssSelector("img.a-dynamic-image"));
                images = imgs.stream().map(img -> img.getAttribute("src")).distinct().collect(Collectors.toList());
            } catch (Exception ignored) {}

            String seller = tryGetText(localDriver, By.id("sellerProfileTriggerId"));
            if (seller.isEmpty()) {
                seller = tryGetText(localDriver, By.id("bylineInfo"));
            }

            ProductEntity product = new ProductEntity();
            product.setAsin(asin);
            product.setTitle(title);
            product.setPrice(price);
            product.setRating(rating);
            product.setReviewCount(reviewCount);
            product.setBulletPoints(bulletPoints.toString());
            product.setProductDescription(description);
            product.setImages(images.toString());
            product.setSellerInfo(seller);
            product.setExecutedAt(LocalDateTime.now().toString());
            product.setUrl(url);

            long end = System.currentTimeMillis();
            log.info("Produto [{}] extraído em {} ms", asin, (end - start));
            return product;
        } catch (Exception e) {
            log.error("Erro ao extrair produto [{}]: {}", asin, e.getMessage());
            return null;
        } finally {
            localDriver.quit();
        }
    }

    private String tryGetText(WebDriver d, By selector) {
        try {
            return d.findElement(selector).getText();
        } catch (Exception e) {
            return "";
        }
    }


    private WebDriver createIsolatedDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");
        options.addArguments("--user-agent=" + getRandomUserAgent());
        return new ChromeDriver(options);
    }
    private String getRandomUserAgent() {
        List<String> agents = List.of(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.6367.202 Safari/537.36",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 13_4) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.4 Safari/605.1.15",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:124.0) Gecko/20100101 Firefox/124.0"
        );
        return agents.get(new Random().nextInt(agents.size()));
    }

    /**
     * Fecha o driver ao final. Chame este método quando concluir o scraping,
     * ou gerencie via @PreDestroy se quiser encerrar automaticamente.
     */
    public void quitDriver() {
        if (driver != null) {
            driver.quit();
        }
    }
}
