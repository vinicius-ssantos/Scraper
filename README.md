# Scraper Amazon - Spring Boot + Selenium + Docker + Kubernetes ☁️

Aplicação Java que realiza scraping de produtos da Amazon Brasil com Selenium e armazena os dados em banco H2. A API é baseada em Spring Boot e está preparada para execução local, containerizada (Docker) ou orquestrada (Kubernetes).

---

## 🧩 Tecnologias Utilizadas

- **Java 22** + **Spring Boot 3**
- **Spring Data JPA + H2 Database** (modo memória)
- **Selenium WebDriver + WebDriverManager**
- **Gson + Jackson** (serialização JSON)
- **Logback (via SLF4J)** para logging customizado
- **Spring Legacy Web (MVC)**
- **Docker + Docker Compose**
- **Kubernetes (deployment + service)**

---

## 📁 Arquitetura de Classes

- `ScraperController`: Endpoint REST
- `ScraperService`: Orquestra scraping e persistência
- `SeleniumScraper`: Realiza scraping de fato via Selenium
- `WebDriverManagerUtil`: Gerencia instância do ChromeDriver
- `WebDriverConfig`: Configura opções do navegador
- `ProductRepository`: Repositório Spring JPA
- `ProductEntity`: Entidade JPA mapeada para a tabela `products`

---

## 📦 Execução Local

```bash
mvn spring-boot:run
```

Acesse via:
```http
GET http://localhost:8080/api/scraper?max=5
```

---

## 🐳 Docker

### Build
```bash
docker build -t amazon-scraper .
```

### Run
```bash
docker run -p 8080:8080 --rm amazon-scraper
```

---

## ☸️ Kubernetes

### Aplicação
```bash
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
```

### Acesso
```bash
kubectl port-forward svc/amazon-scraper 8080:8080
curl http://localhost:8080/api/scraper?max=5
```

---

## 📄 Exemplo de JSON extraído
```json
{
  "title": "8K USB Displayport KVM Switch",
  "price": "R$ 749,90",
  "rating": "4.5 de 5 estrelas",
  "url": "https://www.amazon.com.br/...",
  "executedAt": "2025-04-13 21:28:25"
}
```

---

## 📜 Spring Legacy

A aplicação utiliza arquitetura baseada em Spring Boot com dependências compatíveis com projetos legados:

- Utiliza `spring-boot-starter-web` com Spring MVC tradicional (Servlets + Controllers).
- Sem uso de WebFlux ou módulos reativos, ideal para manutenção em ambientes legados.
- Usa H2 como banco temporário (mas pode ser substituído facilmente por PostgreSQL ou MySQL).

---

## 🛠️ Funcionalidades Avançadas Implementadas

- Retry com backoff exponencial ao falhar scraping
- WebDriver configurado com Chrome Headless e rotações automáticas
- Logs com timestamp e nível (scraping.log)
- Exportação automática em JSON (`products_info_*.json`)
- Modularização com responsabilidades claras
- Suporte a múltiplos produtos com paralelismo (thread-safe)
- Captura da data e hora da extração (`executedAt`)

---

## 📁 Estrutura de Diretórios

```
scraper-java/
├── src/main/java/
│   └── org/vinissius/scraper_spring/
├── src/main/resources/
│   ├── application.properties
│   ├── logback.xml
├── k8s/
│   ├── deployment.yaml
│   └── service.yaml
├── Dockerfile
└── README.md
```


## 📌 Melhorias Futuras

- Armazenar logs e JSON em bucket S3 ou Azure Blob
- Agendamento automático via `@Scheduled`
- Monitoramento Prometheus + Grafana
- Login básico para autenticar chamadas à API
- Trocar H2 por PostgreSQL persistente

---

## 🧪 Teste de Endpoint
```bash
curl http://localhost:8080/api/scraper?max=5
```

---

## 👨‍💻 Autor
Vinícius Oliveira  
[LinkedIn](https://www.linkedin.com/in/)

---

MIT License

