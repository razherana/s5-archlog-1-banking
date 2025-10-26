# Banking Change Service - Docker Setup

This directory contains the Docker configuration for the Banking Change service.

## Prerequisites

- Docker
- Docker Compose

## Configuration

The service is configured to run on `127.0.0.5:8080` to maintain consistency with the existing project setup.

### Key Configuration Details: 

- **Base Image**: `tomee:10.1.2-plume` (as requested)
- **Java Version**: OpenJDK 17
- **Ports**:
  - HTTP: `127.0.0.5:8080`
  - Shutdown: `127.0.0.5:8005`
- **TomEE Configuration**: Uses custom `server.xml` and `system.properties` from `src/main/tomee/conf/`

## Usage

### Build and Run

```bash
# Build and start the service
docker-compose up --build

# Run in detached mode
docker-compose up -d --build

# View logs
docker-compose logs -f banking-change

# Stop the service
docker-compose down
```

### Development Workflow

1. Make changes to your Java code
2. Rebuild and restart:

   ```bash
   docker-compose down
   docker-compose up --build
   ```

### Accessing the Service

Once running, the service will be available at:

- **HTTP**: `http://127.0.0.5:8080`
- **Application Context**: `/` (ROOT context)

### Logs

Application logs are mounted to `./logs/` directory for easy access.

### Health Check

The container includes a health check that verifies the service is responding on port 8080.

## Configuration Files Used

- `pom.xml` - Maven configuration and dependencies
- `src/main/tomee/conf/server.xml` - TomEE server configuration
- `src/main/tomee/conf/system.properties` - TomEE system properties
- `src/main/webapp/WEB-INF/web.xml` - Web application configuration

## Environment Variables

The following environment variables are configured based on your `pom.xml`:

- `CATALINA_OPTS`: `-Xmx512m -Dtomee.remote.support=true -Dopenejb.cdi.activated=true -Dtomee.serialization.class.blacklist=- -Dopenejb.system.apps=true`
- `JAVA_OPTS`: `-Djava.awt.headless=true -Dfile.encoding=UTF-8`

## Troubleshooting

### Container won't start

- Check logs: `docker-compose logs banking-change`
- Ensure ports 8080 and 8005 on 127.0.0.5 are not in use

### Build fails

- Make sure Maven dependencies are available
- Check if you need to run `mvn clean install` locally first

### Can't access the service

- Verify the service is running: `docker-compose ps`
- Check health status: `docker inspect banking-change-service`
- Ensure you're accessing `http://127.0.0.5:8080` (not localhost)
