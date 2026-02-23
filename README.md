# PaperTrail-API-Quarkus

API service for the PaperTrail Bot, built with Quarkus 3 and optimized for native image builds via GraalVM.

# Self-Hosting Guide

> [!NOTE]
> This part is only for users who have opted to self-host the bot and it's services.

> [!TIP]
> While not strictly necessary, you can deploy the service before deploying the
> [bot](https://github.com/Egg-03/PaperTrailBot?tab=readme-ov-file#self-hosting-guide)
> Doing so will make this service's URL available to the bot when being deployed.

> [!WARNING]
> This API does not implement authentication.
> It is intended to run in a private network environment and should only be accessible by the bot service.
> Do not expose it publicly.

## Step 1: Set Up Required Services & Get Required Secrets

### Services Required

| Service               | Version        |
|-----------------------|----------------|
| `Relational Database` | Postgres       |
| `Distributed Cache`   | Redis / Valkey |

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
| `PORT`        | Optional. HTTP port the API will bind to. Defaults to 8081. |

Example `.env`

```dotenv
DB_URL=jdbc:postgresql://localhost:5432/papertrail
DB_USERNAME=postgres
DB_PASSWORD=yourpassword
REDIS_URL=redis://localhost:6379
PORT=8081
```

## Step 2: Deployment Options

### Local

- Clone the Repository

```shell
git clone https://github.com/eggy03/PaperTrail-API-Quarkus.git
cd PaperTrail-API-Quarkus
```

- Keep your `.env` file ready inside the locally cloned repository

- Make sure docker is running and then choose either `Dockerfile.jvm` or `Dockerfile.native` for building

```shell
docker build -f Dockerfile.jvm -t papertrail-api .
docker run -p 8081:8081 --env-file .env papertrail-api
```

If you have used a custom port other than the default 8081, modify the run command and replace it with your port.

### Cloud Based

You can also deploy on cloud platforms that support docker-based deploys via Dockerfile.
The exact procedure varies, but it usually involves linking the repository, choosing the Dockerfile, and supplying the
necessary environment variables.

#### Choosing between JVM and Native

This project provides two Dockerfiles:

- `Dockerfile.jvm` — Runs the service on JVM
- `Dockerfile.native` — Runs the service natively by building native binaries

While native image allows for very low memory usage and very fast application startup times, compared to JVM mode,
it's tradeoffs include very high time and resource consumption during the build stage.

Choose `Dockerfile.native` if:

- You need very fast startup times
- Your environment has limited memory

Choose `Dockerfile.jvm` if:

- You want fast builds
- You are running on a VPS with plenty of memory (≥512MB recommended)

# Health Check Endpoints

The service exposes three major health check endpoints that determine the status of the service.

> /q/health/live - The application is up and running.
>
> /q/health/ready - The application is ready to serve requests.
>
> /q/health/started - The application is started.

Some cloud platforms may require these endpoints to periodically determine the health of your deployed service.

> /q/health - Accumulates all health check procedures in the application.

# License

This API is licensed under the [AGPLv3](/LICENSE) license.

### What this means for you ?

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