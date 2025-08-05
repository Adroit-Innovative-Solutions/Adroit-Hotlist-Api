# -------- Build Stage --------
FROM openjdk:21-jdk-slim AS builder

RUN apt-get update && \
    apt-get install -y maven curl && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

# -------- Runtime Stage --------
FROM openjdk:21-jdk-slim

WORKDIR /app

# Install certificate tools for importing certs
RUN apt-get update && apt-get install -y ca-certificates

# Create directory to store custom SSL certs
RUN mkdir -p /etc/ssl/certs/custom

# Copy your SSL certificate into the container
COPY nginx/ssl/mymulya.crt /etc/ssl/certs/custom/mymulya.crt

# Import the certificate into Java truststore (cacerts)
RUN keytool -import -trustcacerts -alias mymulya_cert \
    -file /etc/ssl/certs/custom/mymulya.crt \
    -keystore $JAVA_HOME/lib/security/cacerts \
    -storepass changeit -noprompt

# Accept environment profile and port as build args
ARG SPRING_PROFILES_ACTIVE=prod
ARG PORT=8092

ENV SPRING_PROFILES_ACTIVE=$SPRING_PROFILES_ACTIVE
ENV PORT=$PORT

# Copy built jar from builder stage
COPY --from=builder /app/target/hotlistmicroservice-0.0.1-SNAPSHOT.jar app.jar

EXPOSE $PORT

ENTRYPOINT ["sh", "-c", "java -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE -Dserver.port=$PORT -jar app.jar"]
