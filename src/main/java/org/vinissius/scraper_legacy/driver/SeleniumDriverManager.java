package org.vinissius.scraper_legacy.driver;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.Random;

public class SeleniumDriverManager {

    private static final String[] USER_AGENTS = {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.1 Safari/605.1.15"
            // Adicione outros se desejar
    };

    /**
     * Cria um WebDriver do Chrome em modo headless, com user-agent randômico e
     * download/configuração automática do driver via WebDriverManager.
     */
    public static WebDriver getDriver() {
        // Download e setup automático do ChromeDriver
        WebDriverManager.chromedriver().setup();

        // Configurações do Chrome
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--disable-gpu", "--no-sandbox");

        // Rotação de user-agent
        String userAgent = USER_AGENTS[new Random().nextInt(USER_AGENTS.length)];
        options.addArguments("user-agent=" + userAgent);

        return new ChromeDriver(options);
    }
}
