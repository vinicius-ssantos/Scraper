# Amazon Scraper - Projeto em Java + Spring Boot + Selenium

Este projeto é uma aplicação de scraping desenvolvida em Java com Spring Boot e Selenium WebDriver, projetada para extrair informações de produtos da Amazon e persistir em um banco de dados H2 em memória, com logs e saídas organizadas por data.

---

## :rocket: Tecnologias Utilizadas
- **Java 22**
- **Spring Boot 3.0.5**
- **Selenium WebDriver 4.9.0**
- **WebDriverManager 5.4.0**
- **H2 Database**
- **Logback** (com `logback.xml` configurado)
- **Gson** para JSON
- **Docker** + **Docker Compose**
- **Kubernetes (CronJob)**

---

## :gear: Funcionalidades
- Extração automatizada de produtos da Amazon
- Persistência em banco relacional (H2)
- Geração de arquivos `.json` com os produtos extraídos
- Logs organizados com timestamp e arquivo `scraping.log`
- Retry com backoff exponencial para falhas em elementos não encontrados
- Modularização por responsabilidade:
    - `ScraperService`, `SeleniumScraper`, `WebDriverManagerUtil`
- Executável via REST endpoint: `/api/scraper?max=5`
- Suporte a ambiente Docker e execução em Kubernetes (via CronJob)

---

## :file_folder: Estrutura dos Diretórios
```
├── Dockerfile
├── k8s
│   └── scraper-cronjob.yaml
├── logs
│   └── scraping.log
├── output
│   └── products_info_YYYYMMDD_HHmmss.json
├── src
│   └── main
│       └── java
│           └── org.vinissius.scraper_spring
│               ├── WebDriverConfig.java
│               ├── WebDriverManagerUtil.java
│               ├── SeleniumScraper.java
│               ├── ScraperService.java
│               ├── ScraperController.java
│               ├── ProductEntity.java
│               ├── ProductRepository.java
│               └── SpringScraperApplication.java
└── resources
    ├── application.properties
    └── logback.xml
```

---

## :whale: Como Dockerizar

### Dockerfile:
```Dockerfile
FROM eclipse-temurin:22-jdk-alpine
WORKDIR /app
COPY target/ScraperJava.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### docker-compose.yaml:
```yaml
version: '3.8'
services:
  scraper:
    build: .
    container_name: amazon-scraper
    volumes:
      - ./logs:/app/logs
      - ./output:/app/output
    environment:
      - LOG_LEVEL=INFO
    restart: unless-stopped
```

---

## :shipit: Kubernetes (opcional)

### `k8s/scraper-cronjob.yaml`
```yaml
apiVersion: batch/v1
kind: CronJob
metadata:
  name: amazon-scraper
spec:
  schedule: "0 * * * *" # a cada hora
  jobTemplate:
    spec:
      template:
        spec:
          containers:
            - name: scraper
              image: viniciusssantos/amazon-scraper:latest
              env:
                - name: LOG_LEVEL
                  value: INFO
              volumeMounts:
                - mountPath: /app/output
                  name: output
                - mountPath: /app/logs
                  name: logs
          restartPolicy: OnFailure
          volumes:
            - name: output
              emptyDir: {}
            - name: logs
              emptyDir: {}
          nodeSelector:
            scraper: "true"
          tolerations:
            - key: "scraper-only"
              operator: "Exists"
              effect: "NoSchedule"
```

---

## :hammer_and_wrench: Como Executar

### Localmente:
```bash
./mvnw clean package
java -jar target/ScraperJava.jar
```

Acesse:
```
GET http://localhost:8080/api/scraper?max=5
```

### Docker:
```bash
docker-compose up --build
```

### Kubernetes:
```bash
kubectl apply -f k8s/scraper-cronjob.yaml
```

---

## :memo: Exemplo de JSON Gerado
```json
[
  {
    "title": "DisplayPort Switch 8K",
    "asin": "B0D7TR5XV3",
    "price": "R$ 299,90",
    "executedAt": "2025-04-13 21:28:20",
    "url": "https://www.amazon.com.br/..."
  },
  ...
]
```

---

## :bulb: Melhorias Futuras
- Exportação para S3, banco externo ou Google Sheets
- Logs para stack ELK ou Grafana Loki
- Auth na API para controle de acesso
- Frontend para visualização dos produtos
- Integração com email/Telegram para notificar novos produtos
- Exportar para CSV e Excel

---

## :bookmark_tabs: Nome de Commit Sugerido
```
feat: integração completa com Docker e Kubernetes + melhorias estruturais
```

---

## :man_technologist: Autor
**Vinicius Oliveira**  
Desenvolvedor Java Backend, automação, scraping e sistemas distribuídos ☕

---

Se precisar, posso gerar badges para GitHub Actions, DockerHub, e Kubernetes também.

