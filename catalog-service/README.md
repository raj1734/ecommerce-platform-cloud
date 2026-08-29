# Catalog Service - Switchable Repository and Cache

This version supports four combinations without changing Java code:

| Repository | Cache | Environment |
|---|---|---|
| memory | memory | `CATALOG_REPOSITORY_TYPE=memory`, `CATALOG_CACHE_TYPE=memory` |
| mongo | memory | `CATALOG_REPOSITORY_TYPE=mongo`, `CATALOG_CACHE_TYPE=memory` |
| memory | redis | `CATALOG_REPOSITORY_TYPE=memory`, `CATALOG_CACHE_TYPE=redis` |
| mongo | redis | `CATALOG_REPOSITORY_TYPE=mongo`, `CATALOG_CACHE_TYPE=redis` |

## Local with no MongoDB and no Redis

PowerShell:

```powershell
$env:CATALOG_REPOSITORY_TYPE="memory"
$env:CATALOG_CACHE_TYPE="memory"
mvn spring-boot:run
```

Or leave both unset; `memory + memory` is the default.

The memory repository uses `ConcurrentHashMap` and sample products are loaded on startup. Data is lost when the service restarts.

## MongoDB only

```powershell
$env:CATALOG_REPOSITORY_TYPE="mongo"
$env:CATALOG_CACHE_TYPE="memory"
$env:SPRING_DATA_MONGODB_URI="mongodb://localhost:27017/catalog_db"
mvn spring-boot:run
```

## MongoDB + Redis

```powershell
$env:CATALOG_REPOSITORY_TYPE="mongo"
$env:CATALOG_CACHE_TYPE="redis"
$env:SPRING_DATA_MONGODB_URI="mongodb://localhost:27017/catalog_db"
$env:SPRING_REDIS_HOST="localhost"
$env:SPRING_REDIS_PORT="6379"
mvn spring-boot:run
```

## Important

When repository type is `memory`, the MongoDB auto-configurations and Mongo health contributor are excluded. When cache type is `memory`, Redis auto-configurations and Redis health contributor are excluded. Therefore `memory + memory` does not attempt to connect to localhost:27017 or localhost:6379.
