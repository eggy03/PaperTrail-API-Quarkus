# Zombie TXN and high CPU usage Reproducible Build

This is a minimal Quarkus application that reproduces zombie transactions and high CPU usage under load when
`@RunOnVirtualThread` is used on `@Blocking` endpoints in Quarkus 3.36.x and newer.

The issue does not reproduce when the same endpoints are executed on platform threads
or on virtual threads prior to Quarkus 3.36.x

The project contains:

a simple JPA entity
a Panache repository
a transactional service
a REST controller

## Required Environment Variables

```dotenv
DB_URL=jdbc:postgresql://database:5432/dbname
DB_USERNAME=postgres
DB_PASSWORD=yourpassword
PORT=8080(Default)
```

See `env.example` for the complete list

## Running in Docker

```bash
docker build -t papertrail-api .
docker run -d -p HOST_PORT:8080 --cpus="0.15" --memory="512m" --name papertrail-api --env-file .env papertrail-api
```

## Provided Endpoints

#### POST - `/sample`

Request Body

```json
{
  "id": "someLongValue",
  "text": "someText"
}
```

#### DELETE - `/sample/{id}`

## Issue

Limiting the CPU to 0.15 or 0.2 and load testing the endpoints produces warnings like the following and causes CPU
spikes

```text
2026-06-23 00:07:45.456 | __  ____  __  _____   ___  __ ____  ______ 
2026-06-23 00:07:45.456 |  --/ __ \/ / / / _ | / _ \/ //_/ / / / __/ 
2026-06-23 00:07:45.456 |  -/ /_/ / /_/ / __ |/ , _/ ,< / /_/ /\ \   
2026-06-23 00:07:45.456 | --\___\_\____/_/ |_/_/|_/_/|_|\____/___/   
2026-06-23 00:07:45.456 | 2026-06-22 18:37:45,453 INFO  [org.flywaydb.core.FlywayExecutor] (main) Database: jdbc:postgresql://papertrail-staging-pg-s1mple-logger.j.aivencloud.com:22716/defaultdb?ssl=require (PostgreSQL 17.10)
2026-06-23 00:07:46.254 | 2026-06-22 18:37:46,253 INFO  [org.flywaydb.core.internal.command.DbValidate] (main) Successfully validated 2 migrations (execution time 00:00.286s)
2026-06-23 00:07:47.262 | 2026-06-22 18:37:47,262 INFO  [org.flywaydb.core.internal.command.DbMigrate] (main) Current version of schema "sampleschema": 1
2026-06-23 00:07:47.352 | 2026-06-22 18:37:47,351 INFO  [org.flywaydb.core.internal.command.DbMigrate] (main) Schema "sampleschema" is up to date. No migration necessary.
2026-06-23 00:07:51.559 | 2026-06-22 18:37:51,559 INFO  [io.quarkus] (main) zombie-txn-reproducer 0.0.1 on JVM (powered by Quarkus 3.36.3) started in 26.754s. Listening on: http://0.0.0.0:8081
2026-06-23 00:07:51.559 | 2026-06-22 18:37:51,559 INFO  [io.quarkus] (main) Profile prod activated. 
2026-06-23 00:07:51.559 | 2026-06-22 18:37:51,559 INFO  [io.quarkus] (main) Installed features: [agroal, cdi, flyway, hibernate-orm, hibernate-orm-panache, hibernate-validator, jdbc-postgresql, narayana-jta, rest, rest-jackson, smallrye-context-propagation, smallrye-fault-tolerance, smallrye-health, vertx]
2026-06-23 00:08:35.053 | 2026-06-22 18:38:34,448 WARN  [io.vertx.core.impl.BlockedThreadChecker] (vertx-blocked-thread-checker) Thread Thread[vert.x-eventloop-thread-1,5,main] has been blocked for 2639 ms, time limit is 2000 ms: io.vertx.core.VertxException: Thread blocked
2026-06-23 00:08:35.254 | 2026-06-22 18:38:35,165 WARN  [io.vertx.core.impl.BlockedThreadChecker] (vertx-blocked-thread-checker) Thread Thread[vert.x-eventloop-thread-0,5,main] has been blocked for 2639 ms, time limit is 2000 ms: io.vertx.core.VertxException: Thread blocked

2026-06-23 00:09:42.346 | 2026-06-22 18:39:42,247 WARN  [com.arjuna.ats.arjuna] (Transaction Reaper) ARJUNA012117: TransactionReaper::check processing TX 0:ffffac110002:a771:6a398131:b in state  RUN
2026-06-23 00:09:42.347 | 2026-06-22 18:39:42,346 WARN  [com.arjuna.ats.arjuna] (Transaction Reaper) ARJUNA012117: TransactionReaper::check processing TX 0:ffffac110002:a771:6a398131:5 in state  RUN
2026-06-23 00:09:42.446 | 2026-06-22 18:39:42,349 WARN  [com.arjuna.ats.arjuna] (Transaction Reaper Worker 0) ARJUNA012095: Abort of action id 0:ffffac110002:a771:6a398131:b invoked while multiple threads active within it.
2026-06-23 00:09:42.446 | 2026-06-22 18:39:42,446 WARN  [com.arjuna.ats.arjuna] (Transaction Reaper) ARJUNA012117: TransactionReaper::check processing TX 0:ffffac110002:a771:6a398131:c in state  RUN
2026-06-23 00:09:42.450 | 2026-06-22 18:39:42,449 WARN  [com.arjuna.ats.arjuna] (Transaction Reaper) ARJUNA012117: TransactionReaper::check processing TX 0:ffffac110002:a771:6a398131:d in state  RUN
2026-06-23 00:09:42.545 | 2026-06-22 18:39:42,451 WARN  [com.arjuna.ats.arjuna] (Transaction Reaper) ARJUNA012117: TransactionReaper::check processing TX 0:ffffac110002:a771:6a398131:e in state  RUN
2026-06-23 00:09:42.545 | 2026-06-22 18:39:42,544 WARN  [com.arjuna.ats.arjuna] (Transaction Reaper) ARJUNA012117: TransactionReaper::check processing TX 0:ffffac110002:a771:6a398131:f in state  RUN
2026-06-23 00:09:42.646 | 2026-06-22 18:39:42,645 WARN  [com.arjuna.ats.arjuna] (Transaction Reaper) ARJUNA012117: TransactionReaper::check processing TX 0:ffffac110002:a771:6a398131:10 in state  RUN
2026-06-23 00:09:42.847 | 2026-06-22 18:39:42,846 WARN  [com.arjuna.ats.arjuna] (Transaction Reaper) ARJUNA012117: TransactionReaper::check processing TX 0:ffffac110002:a771:6a398131:b in state  CANCEL
2026-06-23 00:09:42.858 | 2026-06-22 18:39:42,858 WARN  [com.arjuna.ats.arjuna] (Transaction Reaper) ARJUNA012378: ReaperElement appears to be wedged: java.base/java.util.stream.Stream.iterate(Unknown Source)
2026-06-23 00:09:42.945 | 2026-06-22 18:39:42,858 WARN  [com.arjuna.ats.arjuna] (Transaction Reaper) ARJUNA012117: TransactionReaper::check processing TX 0:ffffac110002:a771:6a398131:5 in state  SCHEDULE_CANCEL
2026-06-23 00:09:42.947 | 2026-06-22 18:39:42,946 WARN  [com.arjuna.ats.arjuna] (Transaction Reaper Worker 0) ARJUNA012381: Action id 0:ffffac110002:a771:6a398131:b completed with multiple threads - thread quarkus-virtual-thread-31 was in progress with java.base/java.util.concurrent.ConcurrentHashMap.computeIfAbsent(Unknown Source)

2026-06-23 00:09:43.044 | 2026-06-22 18:39:42,947 WARN  [com.arjuna.ats.arjuna] (Transaction Reaper) ARJUNA012117: TransactionReaper::check processing TX 0:ffffac110002:a771:6a398131:c in state  SCHEDULE_CANCEL
2026-06-23 00:09:43.071 | 2026-06-22 18:39:43,071 WARN  [com.arjuna.ats.arjuna] (Transaction Reaper) ARJUNA012117: TransactionReaper::check processing TX 0:ffffac110002:a771:6a398131:d in state  SCHEDULE_CANCEL
2026-06-23 00:09:43.072 | 2026-06-22 18:39:43,044 WARN  [com.arjuna.ats.arjuna] (Transaction Reaper Worker 0) ARJUNA012108: CheckedAction::check - atomic action 0:ffffac110002:a771:6a398131:b aborting with 1 threads active!
2026-06-23 00:09:43.072 | 2026-06-22 18:39:43,071 WARN  [com.arjuna.ats.arjuna] (Transaction Reaper) ARJUNA012117: TransactionReaper::check processing TX 0:ffffac110002:a771:6a398131:e in state  SCHEDULE_CANCEL
2026-06-23 00:09:43.072 | 2026-06-22 18:39:43,072 WARN  [com.arjuna.ats.arjuna] (Transaction Reaper) ARJUNA012117: TransactionReaper::check processing TX 0:ffffac110002:a771:6a398131:f in state  SCHEDULE_CANCEL
2026-06-23 00:09:43.072 | 2026-06-22 18:39:43,072 WARN  [com.arjuna.ats.arjuna] (Transaction Reaper Worker 0) ARJUNA012095: Abort of action id 0:ffffac110002:a771:6a398131:c invoked while multiple threads active within it.
2026-06-23 00:09:43.074 | 2026-06-22 18:39:43,073 WARN  [com.arjuna.ats.arjuna] (Transaction Reaper Worker 0) ARJUNA012381: Action id 0:ffffac110002:a771:6a398131:c completed with multiple threads - thread quarkus-virtual-thread-32 was in progress with java.base/java.util.concurrent.ConcurrentHashMap.computeIfAbsent(Unknown Source)

2026-06-23 00:09:43.074 | 2026-06-22 18:39:43,074 WARN  [com.arjuna.ats.arjuna] (Transaction Reaper Worker 0) ARJUNA012108: CheckedAction::check - atomic action 0:ffffac110002:a771:6a398131:c aborting with 1 threads active!
2026-06-23 00:09:43.075 | 2026-06-22 18:39:43,074 WARN  [com.arjuna.ats.arjuna] (Transaction Reaper Worker 0) ARJUNA012121: TransactionReaper::doCancellations worker Thread[#121,Transaction Reaper Worker 0,5,VirtualThreads] successfully canceled TX 0:ffffac110002:a771:6a398131:c
2026-06-23 00:09:43.144 | 2026-06-22 18:39:43,075 WARN  [com.arjuna.ats.arjuna] (Transaction Reaper Worker 0) ARJUNA012095: Abort of action id 0:ffffac110002:a771:6a398131:d invoked while multiple threads active within it.
2026-06-23 00:09:43.147 | 2026-06-22 18:39:43,146 WARN  [com.arjuna.ats.arjuna] (Transaction Reaper Worker 0) ARJUNA012381: Action id 0:ffffac110002:a771:6a398131:d completed with multiple threads - thread quarkus-virtual-thread-36 was in progress with java.base/java.lang.VirtualThread.parkNanos(Unknown Source)

2026-06-23 00:09:43.147 | 2026-06-22 18:39:43,146 WARN  [com.arjuna.ats.arjuna] (Transaction Reaper) ARJUNA012117: TransactionReaper::check processing TX 0:ffffac110002:a771:6a398131:10 in state  SCHEDULE_CANCEL
2026-06-23 00:09:43.147 | 2026-06-22 18:39:43,147 WARN  [com.arjuna.ats.arjuna] (Transaction Reaper Worker 0) ARJUNA012108: CheckedAction::check - atomic action 0:ffffac110002:a771:6a398131:d aborting with 1 threads active!
2026-06-23 00:09:43.247 | 2026-06-22 18:39:43,246 WARN  [org.hibernate.orm.jta] (Transaction Reaper Worker 0) HHH90007020: Transaction afterCompletion called by a background thread; delaying afterCompletion processing until the original thread can handle it. [status=4]
2026-06-23 00:09:43.247 | 2026-06-22 18:39:43,247 WARN  [com.arjuna.ats.arjuna] (Transaction Reaper Worker 0) ARJUNA012121: TransactionReaper::doCancellations worker Thread[#121,Transaction Reaper Worker 0,5,VirtualThreads] successfully canceled TX 0:ffffac110002:a771:6a398131:d
2026-06-23 00:09:43.247 | 2026-06-22 18:39:43,247 WARN  [com.arjuna.ats.arjuna] (Transaction Reaper Worker 0) ARJUNA012095: Abort of action id 0:ffffac110002:a771:6a398131:e invoked while multiple threads active within it.
2026-06-23 00:09:43.249 | 2026-06-22 18:39:43,248 WARN  [com.arjuna.ats.arjuna] (Transaction Reaper Worker 0) ARJUNA012381: Action id 0:ffffac110002:a771:6a398131:e completed with multiple threads - thread quarkus-virtual-thread-39 was in progress with java.base/java.lang.VirtualThread.parkNanos(Unknown Source)

2026-06-23 00:09:43.250 | 2026-06-22 18:39:43,249 WARN  [com.arjuna.ats.arjuna] (Transaction Reaper Worker 0) ARJUNA012108: CheckedAction::check - atomic action 0:ffffac110002:a771:6a398131:e aborting with 1 threads active!
2026-06-23 00:09:43.250 | 2026-06-22 18:39:43,249 WARN  [org.hibernate.orm.jta] (Transaction Reaper Worker 0) HHH90007020: Transaction afterCompletion called by a background thread; delaying afterCompletion processing until the original thread can handle it. [status=4]
2026-06-23 00:09:43.250 | 2026-06-22 18:39:43,249 WARN  [com.arjuna.ats.arjuna] (Transaction Reaper Worker 0) ARJUNA012121: TransactionReaper::doCancellations worker Thread[#121,Transaction Reaper Worker 0,5,VirtualThreads] successfully canceled TX 0:ffffac110002:a771:6a398131:e
2026-06-23 00:09:43.250 | 2026-06-22 18:39:43,250 WARN  [com.arjuna.ats.arjuna] (Transaction Reaper Worker 0) ARJUNA012095: Abort of action id 0:ffffac110002:a771:6a398131:f invoked while multiple threads active within it.
2026-06-23 00:09:43.345 | 2026-06-22 18:39:43,251 WARN  [com.arjuna.ats.arjuna] (Transaction Reaper Worker 0) ARJUNA012381: Action id 0:ffffac110002:a771:6a398131:f completed with multiple threads - thread quarkus-virtual-thread-41 was in progress with java.base/java.lang.VirtualThread.parkNanos(Unknown Source)
```

I could not reproduce the issue without limiting the CPU to 0.15 or 0.2. I also haven't tested for other CPU values.