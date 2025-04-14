package org.vinissius.scraper_spring.util;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 * Utilitário para configurar e instanciar o WebDriver.
 */
public class WebDriverManagerUtil {

    public static WebDriver getDriver() {
        // Configura o ChromeDriver automaticamente com WebDriverManager
        WebDriverManager.chromedriver().setup();

        // Cria as opções para o Chrome: executa em modo headless, sem GPU e sem sandbox
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--disable-gpu", "--no-sandbox");

        return new ChromeDriver(options);
    }
}
