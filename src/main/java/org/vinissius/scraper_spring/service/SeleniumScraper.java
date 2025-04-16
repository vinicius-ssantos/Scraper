package org.vinissius.scraper_spring.service;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.vinissius.scraper_spring.entity.ProductEntity;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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

    private static final String SELECTORS_FILE = "selectors.txt";

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
        
        WebDriver driver = createIsolatedDriver();



        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        String url = "https://www.amazon.com.br/dp/" + asin;


        try {

            driver.get(url);
            log.info("Acessando URL: {}", url);


            wait.until(ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(By.id("ppd")),
                    ExpectedConditions.presenceOfElementLocated(By.id("centerCol")) // fallback
            ));

            List<By> selectors = loadSelectors();
            By foundSelector = wait.until(d -> {
                for (By selector : selectors) {
                    try {
                        if (d.findElement(selector).isDisplayed()) {
                            return selector;
                        }
                    } catch (Exception ignored) {}
                }
                return null;
            });

            if (foundSelector != null) {
                log.info("Produto [{}] carregado com seletor: {}", asin, foundSelector);
                saveNewSelector(foundSelector, url);


            } else {
                throw new TimeoutException("Nenhum seletor encontrado");
            }

            String price = tryGetText(driver, By.id("priceblock_ourprice"));
            if (price.isEmpty()) {
                price = tryGetText(driver, By.id("priceblock_dealprice"));
            }

            String rating = tryGetText(driver, By.cssSelector("span.a-icon-alt"));
            String reviewCount = tryGetText(driver, By.id("acrCustomerReviewText"));

            List<String> bulletPoints = new ArrayList<>();
            try {
                List<WebElement> bullets = driver.findElements(By.cssSelector("#feature-bullets ul li span"));
                bulletPoints = bullets.stream().map(WebElement::getText).collect(Collectors.toList());
            } catch (Exception ignored) {}

            String description = tryGetText(driver, By.id("productDescription"));

            List<String> images = new ArrayList<>();
            try {
                List<WebElement> imgs = driver.findElements(By.cssSelector("img.a-dynamic-image"));
                images = imgs.stream().map(img -> img.getAttribute("src")).distinct().collect(Collectors.toList());
            } catch (Exception ignored) {}

            String seller = tryGetText(driver, By.id("sellerProfileTriggerId"));
            if (seller.isEmpty()) {
                seller = tryGetText(driver, By.id("bylineInfo"));
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
            driver.quit();
        }
    }

    private List<By> loadSelectors() {
        List<By> selectors = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(SELECTORS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("id=")) {
                    selectors.add(By.id(line.substring(3)));
                } else if (line.startsWith("css=")) {
                    selectors.add(By.cssSelector(line.substring(4)));
                }
            }
        } catch (IOException e) {
            log.error("Erro ao ler arquivo de seletores: {}", e.getMessage());
        }
        return selectors;
    }

    private String tryGetText(WebDriver d, By selector) {
        try {
            return d.findElement(selector).getText();
        } catch (Exception e) {
            return "";
        }
    }

    private List<By> loadSelectors(String url) {
        String domain = getDomainFromUrl(url);
        String filename = "selectors_" + domain + ".txt";
        List<By> selectors = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("id=")) {
                    selectors.add(By.id(line.substring(3)));
                } else if (line.startsWith("css=")) {
                    selectors.add(By.cssSelector(line.substring(4)));
                }
            }
        } catch (IOException e) {
            log.warn("Arquivo de seletores não encontrado para [{}]: {}", domain, filename);
        }

        return selectors;
    }

    private void saveNewSelector(By selector, String url) {
        String selectorString = selector.toString(); // Ex: By.id: centerCol
        String normalized = null;

        if (selectorString.startsWith("By.id: ")) {
            normalized = "id=" + selectorString.substring(8);
        } else if (selectorString.startsWith("By.cssSelector: ")) {
            normalized = "css=" + selectorString.substring(16);
        }

        if (normalized != null) {
            String domain = getDomainFromUrl(url);
            String filename = "selectors_" + domain + ".txt";

            if (!isSelectorInFile(normalized, filename)) {
                try (FileWriter fw = new FileWriter(filename, true)) {
                    fw.write(normalized + System.lineSeparator());
                    log.info("Novo seletor salvo dinamicamente em {}: {}", filename, normalized);
                } catch (IOException e) {
                    log.error("Erro ao salvar seletor no arquivo {}: {}", filename, e.getMessage());
                }
            }
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

    private boolean isSelectorInFile(String selector, String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            return br.lines().anyMatch(line -> line.trim().equals(selector));
        } catch (IOException e) {
            return false;
        }
    }

    private String getDomainFromUrl(String url) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost(); // www.amazon.com.br
            return host.startsWith("www.") ? host.substring(4) : host;
        } catch (URISyntaxException e) {
            log.error("Erro ao extrair domínio da URL: {}", e.getMessage());
            return "default";
        }
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
