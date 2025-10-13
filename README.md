# 🚀 BreakingJob: The AI-Powered Hiring Platform

[![Java](https://img.shields.io/badge/Java-21-orange?logo=java)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/Database-PostgreSQL-blue?logo=postgresql)](https://www.postgresql.org/)
[![GitHub Actions](https://img.shields.io/github/actions/workflow/status/yashpriyadarshan/BreakingJob/maven-test.yml?branch=main&label=CI%20Build&logo=github)](https://github.com/yashpriyadarshan/BreakingJob/actions/workflows/maven-test.yml)
[![License](https://img.shields.io/badge/License-MIT-lightgrey.svg)](LICENSE)
[![Contributions Welcome](https://img.shields.io/badge/Contributions-Welcome-success.svg)](#-contribution)

> **BreakingJob** is a next-generation **AI-powered hiring platform** built using **Spring Boot microservices** and designed to **revolutionize recruitment**.  
> The platform replaces outdated resume screening with an **automated AI video interview** to ensure recruiters only meet **qualified, pre-vetted candidates**, while giving applicants a fair way to prove their skills — beyond a CV.

---

## 📚 Table of Contents

1. [✨ Key Features & Highlights](#-key-features--highlights)
   - [For Candidates](#-for-candidates)
   - [For Companies / Recruiters](#-for-companies--recruiters)
2. [🏗️ Microservices Architecture](#️-microservices-architecture)
3. [💻 Tech Stack Overview](#-tech-stack-overview)
4. [🧪 CI/CD & Testing Workflow](#-cicd--testing-workflow)
5. [🛠️ Getting Started (Local Development)](#️-getting-started-local-development)
   - [Prerequisites](#-prerequisites)
   - [Setup Instructions](#-setup-instructions)
6. [🤝 Contribution](#-contribution)
7. [📞 Contact](#-contact)
8. [📄 License](#-license)

---

## ✨ Key Features & Highlights
BreakingJob eliminates inefficient resume screening with an **AI-first hiring pipeline**.
### 👨‍💻 For Candidates
- **Mandatory AI Interview:**  
  Every candidate must complete a 3-minute AI video interview before applying to any job.
- **Resume-Aware Questions:**  
  The AI dynamically generates questions based on the candidate’s uploaded resume for authenticity and relevance.
- **Verified Applications:**  
  Job applications unlock **only after** interview completion, ensuring serious and qualified candidates.
### 🏢 For Companies / Recruiters
- **AI-Scored Applicants:**  
  Recruiters receive an **AI Interview Score**, **video**, and **resume**, giving an immediate and objective measure of fit.
- **Time-Efficient Hiring:**  
  Significantly reduces manual screening effort while improving conversion rates.
- **End-to-End Job Management:**  
  Tools for posting, tracking, and managing jobs directly within the platform.
---

## 🏗️ Microservices Architecture

BreakingJob is structured as independent **Spring Boot microservices**, enabling modular development, scalability, and deployment flexibility.

```
BreakingJob/
│
├── CompanyMS/       # Handles company profiles and details
├── JobMS/           # Manages job postings, applications, and job pipelines
├── ReviewMS/        # Manage companies review

```

Each microservice has:
- Its own `pom.xml` for dependencies  
- Dedicated RESTful endpoints  
- Independent test suite using **H2 in-memory DB** (For testing)

---

## 💻 Tech Stack Overview

| **Layer** | **Technology** | **Purpose** |
|------------|----------------|--------------|
| **Backend Core** | Spring Boot, Spring MVC | REST APIs and business logic |
| **Data Layer** | Spring Data JPA, Hibernate | ORM and persistence handling |
| **Databases** | PostgreSQL (Prod), H2 (Test) | Reliable & lightweight DBs |
| **AI Layer (Planned)** | Python + TensorFlow/PyTorch | Dynamic question generation, scoring, and video analysis |
| **Frontend (Planned)** | React / Next.js | Fast, responsive user interface |
| **Build & CI** | Maven + GitHub Actions | Automated builds and testing |
| **Runtime Env** | JDK 21 (Temurin) | Modern, secure Java runtime |

---

## 🧪 CI/CD & Testing Workflow

BreakingJob uses **GitHub Actions** for continuous integration and testing.  
The workflow (`.github/workflows/build.yml`) ensures:
- ✅ Every push and pull request to `main` triggers a **clean build and test**.
- ✅ All microservices are built independently with their own `pom.xml`.
- ✅ Tests run using the **H2 in-memory database** to simulate isolated environments.


---

## 🛠️ Getting Started (Local Development)

### ✅ Prerequisites
- **Java Development Kit (JDK 21+)**
- **Maven**
- **Git**
- (Optional) **Docker** for containerization

### ⚙️ Setup Instructions

1. **Clone the Repository**
   ```bash
   git clone https://github.com/YOUR_GITHUB_USERNAME/BreakingJob.git
   cd BreakingJob
   ```

2. **Build All Microservices**
   ```bash
   mvn clean install
   ```

3. **Run Individual Microservices**
   ```bash
   java -jar CompanyMS/target/companyms.jar
   java -jar JobMS/target/jobms.jar
   java -jar ReviewMS/target/reviewms.jar
   ```

4. The app will automatically connect to the **H2 database** for local testing.  
   Use `http://localhost:8081/companies` for Company Services.
   Use `http://localhost:8082/jobs` for Job services
   Use `http://localhost:8082/reviews` for Review services

---

## 🤝 Contribution

We welcome open-source contributions!  

1. **Fork** this repository  
2. **Create a new branch**
   ```bash
   git checkout -b feature/awesome-feature
   ```
3. **Commit your changes**
   ```bash
   git commit -m "Feat: Add awesome feature"
   ```
4. **Push your branch**
   ```bash
   git push origin feature/awesome-feature
   ```
5. **Open a Pull Request**

💡 Have ideas for **AI scoring**, **microservice optimization**, or **UI improvements**? Let’s collaborate!

---

## 📞 Contact

**Project Lead:** [Yash Priyadarshan](https://github.com/yashpriyadarshan)  
**Email:** yashpriyadarshan@gmail.com  
**GitHub:** [@yashpriyadarshan](https://github.com/yashpriyadarshan)

---

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

---

### 🌟 Show Your Support

If you found this project interesting, please **⭐ star the repository** and share it — your support helps BreakingJob grow!

---
