package org.vinissius.scraper_spring.util;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class WebDriverManagerUtil {

    public static WebDriver createWebDriver() {
        ChromeOptions options = new ChromeOptions();

        // Ativa o modo headless (sem GUI)
        options.addArguments("--headless");

        // Outras configurações que ajudam a deixar a execução mais estável:
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");

        // Define uma porta para remote debugging (opcional, mas pode ajudar na estabilidade)
        options.addArguments("--remote-debugging-port=9222");

        return new ChromeDriver(options);


    }
}
