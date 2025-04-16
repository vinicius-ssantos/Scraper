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

        // Obtém proxy dinâmico via API getproxylist.com
        String proxyAddress = ProxyRotatorService.getProxy();
        if (proxyAddress != null) {
            options.addArguments("--proxy-server=http://" + proxyAddress);
            System.out.println("Usando proxy: " + proxyAddress);
        } else {
            System.out.println("Nenhum proxy obtido; prosseguindo sem proxy.");
        }

        // Outras configurações essenciais
        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");

        return new ChromeDriver(options);
    }
}
