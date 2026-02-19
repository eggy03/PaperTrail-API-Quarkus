# PaperTrail PersistenceAPI
A CRUD API for the PaperTrail Bot

## Documentation
[Apidog](https://papertrail.apidog.io/)

# Self-Hosting Guide
> [!IMPORTANT]
> Please note that this is only for advanced users who have opted for self hosting the bot and it's services
>
> It is recommended that you deploy this service before deploying the [bot](https://github.com/Egg-03/PaperTrailBot?tab=readme-ov-file#self-hosting-guide) itself since the bot relies on the URL of this service to communicate. This service is only required for PaperTrail versions 2.0.0 and above.
>
> This is not a public API and should not be publicly exposed and should be accessible only by the services within the local network. If deploying on platforms like Northflank, Render, or Railway, ensure this service is not exposed externally and is only accessible to the bot service.
> 
> A pre-hosted instance with it's services pre-deployed and configured, is also available if you wish to not opt for self-deployment: https://discord.com/discovery/applications/1381658412550590475
>
> Just invite to your guild and configure it from there.

The following guide shows how to set up the API service

To read the guide on deploying bot, click [here](https://github.com/Egg-03/PaperTrailBot?tab=readme-ov-file#self-hosting-guide)

### Step 1: Set Up Required Services & Get Required Secrets

The Persistence service relies on `Redis/Valkey` and any of the following `Database Systems`: `PostgreSQL`, `MySQL`, `Oracle`, `MS SQL Server`, `MariaDB`

You will need the following environment variables to run the service:

| Variable       | Description                                    |
|----------------|------------------------------------------------|
| `DB_URL`       | JDBC connection string (vendor-specific)       |
| `DB_CLASSNAME` | JDBC driver class name (see below)             |
| `DB_USERNAME`  | Database username                              |
| `DB_PASSWORD`  | Database password                              |
| `REDIS_URL`    | Redis connection string (also supports Valkey) |

Supported JDBC Classnames

- PostgreSQL: org.postgresql.Driver
- MySQL: com.mysql.cj.jdbc.Driver
- Oracle: oracle.jdbc.OracleDriver
- MS SQL Server: com.microsoft.sqlserver.jdbc.SQLServerDriver
- MariaDB: org.mariadb.jdbc.Driver

### Step 2: Deployment Options

Fork this repository to your GitHub account, connect it to your preferred cloud platform, and configure your environment variables in the platform. Some paltform services may also support adding secrets directly from your `.env` file.

#### Cloud Platforms with GitHub + Docker Support
- These can auto-deploy using the included `Dockerfile`

#### Locally
- You can also test it locally by building and running using the `Dockerfile`
- Navigate your terminal to the repository and execute the following commands
  
  ```
  docker build -t persistence-api .
  docker run -p 8081:8081 --env-file .env persistence-api
  ```
  
#### Healthcheck Endpoint

The Persistence API exposes an `/actuator/health` endpoint on port 8081.
This endpoint simply returns 200 OK with a body and is intended for platforms or uptime monitors to check if the API Service is alive.

---

#### Internal

Build native using

```
mvn clean package -Dnative "-Dquarkus.profile=prod" "-Dquarkus.native.additional-build-args=-J-Djava.net.preferIPv4Stack=true"
```


