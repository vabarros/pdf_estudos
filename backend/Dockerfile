# Stage 1: Build the application using Maven and OpenJDK 17
# Usa uma imagem Maven com OpenJDK 17 para o processo de build
FROM maven:3.8.5-openjdk-17 AS build

# Define o diretório de trabalho dentro do container
WORKDIR /app

# Copia o arquivo pom.xml e o código fonte para o container
COPY pom.xml .
COPY src ./src

# Executa o build do Maven. -DskipTests para pular os testes durante o deploy.
RUN mvn clean install -DskipTests

# Stage 2: Create the final lightweight runtime image
# Usa uma imagem OpenJDK 17 slim (reduzida) para a imagem final do aplicativo
FROM openjdk:17-jdk-slim

# Define o diretório de trabalho para a aplicação final
WORKDIR /app

# Copia o JAR compilado do estágio de build para a imagem final
# O nome do JAR é geralmente gerado como 'nome-do-projeto-versao.jar'
# O '*' garante que pegue qualquer nome de JAR na pasta target
COPY --from=build /app/target/*.jar app.jar

# Expõe a porta que sua aplicação Spring Boot escuta (padrão é 8080)
EXPOSE 8080

# Comando para iniciar a aplicação quando o container for executado
CMD ["java", "-jar", "app.jar"]