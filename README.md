<div align="center">

# DEADLOCK ESCAPE GAME

### *An Interactive Operating Systems Deadlock Simulator*

[![Java](https://img.shields.io/badge/Java-25-orange?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21-blue?style=for-the-badge&logo=javafx&logoColor=white)](https://openjfx.io/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-00758F?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Maven](https://img.shields.io/badge/Maven-3.9-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)](LICENSE)

<br>

**Learn Operating System deadlock concepts through an interactive, visual, and gamified experience.**

</div>

---

## About The Project

**Deadlock Escape Game** is a JavaFX-based educational simulator that teaches players about **deadlock detection, prevention, and safe resource allocation** in Operating Systems. Players must manage processes and resources, find safe sequences, and avoid circular waits — all through an interactive node-graph visualization with a premium cyber-security themed UI.

### Key Highlights

- **3 progressively challenging levels** with unique deadlock scenarios
- **Real-time animated graph visualization** of Resource Allocation Graphs (RAG)
- **Deadlock detection** using DFS cycle detection algorithm
- **MySQL database integration** for persistent score tracking
- **Leaderboard system** with name-wise sorting and highest score display
- **Player login system** for personalized gameplay

---

## Features

| Feature | Description |
|---------|-------------|
| **Player Login** | Enter your name to start — scores are saved per player |
| **3 Levels** | Easy (5P/5R) → Medium (8P/8R) → Hard (12P/12R) |
| **Graph Visualization** | Animated nodes and connections showing process-resource relationships |
| **Deadlock Detection** | Automatic + manual deadlock detection with visual cycle highlighting |
| **Safe State Detection** | Green celebration animation when a safe state is found |
| **Scoring System** | Points for allocations, releases, process completion, and level win |
| **Timer** | Countdown timer adds urgency to each level |
| **Leaderboard** | View all scores sorted by player name with highest score display |
| **MySQL Database** | All scores persist in MySQL for cross-session tracking |
| **Responsive UI** | Full-screen Navy + Neon cyber-security themed interface |

---

## Gameplay Rules

```
ALLOCATE  → Select Process + Resource → Request a resource
            If free: granted immediately (+10 pts)
            If held:  process starts WAITING

RELEASE   → Select Process + Resource → Release ONE resource
            Process auto-finishes when all resources released (+25 pts)

FINISH    → Manually complete a process with no resources left

GOAL      → Release resources in the correct order to finish ALL processes
            without creating a circular wait (deadlock)
```

---

## Levels

### Level 1: The Basics (Easy)
> 5 Processes, 5 Resources — Simple Chain

```
P1→R1→P2→R2→P3→R3→P4→R4→P5
```
- P5 has no request (escape hatch) — finish first
- Safe order: P5 → P4 → P3 → P2 → P1

### Level 2: The Crossroads (Medium)
> 8 Processes, 8 Resources — Branching with Cross-Links

```
Chain A: P1→R1→P2→R2→P3
Chain B: P4→R4→P5→R5→P6
Cross:   P7 wants R2, P8 wants R8
```
- Two valid safe paths exist
- Wrong order on cross-links = deadlock

### Level 3: The Web (Hard)
> 12 Processes, 12 Resources — Inter-Connected Chains

```
Chain A: P1→R1→P2→R2→P3→R3→P4
Chain B: P5→R5→P6→R6→P7→R7→P8
Chain C: P9→R9→P10→R10→P11→R11→P12
Cross:   P2 wants R6, P6 wants R9, P10 wants R3
```
- Three chains sharing resources between them
- Must finish escape hatches in specific order

---

## OS Concepts Covered

| Concept | Implementation |
|---------|---------------|
| **Process** | Game characters (P1, P2, ...) with held resources and waiting state |
| **Resource** | Objects (R1, R2, ...) that processes need — mutual exclusion enforced |
| **Allocation** | Process holds a resource |
| **Request/Wait** | Process blocked on an unavailable resource |
| **Deadlock** | Circular wait detected via DFS cycle detection |
| **Safe Sequence** | Valid order to finish all processes without deadlock |
| **Resource Allocation Graph** | Visual node-graph with animated connections |
| **Banker's Safety Algorithm** | Used in `findSafeSequence()` to verify solvability |

---

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Java 25 |
| UI Framework | JavaFX 21 |
| Build Tool | Maven |
| Database | MySQL 8.0 |
| JDBC Driver | MySQL Connector/J 8.3 |
| IDE | IntelliJ IDEA |

---

## Project Structure

```
Deadlock-Escape-Game/
├── src/main/java/
│   ├── application/
│   │   └── Main.java                  # App entry point
│   ├── model/
│   │   ├── Level.java                 # Level data (processes, resources, setup)
│   │   ├── Process.java               # Process state management
│   │   ├── Resource.java              # Resource state management
│   │   └── PlayerSession.java         # Player name singleton
│   ├── logic/
│   │   ├── GameManager.java           # Core game logic & scoring
│   │   └── DeadlockDetector.java      # DFS-based deadlock detection
│   ├── ui/
│   │   ├── LoginScreen.java           # Player login
│   │   ├── MenuScreen.java            # Main menu
│   │   ├── LevelSelectionScreen.java  # Level chooser
│   │   ├── GameScreen.java            # Main gameplay
│   │   ├── ResultScreen.java          # Results + DB save
│   │   ├── LeaderboardScreen.java     # Score leaderboard
│   │   └── graph/
│   │       ├── GraphCanvas.java       # Graph visualization engine
│   │       ├── NodeView.java          # Process/Resource node circles
│   │       └── ConnectionLine.java    # Animated connection lines
│   └── db/
│       └── ScoreDatabase.java         # MySQL CRUD operations
├── src/main/resources/
│   └── style.css                      # Navy + Neon theme stylesheet
└── pom.xml                            # Maven configuration
```

---

## Getting Started

### Prerequisites

- **Java JDK 25+**
- **MySQL 8.0+** with MySQL Workbench
- **Maven 3.9+**

### Installation

**1. Clone the repository**
```bash
git clone https://github.com/aliyaahmad948/OS-DEADLOCK-ESCAPE.git
cd OS-DEADLOCK-ESCAPE
```

**2. Set up MySQL database**
```sql
CREATE DATABASE deadlock_game;
```

**3. Update database credentials** in `src/main/java/db/ScoreDatabase.java`:
```java
private static final String DB_USER = "root";
private static final String DB_PASS = "your_password";
```

**4. Run the application**
```bash
mvn clean javafx:run
```

Or open in **IntelliJ IDEA** → Right-click `Main.java` → **Run**

---


## Scoring System

| Action | Points |
|--------|--------|
| Allocate a resource | +10 |
| Release a resource | +10 |
| Process finishes | +25 |
| Win the level | +50 (bonus) |

---

## Author

**Ali Yahmad**
- GitHub: [@aliyaahmad948](https://github.com/aliyaahmad948)
- Email: aliyaahmad948@gmail.com

---

## License

This project is open source and available for educational purposes.

---

<div align="center">

**If you found this project helpful, give it a star!**

</div>
