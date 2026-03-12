# PaperTrail-API-Quarkus

API service for the PaperTrail Bot, built with Quarkus 3 and optimized for native image builds via GraalVM.

# Status

![Build and Tests](https://img.shields.io/github/actions/workflow/status/eggy03/PaperTrail-API-Quarkus/.github%2Fworkflows%2Fbuild_verify.yml?style=for-the-badge&label=BUILD)
![Docker Images](https://img.shields.io/github/actions/workflow/status/eggy03/PaperTrail-API-Quarkus/.github%2Fworkflows%2Fpublish-docker-images.yml?style=for-the-badge&label=IMAGES)
![Latest Tag](https://img.shields.io/github/v/tag/eggy03/PaperTrail-API-Quarkus?sort=semver&style=for-the-badge&label=LATEST%20TAG)
![Latest Release](https://img.shields.io/github/v/release/eggy03/PaperTrail-API-Quarkus?sort=date&display_name=tag&style=for-the-badge&label=LATEST%20RELEASE)


# Self-Hosting Guide

> [!WARNING]
> This API does not implement authentication.
> It is intended to run in a private network environment and should only be accessible by the bot service.
> Do not expose it publicly.

## Step 1: Set Up Required Services & Get Required Secrets

### Services Required

| Service Type          | Supported Variants |
|-----------------------|--------------------|
| `Relational Database` | Postgres           |
| `Distributed Cache`   | Redis / Valkey     |

It is recommended that you deploy the latest or the officially supported versions of Postgres and Redis or Valkey.
At the time of development, Postgres 17 and 18 and Valkey 8 were fully supported.

Below are the links to the official docs stating the support status of each of them:

- [Postgres Supported Versions](https://www.postgresql.org/support/versioning/)
- [Redis Supported Versions](https://redis.io/docs/latest/operate/rs/references/supported-platforms/)
- [Valkey Supported Versions](https://valkey.io/topics/releases/)

### Environment Variables Required

| Variable      | Description                                                 |
|---------------|-------------------------------------------------------------|
| `DB_URL`      | Example: `jdbc:postgresql://<host>:<port>/<database>`       |
| `DB_USERNAME` | Database username                                           |
| `DB_PASSWORD` | Database password                                           |
| `REDIS_URL`   | Example: `redis://<host>:<port>`                            |

Example `.env`

```dotenv
DB_URL=jdbc:postgresql://localhost:5432/papertrail
DB_USERNAME=postgres
DB_PASSWORD=yourpassword
REDIS_URL=redis://localhost:6379
```

## Step 2: Deployment Options

### Local

- Option 1: Using pre-built images from GitHub Container Registry

```shell
# either jvm
docker run -d --name papertrail-api -p 9000:8080 -e DB_URL="url" -e DB_USERNAME="uname" -e DB_PASSWORD="pwd" -e REDIS_URL="redisUrl" ghcr.io/eggy03/papertrail-api:latest-jvm
# or native
docker run -d --name papertrail-api -p 9000:8080 -e DB_URL="url" -e DB_USERNAME="uname" -e DB_PASSWORD="pwd" -e REDIS_URL="redisUrl" ghcr.io/eggy03/papertrail-api:latest-native
```

You can also use an `.env` file instead

```shell
docker run -d --name papertrail-api -p 9000:8080 --env-file .env ghcr.io/eggy03/papertrail-api:latest-jvm
```

Docker Compose Example:

```yaml
services:
  papertrail-api:
    container_name: papertrail-api
    image: ghcr.io/eggy03/papertrail-api:latest-native
    mem_limit: 512m
    restart: unless-stopped
    environment:
      DB_URL: jdbc:postgresql://database:5432/papertrail
      DB_USERNAME: defaultdb
      DB_PASSWORD: ${DB_PASSWORD}
      REDIS_URL: redis://default:${CACHE_PASSWORD}@cache:6379
    healthcheck:
      test: [ "CMD", "curl", "-f", "http://localhost:8080/q/health" ]
      interval: 10s
      timeout: 5s
      retries: 3
```

The API will now listen on http://localhost:9000.

- Option 2: Building from source

```shell
git clone https://github.com/eggy03/PaperTrail-API-Quarkus.git
cd PaperTrail-API-Quarkus
```

```shell
#either jvm
docker build -f Dockerfile.jvm -t papertrail-api .
#or native
docker build -f Dockerfile.native -t papertrail-api .
#and then
docker run -p 9000:8080 --env-file .env papertrail-api
```
The API will now listen on http://localhost:9000.

### Cloud Based

Many cloud platforms support Docker-based deployments directly from a repository.

Typically, the process involves:

- Linking the repository
- Selecting the `Dockerfile`
- Supplying the required environment variables

Alternatively, you can deploy using the pre-built container images found in the GitHub Container Registry

This avoids building the image during deployment and can significantly speed up startup time.

### Choosing between JVM and Native

This project provides two Dockerfiles:

- `Dockerfile.jvm` — Runs the service on JVM
- `Dockerfile.native` — Runs the service natively by building native binaries

While native image allows for very low memory usage and very fast application startup times, compared to JVM mode,
it's tradeoffs include very high build time and resource usage.

If you are using pre-built images from the container registry, you are encouraged to use the native image,
since the expensive build step has already been completed.

# Health Check Endpoints

The service exposes three major health check endpoints that determine the status of the service.

> /q/health/live - The application is up and running.
>
> /q/health/ready - The application is ready to serve requests.
>
> /q/health/started - The application is started.

Some cloud platforms may require these endpoints to periodically determine the health of your deployed service.

> /q/health - Accumulates all health check procedures in the application.

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

### What this means for you:

- If you deploy this project **without modifying the source code**, you do not need to provide anything additional.
  The source code is already publicly available.

- If you **modify the source code** and run it as a service where users interact with it over a network,
  you must make the complete corresponding source code of your modified version available to those users.
  You are not required to publish it publicly.

- You may modify, redistribute, rebrand, and monetize the project. However:
  - Your version must remain licensed under AGPLv3.
  - You must preserve copyright notices and the original license.
  - You must clearly state any changes you have made.

- This software is provided without warranty, as described in AGPLv3.

# Help

If you face any problems during self-hosting or have a question that needs to be answered, please feel free to
open an issue in the Issues tab. I will try my best to answer them as soon as I can.
