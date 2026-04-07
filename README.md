# GameBoost — PC Optimizer for Gamers

## Requirements
- Java 17 or 21 JDK (JDK, not JRE)
- IntelliJ IDEA (Community or Ultimate)
- Windows 10 or 11
- Run as Administrator for optimizations to work

---

## Setup in IntelliJ IDEA

### Step 1 — Open the project
File → Open → select the `GameBoost` folder (the one containing `pom.xml`).
IntelliJ will detect it as a Maven project automatically.

### Step 2 — Set the JDK
File → Project Structure → Project → SDK → select Java 17 or 21.
If not installed: click "Add SDK → Download JDK" and pick Microsoft or Eclipse Temurin 21.

### Step 3 — Load Maven dependencies
If prompted "Maven project needs to be imported" → click **Load**.
Or: right-click `pom.xml` → Maven → Reload Project.
Wait for IntelliJ to download all dependencies (~30 seconds first time).

### Step 4 — Create a Run Configuration
Run → Edit Configurations → click + → Application

Fill in:
- Name: `GameBoost`
- Main class: `com.gameboost.MainApp`
- Module/Classpath: `GameBoost` (the module from the dropdown)

Click OK.

### Step 5 — Run
Click the green ▶ button or press Shift+F10.

> **Note:** Some optimizations (kill processes, set power plan, registry edits) require
> Administrator privileges. For full testing, right-click IntelliJ → "Run as administrator",
> or use `run-as-admin.bat` from the project root.

---

## Project Structure

```
GameBoost/
├── pom.xml                              # Maven — dependencies, build config
├── run-as-admin.bat                     # Launch with UAC elevation (for testing)
├── README.md
└── src/main/
    ├── java/com/gameboost/
    │   ├── MainApp.java                 # JavaFX entry point
    │   ├── controller/
    │   │   └── MainController.java      # All UI logic + service wiring
    │   ├── model/
    │   │   ├── PcProfile.java           # Hardware scan result (cached to disk)
    │   │   ├── OptimizationEntry.java   # One optimization + risk metadata
    │   │   ├── HistoryEntry.java        # Single log entry
    │   │   └── LicenseState.java        # Free/Pro tier + usage counter
    │   ├── service/
    │   │   ├── PcScanService.java       # OSHI one-time hardware scan
    │   │   ├── OptimizationService.java # Windows commands (powershell/taskkill)
    │   │   ├── SystemStatsService.java  # Live CPU/RAM/disk/temp (polled every 2s)
    │   │   └── StorageService.java      # JSON read/write to ~/.gameboost/
    │   └── util/
    │       └── AdminChecker.java        # Checks if running as Administrator
    └── resources/com/gameboost/
        ├── main.fxml                    # UI layout (JavaFX FXML)
        └── css/dark-theme.css           # Dark gaming theme

Data stored at: %USERPROFILE%\.gameboost\
  pc-profile.json   — hardware scan (written once, read on every launch)
  history.json      — optimization log (newest first, capped at 200 entries)
  license.json      — free tier counter + pro key
```

---

## Implemented Optimizations

| ID | Name | Risk | Revertable |
|----|------|------|-----------|
| `kill_processes` | Kill Background Processes | 🟢 Safe | No |
| `power_plan` | Set High Performance Power Plan | 🟢 Safe | ✓ Yes |
| `clear_ram` | Clear RAM Cache | 🟢 Safe | No |
| `clear_temp` | Clear Temp Files | 🟢 Safe | No |
| `disable_gamebar` | Disable Xbox Game Bar | ⚠ Restart Required | ✓ Yes |
| `gpu_performance` | GPU Maximum Performance | 🔴 Risky | ✓ Yes |

---

## License Key Format (MVP validation)
`GB-XXXX-XXXX-XXXX` — where X is any letter A-Z or digit 0-9.
Example valid key: `GB-A1B2-C3D4-E5F6`

---

## Roadmap
- [ ] Startup Manager (HKCU Run registry keys)
- [ ] Network Optimizer (TCP registry tweaks for lower latency)
- [ ] Game Mode (one-click: kill procs + high perf + clear RAM)
- [ ] Real license server with HMAC validation
