# Bottle Drop — Distributed Beverage Management Platform

## Project Overview
Bottle Drop is a distributed platform for managing beverage supply and empty bottle collection at large events (inspired by the c3bottles system used at CCC events). The system covers the full lifecycle of drinks: from a central warehouse, to bars distributed across the event venue, to bottle drop points where visitors return empties. 

The platform supports multiple user roles (planner, warehouse assistant, bartender, visitor) and handles automated replenishment, capacity monitoring, and empties transport simulation via messaging. This project is developed as part of the DMSA SoSe26 course.

## Patterns & Technologies Used
* **Architecture:** Microservice Architecture
* **Pattern:** Domain-Driven Design (DDD)
* **Framework:** Spring Boot 3 (Java 21)
* **Communication:** REST APIs / Service Discovery (Netflix Eureka)
* **Configuration:** Centralized Spring Cloud Config Server
* **Resilience:** Resilience4j
* **Database:** Polyglot Persistence (Each microservice manages its own database, e.g., H2 In-Memory, MySQL, etc.)
* **Frontend:** Modern React SPA (Single Page Application) built with Vite and TailwindCSS

## Team Members & Microservices
The system is divided into 5 independent Bounded Contexts (Microservices), each managed by a dedicated architect:

* **BC-1: User Management Service** — Developed by **[Ali Murad]**
* **BC-2: Warehouse Management Service** — Developed by **[Onyedikachi Onah]**
* **BC-3: Bar Management Service** — Developed by **[Mehmet Can Ikiz]**
* **BC-4: Bottle Drop Point Service** — Developed by **[Faryal Khalid Mahmood]**
* **BC-5: Visitor Purchasing Service** — Developed by **[Mohamad Hussein]**

## Architecture & Documentation
* **Domain & Service Modeling:** Formal LEMMA domain and service models are included under [`lemma/`](./lemma).
* **Containerized Stack:** Full multi-service orchestration via [`docker-compose.yml`](./docker-compose.yml).


## How to Run the Project — Evaluator Instructions

This project can be started with **Docker Compose**.

The evaluator does not need to manually install MySQL, Eureka, Config Server, or run the Spring Boot services from IntelliJ.

---

## 1. Prerequisites

Please make sure the following tools are installed:

- Docker Desktop
- Docker Compose
- Git

Check Docker installation:

```bash
docker --version
docker compose version
```

---

## 2. Clone the Repository

```bash
git clone <REPOSITORY_URL>
cd dmsa-sose26-mh23mc25
```

Replace `<REPOSITORY_URL>` with the actual Git repository URL.

---

## 3. Start the Project

From the root directory of the repository, run:

```bash
docker compose up -d --build
```

This command builds the required Docker images and starts the containers.

---

## 4. Check Running Containers

After startup, check the container status:

```bash
docker compose ps
```

The main containers should show `running` or `healthy`.

Expected main services:

- `mysql-container`
- `eureka-server`
- `config-server`
- `purchase-service`
- `bar-management-service`
- `user-management-service`
- `frontend`

Depending on the current project setup, additional services may also be started.

---

## 5. Access the Services

### Bar Management Simulation UI

```text
http://localhost:8083
```

### Eureka Dashboard

```text
http://localhost:8761
```

---

### Config Server Health Check

```text
http://localhost:8888/actuator/health
```

### Frontend

```text
http://localhost:3000
```

### Frontend

```text
http://localhost:8089
```


### Required Startup Order:
To ensure the microservices can fetch their configurations and discover each other, they **MUST** be started in this exact sequence:

1. **Service Discovery:** Start `eureka-server` first (`./mvnw spring-boot:run`).
2. **Configuration Management:** Start `config-server` second. Wait for it to fully initialize.
3. **Core Microservices:** Start the remaining 5 microservices in any order (User, Warehouse, Bar, Drop Point, Purchase).
4. **Frontend UI:** 
   - Navigate to the `frontend` folder: `cd frontend`
   - Install dependencies: `npm install`
   - Start the React server: `npm run dev`
   - Access the application at `http://localhost:5173`

