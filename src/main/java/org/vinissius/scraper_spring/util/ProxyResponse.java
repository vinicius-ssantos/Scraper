package org.vinissius.scraper_spring.util;

public class ProxyResponse {
    private String ip;
    private Integer port;
    private String protocol;
    // Outras propriedades podem ser adicionadas se necessário

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
}
