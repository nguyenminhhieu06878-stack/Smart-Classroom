# 🐳 Hướng dẫn sử dụng Docker

## Cấu trúc file

```
.
├── docker-compose.yml          # Development mode (PostgreSQL + Adminer)
├── docker-compose.prod.yml     # Production mode (App + PostgreSQL + Adminer)
├── Dockerfile                  # Build ứng dụng Spring Boot
├── .env                        # Environment variables
├── DOCKER.md                   # File này
└── src/
    └── main/
        └── resources/
            ├── application.properties              # Config chung
            ├── application-dev.properties          # Profile: dev (H2)
            └── application-docker.properties       # Profile: docker (PostgreSQL)
```

## 🚀 Cách chạy

### Option 1: Chạy PostgreSQL với Docker, App chạy local (Khuyến nghị cho dev)

**Bước 1:** Khởi động PostgreSQL

```bash
docker-compose up -d
```

**Bước 2:** Chạy ứng dụng với profile `docker`

```bash
# Maven
mvn spring-boot:run -Dspring-boot.run.profiles=docker

# Hoặc build jar rồi chạy
mvn clean package -DskipTests
java -jar -Dspring.profiles.active=docker target/smart-classroom-platform-1.0.0.jar
```

**Truy cập:**
- Ứng dụng: http://localhost:1002
- Adminer (DB Management): http://localhost:8080
  - System: PostgreSQL
  - Server: localhost:5432
  - Database: smartclass
  - Username: smartclass
  - Password: smartclass123

### Option 2: Chạy toàn bộ với Docker Compose (Production mode)

**Bước 1:** Cấu hình environment variables

```bash
# Copy file .env và chỉnh sửa
cp .env.example .env
# Sau đó edit .env và thay thế các API keys
```

**Bước 2:** Build và chạy

```bash
docker-compose -f docker-compose.prod.yml up -d --build
```

**Hoặc** nếu đã build image trước đó:

```bash
docker-compose -f docker-compose.prod.yml up -d
```

**Truy cập:**
- Ứng dụng: http://localhost:1002
- Adminer: http://localhost:8080

## 📋 Các lệnh Docker hữu ích

```bash
# Khởi động PostgreSQL (development)
docker-compose up -d

# Dừng PostgreSQL
docker-compose down

# Xem logs
docker-compose logs -f postgres

# Truy cập PostgreSQL container
docker exec -it smartclass-postgres psql -U smartclass -d smartclass

# Xóa data và khởi động lại
docker-compose down -v
docker-compose up -d

# Production mode
docker-compose -f docker-compose.prod.yml up -d
docker-compose -f docker-compose.prod.yml down

# Xem logs production
docker-compose -f docker-compose.prod.yml logs -f app
```

## 🔧 Cấu hình Database

### Kết nối từ ứng dụng

| Property | Dev (H2) | Docker (PostgreSQL) |
|----------|----------|---------------------|
| URL | `jdbc:h2:mem:smartclass` | `jdbc:postgresql://localhost:5432/smartclass` |
| Username | `sa` | `smartclass` |
| Password | (empty) | `smartclass123` |

### Thay đổi cấu hình PostgreSQL

Edit file `.env`:

```env
POSTGRES_DB=your_database_name
POSTGRES_USER=your_username
POSTGRES_PASSWORD=your_secure_password
```

Sau đó restart:

```bash
docker-compose down -v
docker-compose up -d
```

## 🌟 Profile trong Spring Boot

| Profile | Database | Use Case |
|---------|----------|----------|
| `dev` (default) | H2 In-memory | Phát triển nhanh, không cần cài đặt gì thêm |
| `docker` | PostgreSQL | Khi chạy với Docker |

Chuyển đổi profile:

```bash
# Chạy với profile dev (H2)
mvn spring-boot:run

# Chạy với profile docker (PostgreSQL)
mvn spring-boot:run -Dspring-boot.run.profiles=docker
```

## 🐛 Troubleshooting

### Lỗi kết nối database

```bash
# Kiểm tra container đang chạy
docker ps

# Kiểm tra logs
docker-compose logs postgres

# Restart PostgreSQL
docker-compose restart postgres
```

### Port đã được sử dụng

```bash
# Tìm process đang dùng port 5432
netstat -ano | findstr :5432

# Hoặc thay đổi port trong docker-compose.yml
ports:
  - "5433:5432"  # Map port 5433 trên host -> 5432 trong container
```

### Xóa tất cả data

```bash
# Xóa container và volume
docker-compose down -v

# Xóa tất cả images (cẩn thận!)
docker system prune -a
```

## 📚 Tham khảo

- [PostgreSQL Docker Image](https://hub.docker.com/_/postgres)
- [Adminer](https://www.adminer.org/)
- [Spring Boot with Docker](https://spring.io/guides/gs/spring-boot-docker/)
