package org.vinissius.scraper_spring.util;

import org.springframework.web.client.RestTemplate;

public class ProxyRotatorService {

    private static final String PROXY_API_URL = "https://api.getproxylist.com/proxy";

    public static String getProxy() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            ProxyResponse response = restTemplate.getForObject(PROXY_API_URL, ProxyResponse.class);
            if (response != null && response.getIp() != null && response.getPort() != null) {
                // Retorna no formato "ip:porta"
                return response.getIp() + ":" + response.getPort();
            } else {
                System.out.println("API não retornou proxy válido.");
                return null;
            }
        } catch (Exception e) {
            System.err.println("Erro ao obter proxy: " + e.getMessage());
            return null;
        }
    }
}
