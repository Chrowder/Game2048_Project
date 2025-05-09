# 2048 Full‑Stack Java Project

## What is this?

A **complete classic _2048_ puzzle game** implemented in Java.

```
Game2048_Project
├── Game2048-Server  # Spring Boot REST + Thymeleaf + JPA
└── Game2048-Swing   # Java Swing desktop client (with AI)
```

## What does it do?

| Feature | Description |
|---------|-------------|
| **Desktop Client** | Smooth Swing GUI with keyboard & AI autoplay |
| **Powerful AI** | Iterative‑deepening Expectimax, scores 15‑20 k+ |
| **REST API** | `/scores` POST upload, `/scores/json` GET top scores |
| **Web Leaderboard** | `/scores` shows modern Bootstrap page, auto‑refresh |
| **Timestamps** | Every entry stored with upload time |
| **Desktop ↔ Web** | In‑game button opens the web leaderboard |
| **Persistence** | Default H2, switch to MySQL with 1‑line config |

## Technologies

* Java 17, Maven 3.8+, Database
* Swing, multithreading, Gson
* Spring Boot 3, Spring Data JPA, Bootstrap 5

## Quick‑start

### 1 Start back‑end

```bash
cd Game2048-Server
mvn spring-boot:run
```

### 2 Run desktop client

```bash
cd Game2048-Swing
mvn clean package
java -jar target/Game2048-Swing*-jar-with-dependencies.jar
```

*Controls*: ← ↑ → ↓ to move • **A** toggle AI • **R** restart

### 3 Web leaderboard

Open <http://localhost:8080/scores>

