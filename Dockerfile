# ==============================================================
# Dockerfile para desplegar GlucoAPI en Render (o cualquier host
# que ejecute contenedores).
#
# Etapa 1: Maven + JDK 21 -> construye el WAR
# Etapa 2: Tomcat 11 + JDK 21 -> ejecuta el WAR
# ==============================================================

# ---------- Etapa 1: compilacion ----------
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# Copiar el POM y descargar dependencias (cache de capas)
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copiar el codigo y empaquetar el WAR
COPY src ./src
COPY web ./web
RUN mvn -B clean package -DskipTests

# ---------- Etapa 2: ejecucion ----------
FROM tomcat:11.0-jdk21-temurin

ENV JAVA_OPTS="-Xms64m -Xmx256m"

# Quitar las aplicaciones de ejemplo de Tomcat
RUN rm -rf /usr/local/tomcat/webapps/*

# Publicar la API en la raiz (/) y tambien con el nombre del proyecto
COPY --from=builder /app/target/API_HealthBlood_512.war /usr/local/tomcat/webapps/ROOT.war
COPY --from=builder /app/target/API_HealthBlood_512.war /usr/local/tomcat/webapps/API_HealthBlood_512.war

# Render asigna el puerto en la variable $PORT
RUN echo '#!/bin/sh\n\
PORT="${PORT:-8080}"\n\
echo "Iniciando Tomcat en el puerto $PORT..."\n\
sed -i "s/port=\"8080\"/port=\"$PORT\"/g" /usr/local/tomcat/conf/server.xml\n\
sed -i "s/<Server port=\"8005\"/<Server port=\"-1\"/g" /usr/local/tomcat/conf/server.xml\n\
exec catalina.sh run' > /usr/local/bin/entrypoint.sh && chmod +x /usr/local/bin/entrypoint.sh

EXPOSE 8080

ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
