# 1. Use uma imagem base de Linux com OpenJDK 17 (Java 17)
FROM openjdk:17-slim

# 2. Ajusta o modo "não interativo" para evitar travar
ENV DEBIAN_FRONTEND=noninteractive

# 3. Instala dependências necessárias para rodar o Chrome/Chromium
#    e o ChromeDriver (ou chromium-driver, dependendo do repositório)
RUN apt-get update && apt-get install -y --no-install-recommends \
    ca-certificates \
    fonts-liberation \
    libappindicator3-1 \
    xdg-utils \
    wget \
    gnupg2 \
    chromium \
    chromium-driver \
    && rm -rf /var/lib/apt/lists/*

# 4. Define variáveis de ambiente que algumas ferramentas usam
ENV CHROME_DRIVER=/usr/bin/chromedriver
ENV CHROME_BIN=/usr/bin/chromium

# 5. Cria uma pasta "app" e copia seu jar (ex.: "spring-scraper.jar") pra lá
WORKDIR /app
COPY target/spring-scraper.jar /app/app.jar

# 6. Exponha a porta 8080 (onde o Spring Boot roda por padrão)
EXPOSE 8080

# 7. Defina o entrypoint p/ executar o jar
ENTRYPOINT ["java", "-jar", "app.jar"]
