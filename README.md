# PaperTrail-API-Quarkus

API Service required by PaperTrailBot to store per-server configuration and messages.

# Status

![Build and Tests](https://img.shields.io/github/actions/workflow/status/eggy03/PaperTrail-API-Quarkus/.github%2Fworkflows%2Fbuild_verify.yml?style=for-the-badge&label=BUILD)
![Latest Release](https://img.shields.io/github/v/release/eggy03/PaperTrail-API-Quarkus?sort=date&display_name=tag&style=for-the-badge&label=LATEST%20RELEASE)

# Self-Host Guide

> [!WARNING]
> This API service is meant to be run internally and accessible only by the bot, and as such, does not contain any form
of authentication. Do not make this service publicly available once deployed.

## Step 1: Set up the required services

| Service Type          | Supported Variants |
|-----------------------|--------------------|
| `Relational Database` | PostgreSQL         |
| `Distributed Cache`   | Redis / Valkey     |

## Step 2: Set up the required environment variables

| Variable      | Description                                            |
|---------------|--------------------------------------------------------|
| `DB_URL`      | Example: `jdbc:postgresql://<host>:<port>/<database>`  |
| `DB_USERNAME` | Database username                                      |
| `DB_PASSWORD` | Database password                                      |
| `REDIS_URL`   | Example: `redis://<username>:<password>@<host>:<port>` |

See example : `.env.example` in project root

## Step 3: Deploy the API service

The base URL you get after deploying the service will be required by the bot service.

By default, the service will listen at port 8080. You can change this by supplying a custom value
in env variable `PORT`.

#### Option A : Deploy Using Pre-Built Docker Images

The GitHub Container Registry
has pre-built docker images for both JVM and Native versions the API service which you can use.

You may choose either one.

```bash
# JVM
docker run -d --name papertrail-api --env-file .env ghcr.io/eggy03/papertrail-api:latest
```

```bash
# Native
docker run -d --name papertrail-api-native --env-file .env ghcr.io/eggy03/papertrail-api-native:latest
```

#### Option B : Building From Source With Docker

```bash
git clone https://github.com/eggy03/PaperTrail-API-Quarkus.git
cd PaperTrail-API-Quarkus
```

```bash
# JVM
docker build -f -t papertrail-api .
docker run -d --name papertrail-api --env-file .env papertrail-api
```

```bash
# Native
docker build -f Dockerfile.native -t papertrail-api-native .
docker run -d --name papertrail-api-native --env-file .env papertrail-api-native
```

#### Option C : Building From Source Without Docker

```bash
git clone https://github.com/eggy03/PaperTrailBot.git
cd PaperTrailBot
```

```bash
# JVM
./mvnw clean package
java -jar target/quarkus-app/quarkus-run.jar
```

```bash
# Native
./mvnw clean package -Dnative
```

The built application will be found in the `target` folder of the project.

#### Option D : Cloud Deployment

If your cloud supports building from Dockerfile, point the source towards `Dockerfile` (for JVM Build)
or `Dockerfile.native` (for Native Build), found in the project's root.

If your cloud supports using pre-built docker images, you can find the image links in
the container registry.

# Health Check Endpoints

| Endpoint            | Description                            |
|---------------------|----------------------------------------|
| `/q/health/live`    | Application is running                 |
| `/q/health/ready`   | Application is ready to serve requests |
| `/q/health/started` | Application startup has completed      |
| `/q/health`         | Aggregated health status               |

By default, the health check interface will listen to port 9000. You can change this by supplying a custom value
in env variable `MANAGEMENT_PORT`.

# Migration Guide

> [!NOTE]
> This section applies only to users migrating from the Spring-based API.

Depending on your existing database setup, you may encounter up to **two** breaking changes:

**Case-1**: Using a database other than PostgreSQL

You need to migrate your existing data to a newly created Postgres DB.
This API exclusively supports Postgres. Support for other DBs have been dropped to ease maintainability.

**Case-2**: Already using Postgres

There is only **one** breaking change:

- Previously, tables were created in the default schema.
- The new API uses flyway to check and create tables in a custom schema named `papertrailbot` on startup.

The table structures and relationships remain unchanged.
You only need to migrate your existing data from the default schema to the `papertrailbot` schema.

# License

This API is licensed under the [AGPLv3](/LICENSE) license.

# Help

If you face any problems during self-hosting or have a question that needs to be answered, please feel free to
open an issue in the Issues tab. I will try my best to answer them as soon as I can.