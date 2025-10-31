FROM eclipse-temurin:17-jdk
WORKDIR /app

# Installa Arial (Microsoft Core Fonts) e fontconfig
RUN apt-get update && \
    echo "ttf-mscorefonts-installer msttcorefonts/accepted-mscorefonts-eula select true" | debconf-set-selections && \
    apt-get install -y ttf-mscorefonts-installer fontconfig && \
    fc-cache -f -v && \
    apt-get clean && rm -rf /var/lib/apt/lists/*


# Imposto timezone
ENV TZ=Europe/Rome
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone


# Copia sorgenti
COPY . .

# Build Spring Boot
RUN ./mvnw clean package -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/app.jar"]
