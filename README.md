# Spring Boot AWS S3 / MinIO Storage Microservice

![banner](./assets/banner.jpg)

A production-ready Spring Boot microservice for object storage operations, supporting both AWS S3 and MinIO with enterprise-grade multi-tenancy features, full API documentation, health checks, and monitoring.

## Table of Contents

1. [Features](#features)
2. [Prerequisites](#prerequisites)
3. [Quick Start](#quick-start)
4. [Architecture](#architecture)
5. [Configuration](#configuration)
6. [Multi-Tenancy Features](#multi-tenancy-features)
7. [API Documentation](#api-documentation)
8. [Security Features](#security-features)
9. [Deployment](#deployment)
10. [Health & Monitoring](#health--monitoring)
11. [Testing](#testing)
12. [Best Practices](#best-practices)

---

## Features

### Core Features
- ✅ **Multi-provider support**: AWS S3 and MinIO
- ✅ **Spring Boot 3.5.9** with Java 17
- ✅ **AWS SDK v2** (latest async-capable version)
- ✅ **RESTful API** with versioning (`/api/v1`)
- ✅ **OpenAPI/Swagger documentation**
- ✅ **Health checks** via Spring Boot Actuator
- ✅ **Metrics & monitoring** (Prometheus-ready)
- ✅ **Global exception handling**
- ✅ **Input validation** on all endpoints
- ✅ **DTOs** for clean request/response contracts
- ✅ **Profile-based configuration** (s3, minio)
- ✅ **Docker support** with multi-stage builds

### Enterprise Multi-Tenancy Features
- ✅ **Multi-client/tenant support** with isolated storage
- ✅ **Three bucket strategies** (shared, per-client, per-environment)
- ✅ **Environment-based organization** (dev, staging, prod, test, UAT)
- ✅ **Path security** with advanced sanitization
- ✅ **Five duplicate file strategies** (UUID, timestamp, versioning, overwrite, reject)
- ✅ **Storage quotas** per client with enforcement
- ✅ **Public API** - No authentication required
- ✅ **Auto-bucket creation** when needed

---

## Prerequisites

- Java 17 or higher
- Maven 3.9+
- Docker & Docker Compose (for local MinIO)
- AWS Account (for S3) or MinIO instance
- Your favorite IDE or CLI

---

## Quick Start

### Option 1: Local Development with MinIO

1. **Start MinIO using Docker Compose**:
```bash
docker-compose up -d
```

This will start:
- MinIO server on `http://localhost:9000`
- MinIO console on `http://localhost:9001`
- Auto-creates a `test-bucket`

2. **Run the application**:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=minio
```

3. **Access the application**:
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Health Check: http://localhost:8080/actuator/health
- MinIO Console: http://localhost:9001 (minioadmin/minioadmin)

### Option 2: AWS S3

1. **Set AWS credentials**:
```bash
export AWS_ACCESS_KEY_ID=your_access_key
export AWS_SECRET_ACCESS_KEY=your_secret_key
export AWS_REGION=us-east-1
```

2. **Run with S3 profile**:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=s3
```

---

## Architecture

### Project Structure

```
src/main/java/br/com/example/davidarchanjo/
├── application/          # Main Spring Boot application
├── config/               # Configuration classes
│   ├── AwsS3ClientConfig.java       # S3 client configuration
│   ├── StorageProperties.java       # Configuration properties
│   └── WebMvcConfig.java            # Web MVC config (CORS)
├── controller/           # REST API controllers
│   └── S3BucketStorageController.java
├── dto/                  # Data Transfer Objects
│   ├── ErrorResponse.java
│   ├── FileListResponse.java
│   ├── FileUploadResponse.java
│   ├── ClientBucketMappingDTO.java
│   └── PublicDownloadResponse.java
├── enumeration/          # Enums
│   ├── FileMediaType.java
│   ├── BucketStrategy.java
│   └── DuplicateFileStrategy.java
├── exception/            # Custom exceptions
│   ├── StorageException.java
│   ├── FileNotFoundException.java
│   ├── BucketNotFoundException.java
│   ├── FileUploadException.java
│   └── GlobalExceptionHandler.java
├── health/               # Health check indicators
│   └── StorageHealthIndicator.java
├── model/                # Domain models
│   └── Client.java      # Client/tenant entity
├── security/             # Security components
│   └── ApiKeyAuthenticationInterceptor.java
├── service/              # Business logic layer
│   ├── S3BucketStorageService.java
│   ├── BucketManagementService.java
│   ├── ClientService.java
│   └── impl/
│       ├── S3BucketStorageServiceImpl.java
│       ├── BucketManagementServiceImpl.java
│       └── ClientServiceImpl.java
└── util/                 # Utility classes
    ├── PathSanitizer.java
    └── FileNameGenerator.java
```

### Multi-Tenancy Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Client Request                          │
│              (with API Key + Client ID)                     │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│          Controller (S3BucketStorageController)             │
│  • Validates API Key                                        │
│  • Checks client permissions                                │
│  • Sanitizes paths                                          │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│              BucketManagementService                        │
│  • Determines bucket name based on strategy                 │
│  • Builds object keys with prefixes                         │
│  • Creates buckets if needed                                │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│              S3BucketStorageService                         │
│  • Handles duplicate files                                  │
│  • Enforces quotas                                          │
│  • Performs S3 operations                                   │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                   S3 / MinIO Storage                        │
└─────────────────────────────────────────────────────────────┘
```

---

## Configuration

### Spring Profiles

Two Spring profiles are available:

1. **s3** - AWS S3 (default)
2. **minio** - MinIO

### Basic Configuration Properties

| Property | Description | Default |
|----------|-------------|---------|
| `storage.provider` | Provider type (s3/minio) | s3 |
| `storage.region` | AWS region | us-east-1 |
| `storage.endpoint-url` | Custom endpoint (required for MinIO) | - |
| `storage.access-key` | Access key ID | - |
| `storage.secret-key` | Secret access key | - |
| `storage.path-style-access` | Enable path-style access | false |
| `storage.max-file-size-mb` | Max file size in MB | 10 |

### Multi-Tenancy Configuration

```yaml
storage:
  # Basic Configuration
  provider: minio
  region: us-east-1
  endpoint-url: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  path-style-access: true
  max-file-size-mb: 10

  # Multi-Tenancy Configuration
  multi-tenancy-enabled: true
  bucket-strategy: SHARED_WITH_PREFIX
  shared-bucket: shared-storage
  bucket-suffix: storage
  auto-create-buckets: true
  duplicate-file-strategy: UUID_SUFFIX

  # Security
  require-api-key: false  # Set to true in production
  enforce-quota: true
```

### Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Active profile | s3, minio |
| `SERVER_PORT` | Server port | 8080 |
| `STORAGE_PROVIDER` | Storage provider | s3, minio |
| `STORAGE_REGION` | Storage region | us-east-1 |
| `STORAGE_ENDPOINT_URL` | Custom endpoint | http://localhost:9000 |
| `STORAGE_ACCESS_KEY` | Access key | minioadmin |
| `STORAGE_SECRET_KEY` | Secret key | minioadmin |
| `STORAGE_BUCKET_STRATEGY` | Bucket strategy | SHARED_WITH_PREFIX |
| `STORAGE_REQUIRE_API_KEY` | Enable API key auth | true/false |

---

## Multi-Tenancy Features

### Bucket Strategies

#### 1. SHARED_WITH_PREFIX (Default)
All clients share a single bucket with client/environment prefixes.

**Structure:**
```
shared-storage/
├── client-001/
│   ├── dev/files/
│   ├── staging/files/
│   └── prod/files/
└── client-002/
    └── dev/files/
```

**Pros:**
- ✅ Single bucket to manage
- ✅ Cost-effective
- ✅ Easy to monitor

**Configuration:**
```yaml
storage:
  bucket-strategy: SHARED_WITH_PREFIX
  shared-bucket: shared-storage
```

#### 2. PER_CLIENT
Each client gets their own dedicated bucket.

**Structure:**
```
client-001-storage/
├── dev/files/
├── staging/files/
└── prod/files/

client-002-storage/
└── dev/files/
```

**Pros:**
- ✅ Complete client isolation
- ✅ Easy to apply bucket-level policies
- ✅ Can delete all client data by deleting bucket

**Configuration:**
```yaml
storage:
  bucket-strategy: PER_CLIENT
  bucket-suffix: storage
```

#### 3. PER_CLIENT_PER_ENVIRONMENT
Separate bucket for each client AND environment combination.

**Structure:**
```
client-001-dev-storage/files/
client-001-staging-storage/files/
client-001-prod-storage/files/
client-002-dev-storage/files/
```

**Pros:**
- ✅ Maximum isolation
- ✅ Easy environment-specific policies
- ✅ Can replicate prod to staging easily

**Configuration:**
```yaml
storage:
  bucket-strategy: PER_CLIENT_PER_ENVIRONMENT
  bucket-suffix: storage
```

### Client Management

#### Demo Clients

| Client ID | API Key | Quota | Environments |
|-----------|---------|-------|--------------|
| client-001 | demo-api-key-001 | 1GB | dev, staging, prod |
| client-002 | demo-api-key-002 | 500MB | dev, staging |
| client-003 | enterprise-api-key-003 | Unlimited | all |

#### Client Features

1. **Storage Quotas**
   - Per-client storage limits
   - Auto-tracked usage
   - Upload rejection when exceeded

2. **Environment Restrictions**
   - Clients can be limited to specific environments
   - Prevents unauthorized prod access

3. **Custom Buckets**
   - Override default bucket strategy
   - Client-specific bucket names

### Environment Support

Available environments:
- **DEVELOPMENT** (dev)
- **STAGING** (staging)
- **PRODUCTION** (prod)
- **TEST** (test)
- **UAT** (uat)

### Duplicate File Handling Strategies

#### 1. UUID_SUFFIX (Default)
```
document.pdf → document_a1b2c3d4.pdf
```

#### 2. TIMESTAMP_SUFFIX
```
document.pdf → document_20251230_103045.pdf
```

#### 3. VERSION
```
document.pdf → document_v2.pdf, document_v3.pdf
```

#### 4. OVERWRITE
```
Replaces existing file
```

#### 5. REJECT
```
Returns 409 Conflict if file exists
```

---

## API Documentation

### Swagger UI
Access interactive API documentation at: **http://localhost:8080/swagger-ui.html**

### API Endpoints

#### Base URL: `/api/v1/storage`

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/{bucketName}` | List all files in bucket |
| POST | `/{bucketName}/upload` | Upload a file |
| GET | `/{bucketName}/download/{fileName}` | Download a file |
| DELETE | `/{bucketName}/{fileName}` | Delete a file |

#### Multi-Tenant Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/{clientId}/{environment}` | List files for client/env |
| POST | `/{clientId}/{environment}/upload` | Upload file (requires API key) |
| GET | `/{clientId}/{environment}/download/{fileName}` | Download file (public) |
| DELETE | `/{clientId}/{environment}/{fileName}` | Delete file (requires API key) |

### Example Requests

#### Upload File (Basic)
```bash
curl -X POST http://localhost:8080/api/v1/storage/test-bucket/upload \
  -F "file=@/path/to/file.pdf" \
  -F "fileName=document.pdf"
```

#### Upload File (Multi-Tenant)
```bash
curl -X POST http://localhost:8080/api/v1/storage/client-001/dev/upload \
  -H "X-API-Key: demo-api-key-001" \
  -F "file=@document.pdf" \
  -F "fileName=report.pdf" \
  -F "directory=reports/2025"
```

**Result:**
- Client: client-001
- Environment: dev
- Directory: reports/2025
- Final path: `client-001/dev/reports/2025/report.pdf`

#### List Files
```bash
# Basic
curl http://localhost:8080/api/v1/storage/test-bucket

# Multi-tenant (development)
curl http://localhost:8080/api/v1/storage/client-001/dev

# With directory filter
curl http://localhost:8080/api/v1/storage/client-001/prod?directory=reports/2025
```

#### Download File
```bash
curl -X GET \
  "http://localhost:8080/api/v1/storage/client-001/prod/download/report.pdf?directory=reports/2025" \
  --output report.pdf
```

#### Delete File
```bash
curl -X DELETE \
  "http://localhost:8080/api/v1/storage/client-001/dev/report.pdf?directory=reports/2025" \
  -H "X-API-Key: demo-api-key-001"
```

---

## Security Features

### 1. Path Sanitization

The `PathSanitizer` utility prevents security vulnerabilities:

**Blocks:**
- ❌ Path traversal (`../../../etc/passwd`)
- ❌ Absolute paths (`/etc/passwd`, `C:\Windows`)
- ❌ Invalid characters (`<`, `>`, `:`, `|`, `?`, `*`)
- ❌ Windows reserved names (`CON`, `PRN`, `AUX`, `NUL`)

**Allows:**
- ✅ Valid relative paths (`documents/file.pdf`)
- ✅ Nested directories (`2025/invoices/inv1.pdf`)

**Examples:**
```java
// BLOCKED
uploadFile("../../etc/passwd")       // Path traversal
uploadFile("/absolute/path")          // Absolute path
uploadFile("file<>name.txt")          // Invalid chars

// ALLOWED
uploadFile("documents/file.pdf")      // Valid path
uploadFile("2025/invoices/inv1.pdf")  // Nested dirs
```

### 2. API Key Authentication

**Request Header:**
```http
X-API-Key: demo-api-key-001
```

**Validation:**
- Checks if API key exists
- Verifies client is active
- Validates environment access
- Checks storage quota

### 3. Storage Quotas

Automatically enforced per client:
```java
if (client.hasExceededQuota()) {
    throw new StorageException("Storage quota exceeded");
}
```

### 4. Global Exception Handling

All errors return standardized JSON responses:

```json
{
  "timestamp": "2025-12-30T10:15:30",
  "status": 404,
  "message": "File 'document.pdf' not found in bucket 'test-bucket'",
  "path": "/api/v1/storage/test-bucket/download/document.pdf"
}
```

#### Error Codes

| Status | Description |
|--------|-------------|
| 400 | Bad Request - Validation failed |
| 404 | Not Found - File or bucket not found |
| 409 | Conflict - Duplicate file (REJECT strategy) |
| 413 | Payload Too Large - File size exceeds limit |
| 500 | Internal Server Error - Storage operation failed |

---

## Deployment

### Option 1: Docker Deployment (Recommended)

#### Build and Run
```bash
# Build the image
docker-compose build

# Start the service
docker-compose up -d

# Check logs
docker-compose logs -f storage-gateway

# Check health
curl http://localhost:8080/actuator/health
```

#### Environment Configuration

Create `.env` file:
```env
# MinIO Configuration
SPRING_PROFILES_ACTIVE=production
STORAGE_PROVIDER=minio
STORAGE_ENDPOINT_URL=http://your-minio-server.com:9000
STORAGE_ACCESS_KEY=your-access-key
STORAGE_SECRET_KEY=your-secret-key
STORAGE_PATH_STYLE_ACCESS=true

# AWS S3 Configuration (alternative)
# STORAGE_PROVIDER=s3
# STORAGE_REGION=us-east-1
# STORAGE_ACCESS_KEY=AKIAIOSFODNN7EXAMPLE
# STORAGE_SECRET_KEY=wJalrXUt...
# STORAGE_PATH_STYLE_ACCESS=false

# Multi-Tenancy
STORAGE_BUCKET_STRATEGY=SHARED_WITH_PREFIX
STORAGE_SHARED_BUCKET=production-storage
STORAGE_REQUIRE_API_KEY=true
STORAGE_ENFORCE_QUOTA=true
```

### Option 2: Kubernetes Deployment

**deployment.yaml:**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: storage-gateway
  namespace: storage
spec:
  replicas: 3
  selector:
    matchLabels:
      app: storage-gateway
  template:
    metadata:
      labels:
        app: storage-gateway
    spec:
      containers:
      - name: storage-gateway
        image: your-registry/storage-gateway:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        - name: STORAGE_PROVIDER
          value: "s3"
        - name: STORAGE_REGION
          value: "us-east-1"
        - name: STORAGE_ACCESS_KEY
          valueFrom:
            secretKeyRef:
              name: s3-credentials
              key: access-key
        - name: STORAGE_SECRET_KEY
          valueFrom:
            secretKeyRef:
              name: s3-credentials
              key: secret-key
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: storage-gateway
  namespace: storage
spec:
  selector:
    app: storage-gateway
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
  type: LoadBalancer
```

**Deploy:**
```bash
# Create namespace
kubectl create namespace storage

# Apply manifests
kubectl apply -f deployment.yaml

# Check status
kubectl -n storage get pods
kubectl -n storage logs -f deployment/storage-gateway
```

### Option 3: Standalone JAR Deployment

**Build and Run:**
```bash
# Build the JAR
mvn clean package -DskipTests

# Set environment variables
export STORAGE_ACCESS_KEY=your-access-key
export STORAGE_SECRET_KEY=your-secret-key

# Run
java -jar target/spring-boot-aws-s3-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=production
```

**Run as systemd service:**
```ini
[Unit]
Description=S3/MinIO Storage Gateway
After=network.target

[Service]
Type=simple
User=storage
WorkingDirectory=/opt/storage-gateway
ExecStart=/usr/bin/java -jar /opt/storage-gateway/app.jar --spring.profiles.active=production
Restart=on-failure
RestartSec=10

Environment="STORAGE_PROVIDER=s3"
Environment="STORAGE_ACCESS_KEY=your-key"
Environment="STORAGE_SECRET_KEY=your-secret"

[Install]
WantedBy=multi-user.target
```

```bash
# Enable and start
sudo systemctl daemon-reload
sudo systemctl enable storage-gateway
sudo systemctl start storage-gateway
sudo systemctl status storage-gateway
```

### Scaling

The service is **stateless** and can be scaled horizontally:

```bash
# Docker Compose
docker-compose up -d --scale storage-gateway=3

# Kubernetes
kubectl -n storage scale deployment/storage-gateway --replicas=5
```

---

## Health & Monitoring

### Health Checks

```bash
# Overall health
curl http://localhost:8080/actuator/health

# Storage connection health
curl http://localhost:8080/actuator/health/storage

# Readiness probe
curl http://localhost:8080/actuator/health/readiness

# Liveness probe
curl http://localhost:8080/actuator/health/liveness
```

**Response:**
```json
{
  "status": "UP",
  "components": {
    "storageHealthIndicator": {
      "status": "UP",
      "details": {
        "provider": "minio",
        "region": "us-east-1",
        "endpoint": "http://localhost:9000",
        "status": "Connection successful"
      }
    }
  }
}
```

### Metrics

```bash
# All metrics
curl http://localhost:8080/actuator/metrics

# Specific metric
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# Prometheus format
curl http://localhost:8080/actuator/prometheus
```

### Recommended Monitoring Setup

1. **Prometheus** for metrics collection
2. **Grafana** for visualization
3. **ELK Stack** for log aggregation
4. **AWS CloudWatch** (for S3 deployments)
5. **Distributed tracing** with Spring Cloud Sleuth + Zipkin

---

## Testing

### Local Testing

1. **Clone the repository**:
```bash
git clone https://github.com/davidarchanjo/spring-boot-aws-s3
cd spring-boot-aws-s3
```

2. **Start MinIO**:
```bash
docker-compose up -d
```

3. **Run the application**:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=minio
```

4. **Test using Swagger UI**:
Navigate to http://localhost:8080/swagger-ui.html

5. **Test using cURL**:
See [API Documentation](#api-documentation) for examples

6. **Test using Postman**:
Import the collection from `assets/` directory

### Multi-Tenancy Testing Checklist

- [ ] Test with demo API keys
- [ ] Upload files to different environments
- [ ] Try path traversal attacks (should be blocked)
- [ ] Test duplicate file handling
- [ ] Verify storage quota enforcement
- [ ] Test all three bucket strategies
- [ ] Upload files with directories
- [ ] Test file download/delete
- [ ] Check bucket auto-creation
- [ ] Monitor storage usage updates

---

## Best Practices

### Production Security Checklist

- [ ] **Enable API Key Authentication**
  ```env
  STORAGE_REQUIRE_API_KEY=true
  ```

- [ ] **Use HTTPS/TLS**
  - Deploy behind reverse proxy (Nginx, Traefik)
  - Configure SSL certificates

- [ ] **Secure Credentials**
  - Never commit `.env` files
  - Use secrets management (AWS Secrets Manager, Vault)
  - Rotate keys regularly

- [ ] **Enable Quotas**
  ```env
  STORAGE_ENFORCE_QUOTA=true
  ```

- [ ] **Configure CORS Appropriately**
  - Update `WebMvcConfig.java` with specific origins

- [ ] **Monitor and Log**
  - Enable application logging
  - Configure log aggregation
  - Set up alerts

- [ ] **Use IAM Roles (AWS)**
  - Prefer IAM roles over static keys
  - Remove access/secret keys from environment

- [ ] **Enable S3 Features**
  - Bucket versioning
  - Lifecycle policies
  - Server-side encryption
  - Cross-region replication

### Bucket Strategy Selection

**Use SHARED_WITH_PREFIX when:**
- You have many clients (> 100)
- Cost optimization is important
- Simple access patterns

**Use PER_CLIENT when:**
- Strong client isolation required
- Different retention policies per client
- Compliance requirements

**Use PER_CLIENT_PER_ENVIRONMENT when:**
- Maximum isolation needed
- Environment-specific replication
- Separate backup strategies

### Directory Organization

**Recommended structure:**
```
client-id/environment/category/year/month/filename
```

**Example:**
```
client-001/prod/invoices/2025/01/invoice-001.pdf
client-001/dev/documents/contracts/contract-v1.pdf
```

### File Naming Best Practices

**Good:**
- ✅ `invoice-2025-01-15.pdf`
- ✅ `user-profile-picture.jpg`
- ✅ `contract-v2-signed.pdf`

**Bad:**
- ❌ `Invoice (1).pdf`
- ❌ `../../../file.pdf`
- ❌ `file?.txt`

---

## Troubleshooting

### Common Issues

**1. Bucket not found**
```
Solution: Enable auto-create-buckets: true
Or manually create buckets before use
```

**2. Path traversal error**
```
Error: "Invalid path: path traversal detected"
Solution: Don't use ../ in paths, use only relative paths
```

**3. Storage quota exceeded**
```
Error: "Storage quota exceeded for client"
Solution: Increase client quota or clean up old files
```

**4. API key invalid**
```
Error: "Invalid or inactive API key"
Solution: Check API key is correct and client is active
```

**5. Cannot connect to S3/MinIO**
```bash
# Check configuration
docker-compose exec storage-gateway env | grep STORAGE

# Check logs
docker-compose logs -f storage-gateway

# Test connectivity
curl http://your-minio-server:9000/minio/health/live
```

---

## S3 Bucket Operations (Technical Details)

### Uploading Object
```java
ObjectMetadata metadata = new ObjectMetadata();
metadata.setContentLength(contentLength);
metadata.setContentType(contentType);

s3Client.putObject(bucketName, keyName, inputStream, metadata);
```

### Listing Bucket Objects
```java
ObjectListing objectListing = s3Client.listObjects(bucketName);
objectListing.getObjectSummaries().forEach(o -> {
    log.info("Bucket Name: " + o.getBucketName());
    log.info("Key Name: " + o.getKey());
    log.info("Object Size: " + o.getSize());
});
```

### Downloading Object
```java
S3Object s3Object = s3Client.getObject(bucketName, keyName);
InputStream inputStream = s3Object.getObjectContent();
// Convert to byte array...
```

### Deleting Object
```java
s3Client.deleteObject(bucketName, keyName);
```

---

## Libraries and Dependencies

### Core Dependencies
- **Spring Boot** 3.5.9
- **Spring Web** - REST API support
- **Spring DevTools** - Hot reload for development
- **Spring Boot Actuator** - Health checks and metrics
- **Spring Validation** - Input validation
- **AWS SDK v2** (2.40.16) - S3/MinIO client
- **Lombok** 1.18.42 - Reduces boilerplate code
- **SpringDoc OpenAPI** 2.8.4 - API documentation

### Maven Dependencies
```xml
<dependencies>
  <!-- Spring Boot Web -->
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
  </dependency>

  <!-- Spring Boot Actuator -->
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
  </dependency>

  <!-- AWS SDK v2 for S3 -->
  <dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>s3</artifactId>
    <version>2.40.16</version>
  </dependency>

  <!-- Lombok -->
  <dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.42</version>
    <scope>provided</scope>
  </dependency>

  <!-- OpenAPI Documentation -->
  <dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.4</version>
  </dependency>
</dependencies>
```

---

## Future Enhancements

Potential features to add:

### 1. Database Integration
- Replace in-memory client storage with PostgreSQL/MongoDB
- Track file metadata
- Audit logs

### 2. Advanced Security
- JWT authentication
- Role-based access control (RBAC)
- IP whitelisting
- Rate limiting

### 3. File Management
- File tagging/metadata
- Full-text search
- Thumbnail generation
- Virus scanning
- File versioning with history
- Expiring download links

### 4. Analytics
- Usage dashboards
- Cost analysis
- Performance metrics
- Client analytics

### 5. Automation
- Automatic archiving
- Smart lifecycle management
- Duplicate detection
- Backup automation

---

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

---

## License

This project is licensed under the MIT License.

---

## Support

For issues and questions:
- Check [Swagger UI](http://localhost:8080/swagger-ui.html) for API documentation
- Review [Health Checks](http://localhost:8080/actuator/health) for system status
- Open an issue on GitHub

---

## Summary

This microservice provides a **production-ready storage gateway** with:

✅ **Multi-provider support** (AWS S3 & MinIO)
✅ **Enterprise multi-tenancy** with client isolation
✅ **Flexible bucket strategies** (shared, per-client, per-environment)
✅ **Public API** - All endpoints are publicly accessible
✅ **Advanced security** (path sanitization, quotas)
✅ **Environment organization** (dev, staging, prod)
✅ **Comprehensive API** with Swagger documentation
✅ **Health monitoring** and Prometheus metrics
✅ **Docker & Kubernetes** ready
✅ **Horizontal scalability** (stateless design)

**Ready for production deployment!** 🚀

---

**Built with ❤️ using Spring Boot, AWS SDK v2, and modern microservice best practices.**
