# PaperTrail-API-Quarkus

API for the PaperTrail Bot made in Quarkus 3, optimized for native image building via GraalVM


# Self-Hosting Guide
> [!IMPORTANT]
> Please note that this is only for advanced users who have opted for self-hosting the bot and it's services
>
> It is recommended that you deploy this service before deploying the
> [bot](https://github.com/Egg-03/PaperTrailBot?tab=readme-ov-file#self-hosting-guide)
> itself since the bot relies on the URL of this service to communicate.
>
> This API should be accessible only by the services within the local network.
> If deploying on cloud platforms ensure this service is not exposed externally and is only accessible via the bot
> service.
>
> This API is in early stages of development

The following guide shows how to set up the API service

> Coming Soon

# Health Check Endpoints

The service exposes three major health check endpoints that determine the status of the service.

> /q/health/live - The application is up and running.
>
> /q/health/ready - The application is ready to serve requests.
>
> /q/health/started - The application is started.

Some cloud platforms may require these endpoints to periodically determine the health of your deployed service.

> /q/health - Accumulates all health check procedures in the application.