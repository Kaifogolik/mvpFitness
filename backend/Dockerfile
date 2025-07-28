# Multi-stage build для оптимизации размера образа
FROM openjdk:21-jdk-slim as builder

# Установка необходимых инструментов
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Создание рабочей директории
WORKDIR /app

# Копирование файлов проекта
COPY pom.xml .
COPY src ./src

# Установка Maven
RUN curl -fsSL https://apache.osuosl.org/maven/maven-3/3.9.5/binaries/apache-maven-3.9.5-bin.tar.gz | tar -xzC /opt
ENV PATH="/opt/apache-maven-3.9.5/bin:${PATH}"

# Сборка приложения
RUN mvn clean package -DskipTests

# Production stage
FROM openjdk:21-jre-slim

# Установка дополнительных пакетов для работы с файлами и сетью
RUN apt-get update && \
    apt-get install -y curl wget && \
    rm -rf /var/lib/apt/lists/*

# Создание пользователя для безопасности
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Создание рабочей директории
WORKDIR /app

# Создание директорий для логов и загрузок
RUN mkdir -p /app/logs /app/uploads && \
    chown -R appuser:appuser /app

# Копирование JAR файла из builder stage
COPY --from=builder /app/target/fitness-platform-*.jar app.jar

# Изменение владельца файлов
RUN chown appuser:appuser app.jar

# Переключение на непривилегированного пользователя
USER appuser

# Настройка JVM для контейнера
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+UseStringDeduplication"

# Порт приложения
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Команда запуска
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# Метаданные
LABEL maintainer="FitCoach AI Team"
LABEL version="1.0.0"
LABEL description="FitCoach AI Platform - Fitness and nutrition tracking with AI" 