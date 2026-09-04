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

- **5 game modes** with **5 progressively challenging levels each (25 levels total)**
- **Real structural difficulty scaling** — later levels use harder graph topologies (contested hubs, multi-hold webs, converging chains, open rings), not just more nodes
- **Real-time animated graph visualization** of Resource Allocation Graphs (RAG) with **click-to-select** node interaction
- **Deadlock detection** using DFS cycle detection with visual red-cycle highlighting
- **Banker's Safety Algorithm** (`findSafeSequence()`) guarantees every level is solvable
- **MySQL database integration** for persistent scores, leaderboards AND player progress (stars, level unlocks, XP)
- **Achievement system** with 18 unlockable achievements
- **Dual themes** — Navy + Neon (dark) and light theme with one-click toggle
- **Practice Lab** for risk-free experimentation

---

## Features

| Feature | Description |
|---------|-------------|
| **Player Login** | Enter your name to start — scores and progress are saved per player |
| **5 Game Modes** | Mutual Exclusion, Hold & Wait, No Preemption, Circular Wait, and Deadlock Escape (combined) |
| **25 Levels** | 5 levels per mode with true structural difficulty scaling (4→12 processes) |
| **Graph Visualization** | Animated nodes and clickable process/resource nodes showing relationships |
| **Deadlock Detection** | Automatic + manual detection with visual red-cycle highlighting |
| **Safe State Detection** | Banker's-algorithm `findSafeSequence()` — verifies safe finish order |
| **OS Guide Panel** | Real-time educational hints explaining each deadlock condition |
| **Hints System** | Context-aware hints (limited per level based on difficulty) |
| **Practice Lab** | Free experimentation mode — no timer, no score, no loss |
| **Scoring System** | Points for allocations, releases, process completion, and level win |
| **Star Ratings** | 1–3 stars per level based on time, efficiency and mistakes |
| **Achievements** | 18 achievements tracking wins, clean runs, streaks and more |
| **MySQL Persistence** | Scores, mode leaderboards AND progress (stars / unlocks / XP) persist across sessions |
| **Dark / Light Theme** | One-click theme toggle, persisted between runs |
| **Responsive UI** | Full-screen Navy + Neon cyber-security themed interface |

---

## Gameplay Rules

```
ALLOCATE  → Click a PROCESS node + a RESOURCE node on the graph → Allocate
            If free: granted immediately (+10 pts)
            If held:  process starts WAITING

RELEASE   → Click a PROCESS node + a RESOURCE node → Release ONE resource
            Process auto-finishes when all resources released (+10 pts)

FINISH    → Click a PROCESS node (holding no resources) → Complete it (+25 pts)

GOAL      → Finish ALL processes in a safe sequence
            without creating a circular wait (deadlock)

WARNING   → A careless request that would close a cycle is highlighted red
            before you commit it
```

---

## Game Modes & Level Structure

Every level starts **solvable and deadlock-free**; the "circular wait" danger appears only if the player makes a careless request. Later levels change the **graph structure** itself:

| Mode | Concept | Structure Progression |
|------|---------|----------------------|
| **Mutual Exclusion** | One process per resource | chain → contested hub → parallel hubs → heavy hub + side race → contest mesh |
| **Hold & Wait** | Holding resources while waiting | chain → one dual-holder → branching + two dual-holders → contention points → multi-holder web |
| **No Preemption** | Resources can't be force-taken | two short chains → mixed-depth convergence → two convergence hubs → big hub + deep tail → guarded escape |
| **Circular Wait** | Cycles of waiting | single open ring → longer ring + distractor → two rings → multi-ring + distractors |
| **Deadlock Escape** | Combined (all four) | readable chain → chain + contention → contention + multi-hold → banker hub → OS Chaos web |

> Each mode has 5 levels: process counts 4, 6, 8, 10, 12 with escalating timers (120s → 90s).

---

## Screenshots

> *Screenshots will be added here. Drop your images into the `screenshots/` folder using these file names.*

![Login Screen](screenshots/01-login.png)

![Main Menu](screenshots/02-menu.png)

![Mode Selection](screenshots/03-mode-selection.png)

![Level Selection](screenshots/04-level-selection.png)

![Gameplay - Node Graph](screenshots/05-gameplay.png)

![Practice Lab](screenshots/06-practice-lab.png)

![Level Result](screenshots/07-result.png)

![Player Profile](screenshots/08-profile.png)

![Leaderboard](screenshots/09-leaderboard.png)

![Light Theme](screenshots/10-light-theme.png)

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
| **Banker's Safety Algorithm** | Used in `findSafeSequence()` to verify every level is solvable |

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
│   │   ├── Achievement.java           # Achievement definition
│   │   ├── Difficulty.java            # Difficulty levels (Easy → Chaos)
│   │   ├── GameMode.java              # The 5 game modes
│   │   ├── Level.java                 # Level data (processes, resources, setup)
│   │   ├── LevelResult.java           # Per-level outcome summary
│   │   ├── Mission.java               # Mission + bonus objectives
│   │   ├── Process.java               # Process state management
│   │   ├── Resource.java              # Resource state management
│   │   └── PlayerSession.java         # Player name singleton
│   ├── logic/
│   │   ├── AchievementTracker.java    # Achievement unlock tracking
│   │   ├── GameManager.java           # Core game logic & scoring
│   │   ├── DeadlockDetector.java      # DFS-based deadlock detection
│   │   ├── HintManager.java           # Context-aware hint engine
│   │   ├── LevelFactory.java          # 25 levels — structural difficulty scaling
│   │   ├── OSGuide.java               # Educational OS guidance panel
│   │   ├── ProgressManager.java       # Session + MySQL-backed progress
│   │   ├── RecoveryAdvisor.java       # Deadlock recovery suggestions
│   │   └── StarsCalculator.java       # Star rating + efficiency math
│   ├── ui/
│   │   ├── LoginScreen.java           # Player login
│   │   ├── MenuScreen.java            # Main menu
│   │   ├── ModeSelectionScreen.java   # Choose a game mode
│   │   ├── ConceptModeLevelScreen.java# Level chooser per mode
│   │   ├── GameScreen.java            # Main gameplay (node graph)
│   │   ├── ResultScreen.java          # Results + DB save
│   │   ├── ProfileScreen.java         # Persistent player profile
│   │   ├── LeaderboardScreen.java     # Score leaderboards
│   │   ├── PracticeLabScreen.java     # Free practice (no score/timer)
│   │   ├── ThemeManager.java          # Dark ↔ light theme switching
│   │   └── graph/
│   │       ├── GraphCanvas.java       # Graph visualization engine
│   │       ├── NodeView.java          # Process/Resource node circles
│   │       └── ConnectionLine.java    # Animated connection lines
│   └── db/
│       └── ScoreDatabase.java         # MySQL CRUD operations
├── src/main/resources/
│   ├── style.css                      # Navy + Neon (dark) stylesheet
│   └── style-light.css                # Light theme stylesheet
└── pom.xml                            # Maven configuration
```

---

## Getting Started

### Prerequisites

- **Java JDK 25+**
- **MySQL 8.0+** (running on `localhost:3306`)
- **Maven 3.9+**

### Installation

**1. Clone the repository**
```bash
git clone https://github.com/aliyaahmad948/OS-DEADLOCK-ESCAPE.git
cd OS-DEADLOCK-ESCAPE
```

**2. Create and set up MySQL database** (tables are auto-created on first run)
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
mvn javafx:run
```

Or open in **IntelliJ IDEA** → Right-click `application/Main.java` → **Run**

> If maven can't reach the network, run offline: `mvn -o compile` then run via the IDE.

---

## Scoring System

| Action | Points |
|--------|--------|
| Allocate a resource | +10 |
| Release a resource | +10 |
| Process finishes | +25 |
| Win the level | +50 (bonus) |
| Finish within bonus time | +bonus objective |

**Star rating** (1–3 stars) is based on time taken, efficiency and mistakes — best stars are saved to your profile.

---

## Author

**Alia Ahmad**
- GitHub: [@aliyaahmad948](https://github.com/aliyaahmad948)

---

## License

This project is open source and available for educational purposes.

---

<div align="center">

**If you found this project helpful, give it a star!**

</div>