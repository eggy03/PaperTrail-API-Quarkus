# PaperTrail-API-Quarkus

API service for the PaperTrail Bot, built with Quarkus 3 and optimized for native image builds via GraalVM.

# Status

![Build and Tests](https://img.shields.io/github/actions/workflow/status/eggy03/PaperTrail-API-Quarkus/.github%2Fworkflows%2Fbuild_verify.yml?style=for-the-badge&label=BUILD)
![Docker Images](https://img.shields.io/github/actions/workflow/status/eggy03/PaperTrail-API-Quarkus/.github%2Fworkflows%2Fpublish-docker-images.yml?style=for-the-badge&label=IMAGES)
![Latest Tag](https://img.shields.io/github/v/tag/eggy03/PaperTrail-API-Quarkus?sort=semver&style=for-the-badge&label=LATEST%20TAG)
![Latest Release](https://img.shields.io/github/v/release/eggy03/PaperTrail-API-Quarkus?sort=date&display_name=tag&style=for-the-badge&label=LATEST%20RELEASE)
![GitHub commits since latest release](https://img.shields.io/github/commits-since/eggy03/PaperTrail-API-Quarkus/latest?sort=date&style=for-the-badge)

# Self-Host (Auto Configuration)

Recommended for users who want to deploy a single instance of each of the required services
locally or on a VPS with minimal setup.

Follow the deployment guide in:
[PaperTrail-Deployment Repository](https://github.com/eggy03/PaperTrail-Deployment?tab=readme-ov-file)

# Self-Host (Manual Configuration)

Recommended for users who want full control over the deployment process,
prefer building from source, or are deploying to cloud platforms that support repository-based builds.

> [!WARNING]
> This API does not implement authentication.
> It is intended to run in a private network environment and should only be accessible by the bot service.
> Do not expose it publicly.

## 1: Set up the required services & environment variables

### Required Services

| Service Type          | Supported Variants        |
|-----------------------|---------------------------|
| `Relational Database` | PostgreSQL (v18+)         |
| `Distributed Cache`   | Redis (v8+)/ Valkey (v9+) |

### Required Environment Variables

| Variable      | Description                                            |
|---------------|--------------------------------------------------------|
| `DB_URL`      | Example: `jdbc:postgresql://<host>:<port>/<database>`  |
| `DB_USERNAME` | Database username                                      |
| `DB_PASSWORD` | Database password                                      |
| `REDIS_URL`   | Example: `redis://<username>:<password>@<host>:<port>` |

Example `.env`

```dotenv
DB_URL=jdbc:postgresql://database:5432/papertrail
DB_USERNAME=postgres
DB_PASSWORD=yourpassword
REDIS_URL=redis://default:password@cache:6379
```

## 2: Deploy the API service

This project provides two Dockerfiles for building:

- `Dockerfile.jvm` — Runs the service on JVM
- `Dockerfile.native` — Runs the service natively by building native binaries

While native image allows for very low memory usage and very fast application startup times, compared to JVM mode,
it's tradeoffs include very high build time and resource usage. You can skip this by using the pre-built native images
from the [GitHub Container Registry](https://github.com/eggy03/PaperTrail-API-Quarkus/pkgs/container/papertrail-api).

### Option A: Local Deployment

<ins>Using Pre-Built Images</ins>

The GitHub Container Registry has the native build images for the API which you can use.

Make sure you have the `.env` file containing the required secrets in the root of the folder
you're executing the following commands from.

```bash
docker run -d --name papertrail-api --env-file .env ghcr.io/eggy03/papertrail-api:latest
```

<ins>Building From Source</ins>

Alternatively, you can use the provided Dockerfiles to build from source:

Step 1: Clone the Repository

```shell
git clone https://github.com/eggy03/PaperTrail-API-Quarkus.git
cd PaperTrail-API-Quarkus
```

Step 2: Copy your created `.env` file to the repository root

Step 3: Choose your Dockerfile and then build and run

```shell
#either jvm
docker build -f Dockerfile.jvm -t papertrail-api .
#or native
docker build -f Dockerfile.native -t papertrail-api .
#and then
docker run --env-file .env papertrail-api
```

> [!NOTE]
>
> While the above sub-options use `--env-file .env` for examples, you can also pass environment variables directly
> via `docker -e KEY:"VALUE"`
>
> In both the sub-options, the API uses the default port 8080 of the container. This can be overridden by providing
> the environment variable `PORT=<port>`.
>
> It's also worth noting that in both the sub-options, the container port has not been mapped to the host port because
> the API is intended to be used only by the Bot service.
>
> If you need to expose it to your host machine, you can map it via `docker -p <host_port>:<container_port>`

### Option B: Cloud Deployment

Many cloud platforms support Docker-based deployments directly from a repository.

Typically, the process involves:

- Linking the repository
- Selecting the `Dockerfile`
- Supplying the required environment variables

Alternatively, you can deploy using the pre-built container images found in the GitHub Container Registry, if suported.

# Health Check Endpoints

| Endpoint            | Description                            |
|---------------------|----------------------------------------|
| `/q/health/live`    | Application is running                 |
| `/q/health/ready`   | Application is ready to serve requests |
| `/q/health/started` | Application startup has completed      |
| `/q/health`         | Aggregated health status               |

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