package org.vinissius.scraper.util;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class WebDriverManagerUtil {

    public static WebDriver getDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions opts = new ChromeOptions();
        opts.addArguments("--headless", "--disable-gpu", "--no-sandbox");
        return new ChromeDriver(opts);
    }
}
