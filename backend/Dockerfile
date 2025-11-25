# ---- Runtime image (small) ----
FROM eclipse-temurin:17-jre
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy your built jar
COPY target/campus-backend-mysql-full-0.1.0-SNAPSHOT.jar /app/app.jar

# Optional JVM flags can be passed via JAVA_OPTS env
ENV SERVER_PORT=8080
EXPOSE 8080

# Health endpoint is /health (used by ALB/compose healthcheck)
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
