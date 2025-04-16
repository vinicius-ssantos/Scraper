package org.vinissius.scraper_spring.util;

import java.util.List;
import java.util.Random;

public class ProxyHandler {

    // Lista de proxies no formato "host:porta"
    private static final List<String> PROXIES = List.of(
        "proxy1.exemplo.com:8080",
        "proxy2.exemplo.com:8080",
        "proxy3.exemplo.com:8080"
    );

    public static String getRandomProxy() {
        return PROXIES.get(new Random().nextInt(PROXIES.size()));
    }
}
