package org.vinissius.scraper_spring.util;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class WebDriverManagerUtil {

    public static WebDriver createWebDriver() {
        ChromeOptions options = new ChromeOptions();

        // Adiciona User-Agent aleatório
        String userAgent = UserAgentProvider.getRandomUserAgent();
        options.addArguments("--user-agent=" + userAgent);

        // Outras configurações essenciais
        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");

        return new ChromeDriver(options);
    }
}
