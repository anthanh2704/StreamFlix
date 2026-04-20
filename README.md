# 🎬 StreamFlix — Video Streaming Database

> **IT079IU — Principles of Database Management**
> Final Project, International University, VNU-HCM

A relational database system that powers an online video-streaming platform, implemented with **MySQL 8** for persistence and a **Spring Boot 3 / Java 17** backend. A minimal single-page HTML frontend demonstrates the REST API.

---

## ✨ Key Features

| Component                         | Delivered via                                               |
| --------------------------------- | ----------------------------------------------------------- |
| Video content management          | `video`, `video_category`, `video_tag` tables               |
| Video storage (metadata + links)  | `video.video_url` (CDN/S3 URL) + resolution/duration fields |
| User profiles & authentication    | `app_user`, Spring Security HTTP Basic, BCrypt              |
| Streaming & delivery              | `watch_history` (trigger auto-increments views)             |
| Search & recommendation engine    | Full-text search, collaborative filtering native query      |
| User interaction                  | `video_reaction`, `comment`, `channel_subscription`         |
| Subscription & payment (business) | `subscription_plan`, `user_subscription`, `payment`         |

---

## 🗂️ Project Layout

```
video-streaming-db/
├── streamflix-database/
│   ├── 01_schema.sql         ← DDL: 16 tables, FKs, triggers, views, indexes
│   ├── 02_sample_data.sql    ← Sample data (10 users, 10 videos, etc.)
│   └── 03_queries.sql        ← 25 queries: basic → advanced
├── streamflix-backend/                  ← Spring Boot 3 API
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/streamflix/
│       │   ├── entity/       ← 14 JPA entities
│       │   ├── repository/   ← Spring Data repositories
│       │   ├── service/      ← Business logic
│       │   ├── controller/   ← REST endpoints
│       │   ├── dto/          ← Request/response records
│       │   ├── exception/    ← Global error handler
│       │   └── security/     ← HTTP Basic + BCrypt config
│       └── resources/application.yml
├── streamflix-frontend/
│   ├── index.html            ← Single-page demo UI
│   ├── styles.css
│   └── app.js
└── streamflix-docs/
    ├── ERD.md                ← Entity-Relationship diagram
    ├── RELATIONAL_SCHEMA.md  ← ERD → relational translation
    ├── NORMALIZATION.md      ← 1NF → 2NF → 3NF → BCNF analysis
    └── API.md                ← REST API reference
```

---

## 🚀 Quick Start

### 1. Set up MySQL

```bash
# Log in as root (or any user allowed to CREATE DATABASE)
mysql -u root -p

# Run the three SQL files in order
mysql> SOURCE streamflix-database/01_schema.sql;
mysql> SOURCE streamflix-database/02_sample_data.sql;
mysql> SOURCE streamflix-database/03_queries.sql;       -- optional, runs the demo queries
```

### 2. Configure the backend

Edit `streamflix-backend/src/main/resources/application.yml` if your MySQL credentials differ from `root / root`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/streamflix_db
    username: <your-mysql-user>
    password: <your-mysql-password>
```

### 3. Build and run

```bash
cd streamflix-backend
mvn spring-boot:run
```

The API is now live at `http://localhost:8080/api`.

Quick health check:
```bash
curl http://localhost:8080/api/auth/health
```

### 4. (Optional) Run without MySQL

Use the embedded H2 database for a zero-dependency demo:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

### 5. Open the frontend

```bash
cd streamflix-frontend
python3 -m http.server 5173
# then open http://localhost:5173
```

---

## 🔐 Authentication

The API uses **HTTP Basic** (good enough for a course demo — swap to JWT for production).

```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
     -H "Content-Type: application/json" \
     -d '{"username":"demo","email":"demo@x.com","password":"secret12"}'

# Call an authenticated endpoint
curl -u demo:secret12 http://localhost:8080/api/auth/me
```

---

## 🧪 Trying the REST API

```bash
# List videos (public)
curl http://localhost:8080/api/videos

# Search
curl "http://localhost:8080/api/videos/search?q=iphone"

# Trending (last 30 days)
curl "http://localhost:8080/api/videos/trending?days=30&limit=5"

# Like a video
curl -u demo:secret12 -X POST http://localhost:8080/api/videos/1/like

# Record a watch
curl -u demo:secret12 -X POST http://localhost:8080/api/videos/1/watch \
     -H "Content-Type: application/json" \
     -d '{"watchDuration":120,"progressPct":50,"deviceType":"WEB"}'

# Post a comment
curl -u demo:secret12 -X POST http://localhost:8080/api/comments/video/1 \
     -H "Content-Type: application/json" \
     -d '{"content":"Great video!","parentCommentId":null}'
```

---

## 📊 Database Highlights

- **16 tables** in 3NF / BCNF with proper primary and foreign keys.
- **5 triggers** keep denormalized counters (`views_count`, `likes_count`, `subscriber_count`) in sync automatically.
- **2 views** (`vw_trending_videos`, `vw_channel_stats`) power analytics.
- **25 SQL queries** in `03_queries.sql` demonstrate basic SELECT → JOIN → GROUP BY → subqueries → CTEs → window functions → relational division.
- **Full-text index** on `video.title / description` for fast search.
- **Composite primary keys** on every many-to-many table.

See `docs/` for the full ERD, relational schema, and normalization analysis.

---

## 👥 Group Members

*(fill in before submission)*

| Name | Student ID | Role |
| ---- | ---------- | ---- |
|      |            | Team Leader |
|      |            | Member |
|      |            | Member |

## 👨‍🏫 Instructor

Assoc. Prof. Nguyen Thi Thuy Loan, PhD
