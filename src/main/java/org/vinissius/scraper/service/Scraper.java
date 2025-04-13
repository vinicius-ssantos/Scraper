package org.vinissius.scraper.service;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import org.vinissius.scraper.model.Product;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Scraper {

    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 5000;

    // Limpa a URL, removendo redirecionamentos
    public String cleanProductUrl(String url) {
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

    // Extrai URLs de produtos a partir da listagem
    public List<String> getProductUrls(WebDriver driver, String listingUrl, int maxProducts) {
        List<String> urls = new ArrayList<>();
        int attempt = 0;
        while (attempt < MAX_RETRIES) {
            try {
                driver.get(listingUrl);
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("div[data-asin].s-result-item")));
                break;
            } catch (Exception e) {
                attempt++;
                try { Thread.sleep(RETRY_DELAY_MS); } catch (InterruptedException ignored) {}
            }
        }
        // Coleta os itens da página
        List<WebElement> items = driver.findElements(By.cssSelector("div[data-asin].s-result-item"));
        for (WebElement item : items) {
            try {
                WebElement link = item.findElement(By.cssSelector("a.a-link-normal"));
                String rawUrl = link.getAttribute("href");
                String finalUrl = cleanProductUrl(rawUrl);
                if (finalUrl != null && !urls.contains(finalUrl)) {
                    urls.add(finalUrl);
                }
            } catch (Exception ignored) {}
            if (urls.size() >= maxProducts) {
                break;
            }
        }
        return urls;
    }

    // Extrai as informações do produto
    public Product getProductInfo(WebDriver driver, String url) {
        int attempt = 0;
        while (attempt < MAX_RETRIES) {
            try {
                driver.get(url);
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
                wait.until(ExpectedConditions.presenceOfElementLocated(By.id("productTitle")));
                break;
            } catch (Exception e) {
                attempt++;
                try { Thread.sleep(RETRY_DELAY_MS); } catch (InterruptedException ignored) {}
            }
        }
        if (attempt == MAX_RETRIES) {
            System.err.println("Falha ao carregar a página: " + url);
            return null;
        }

        Product product = new Product();

        // Title
        product.setTitle(extractTitle(driver));
        // Price (com fallback)
        product.setPrice(extractPrice(driver));
        // Rating
        product.setRating(extractRating(driver));
        // Review Count
        product.setReviewCount(extractReviewCount(driver));
        // Bullet points
        product.setBulletPoints(extractBulletPoints(driver));
        // Descrição
        product.setProductDescription(extractDescription(driver));
        // Images
        product.setImages(extractImages(driver));
        // Seller Info
        product.setSellerInfo(extractSellerInfo(driver));
        // ASIN
        product.setAsin(extractASIN(driver));
        // Data/hora de execução
        product.setExecutedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        // URL
        product.setUrl(url);

        return product;
    }

    // Título
    private String extractTitle(WebDriver driver) {
        try {
            WebElement elem = driver.findElement(By.id("productTitle"));
            return elem.getText().trim();
        } catch (Exception e) {
            return null;
        }
    }

    // Preço com fallback
    private String extractPrice(WebDriver driver) {
        try {
            WebElement priceElem = driver.findElement(By.id("priceblock_ourprice"));
            return priceElem.getText().trim();
        } catch (Exception e1) {
            // fallback
            try {
                WebElement fallbackElem = driver.findElement(By.cssSelector("span.a-price.aok-align-center.reinventPricePriceToPayMargin.priceToPay"));
                String whole = safeText(fallbackElem.findElement(By.cssSelector("span.a-price-whole")));
                String decimal = safeText(fallbackElem.findElement(By.cssSelector("span.a-price-decimal")));
                String fraction = safeText(fallbackElem.findElement(By.cssSelector("span.a-price-fraction")));
                return whole + decimal + fraction;
            } catch (Exception e2) {
                return null;
            }
        }
    }

    // Rating
    private String extractRating(WebDriver driver) {
        try {
            WebElement ratingElem = driver.findElement(By.cssSelector("span.a-icon-alt"));
            return ratingElem.getText().trim();
        } catch (Exception e) {
            return null;
        }
    }

    // Review Count
    private String extractReviewCount(WebDriver driver) {
        try {
            WebElement reviewCountElem = driver.findElement(By.id("acrCustomerReviewText"));
            return reviewCountElem.getText().trim();
        } catch (Exception e) {
            return null;
        }
    }

    // Bullet Points
    private List<String> extractBulletPoints(WebDriver driver) {
        List<String> bulletPoints = new ArrayList<>();
        try {
            List<WebElement> bulletElems = driver.findElements(By.cssSelector("#feature-bullets span.a-list-item"));
            for (WebElement be : bulletElems) {
                String text = be.getText().trim();
                if (!text.isEmpty()) {
                    bulletPoints.add(text);
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return bulletPoints.isEmpty() ? null : bulletPoints;
    }

    // Descrição do produto
    private String extractDescription(WebDriver driver) {
        try {
            WebElement descElem = driver.findElement(By.id("productDescription"));
            return descElem.getText().trim();
        } catch (Exception e1) {
            try {
                WebElement aplusElem = driver.findElement(By.id("aplus"));
                return aplusElem.getText().trim();
            } catch (Exception e2) {
                return null;
            }
        }
    }

    // Imagens
    private List<String> extractImages(WebDriver driver) {
        List<String> images = new ArrayList<>();
        try {
            WebElement imgElem = driver.findElement(By.id("imgTagWrapperId")).findElement(By.tagName("img"));
            String url = imgElem.getAttribute("data-old-hires");
            if (url == null || url.isEmpty()) {
                url = imgElem.getAttribute("src");
            }
            if (url != null && !url.isEmpty()) {
                images.add(url);
            }
        } catch (Exception e) {
            // ignore
        }
        return images.isEmpty() ? null : images;
    }

    // Seller Info
    private String extractSellerInfo(WebDriver driver) {
        try {
            WebElement sellerElem = driver.findElement(By.id("bylineInfo"));
            return sellerElem.getText().trim();
        } catch (Exception e) {
            return null;
        }
    }

    // ASIN
    private String extractASIN(WebDriver driver) {
        try {
            WebElement detailDiv = driver.findElement(By.id("detailBullets_feature_div"));
            List<WebElement> items = detailDiv.findElements(By.tagName("li"));
            for (WebElement li : items) {
                String text = li.getText();
                if (text.contains("ASIN")) {
                    return text.split(":")[1].trim();
                }
            }
        } catch (Exception e) {
            // fallback regex
        }
        String pageSource = driver.getPageSource();
        Pattern pattern = Pattern.compile("ASIN[\"']?\\s*[:=]\\s*[\"']([A-Z0-9]{10})[\"']", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(pageSource);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String safeText(WebElement elem) {
        return elem.getText().trim();
    }
}
