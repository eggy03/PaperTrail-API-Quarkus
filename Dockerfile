FROM eclipse-temurin:25 AS build
WORKDIR /jvm-build

# Copy Maven wrapper
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Set execution permission for the Maven wrapper
RUN chmod +x ./mvnw
RUN ./mvnw -B -e -DskipTests dependency:go-offline

# Copy the source files after dependencies are cached
COPY src ./src

RUN ./mvnw -B -e -DskipTests clean package

# Stage 2: Create the final Docker image using IBM Semeru Runtime
FROM ibm-semeru-runtimes:open-25-jre-noble AS runtime
RUN useradd -r -m papertrail
WORKDIR /app

# Copy the build files from the target folder in the build stage
COPY --from=build /jvm-build/target/quarkus-app /app/quarkus-app
USER papertrail
# quarkus defaults to prod
ENTRYPOINT ["java", "-Djava.net.preferIPv4Stack=true", "-jar", "/work/quarkus-app/quarkus-run.jar"]