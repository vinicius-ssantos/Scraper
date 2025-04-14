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
 * A anotação @Scope("prototype") faz com que cada injeção gere um driver novo.
 */
@Configuration
public class WebDriverConfig {

    @Bean
    @Scope("prototype")
    public WebDriver webDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        // Executa o Chrome em modo headless (remova se quiser visualizar a execução)
        options.addArguments("--headless", "--disable-gpu", "--no-sandbox");
        return new ChromeDriver(options);
    }
}
