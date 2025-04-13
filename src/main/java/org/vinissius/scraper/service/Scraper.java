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

    // Remove redirecionamentos
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
        // extrai os elementos
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
            if (urls.size() >= maxProducts) break;
        }
        return urls;
    }

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
        product.setTitle(extractTitle(driver));
        product.setPrice(extractPrice(driver));
        product.setRating(extractRating(driver));
        product.setReviewCount(extractReviewCount(driver));
        product.setBulletPoints(extractBulletPoints(driver));
        product.setProductDescription(extractDescription(driver));
        product.setImages(extractImages(driver));
        product.setSellerInfo(extractSellerInfo(driver));
        product.setAsin(extractASIN(driver));
        // Data/hora
        product.setExecutedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        product.setUrl(url);

        return product;
    }

    private String extractTitle(WebDriver driver) {
        try {
            return driver.findElement(By.id("productTitle")).getText().trim();
        } catch (Exception e) {
            return null;
        }
    }

    private String extractPrice(WebDriver driver) {
        try {
            return driver.findElement(By.id("priceblock_ourprice")).getText().trim();
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

    private String extractRating(WebDriver driver) {
        try {
            return driver.findElement(By.cssSelector("span.a-icon-alt")).getText().trim();
        } catch (Exception e) {
            return null;
        }
    }

    private String extractReviewCount(WebDriver driver) {
        try {
            return driver.findElement(By.id("acrCustomerReviewText")).getText().trim();
        } catch (Exception e) {
            return null;
        }
    }

    private List<String> extractBulletPoints(WebDriver driver) {
        List<String> bulletPoints = new ArrayList<>();
        try {
            List<WebElement> elems = driver.findElements(By.cssSelector("#feature-bullets span.a-list-item"));
            for (WebElement elem : elems) {
                String text = elem.getText().trim();
                if (!text.isEmpty()) {
                    bulletPoints.add(text);
                }
            }
        } catch (Exception ignored) {}
        return bulletPoints.isEmpty() ? null : bulletPoints;
    }

    private String extractDescription(WebDriver driver) {
        try {
            return driver.findElement(By.id("productDescription")).getText().trim();
        } catch (Exception e1) {
            try {
                return driver.findElement(By.id("aplus")).getText().trim();
            } catch (Exception e2) {
                return null;
            }
        }
    }

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
        } catch (Exception ignored) {}
        return images.isEmpty() ? null : images;
    }

    private String extractSellerInfo(WebDriver driver) {
        try {
            return driver.findElement(By.id("bylineInfo")).getText().trim();
        } catch (Exception e) {
            return null;
        }
    }

    private String extractASIN(WebDriver driver) {
        // Tenta detailBullets
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
            // fallback
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
