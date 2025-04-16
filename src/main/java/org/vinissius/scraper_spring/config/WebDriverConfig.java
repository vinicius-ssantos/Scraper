package org.vinissius.scraper_spring.config;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * Configuração do WebDriver como bean do Spring.
 * @Scope("prototype") faz cada injeção gerar um driver novo.
 */
@Configuration
public class WebDriverConfig {

    @Bean
    @Scope("prototype")
    public WebDriver webDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        // Executa Chrome em modo headless (remover se quiser ver o navegador)
        options.addArguments("--headless", "--disable-gpu", "--no-sandbox");
        return new ChromeDriver(options);
    }
}
