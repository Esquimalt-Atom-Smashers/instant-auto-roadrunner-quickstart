# Road Runner Quickstart

Check out the [docs](https://rr.brott.dev/docs/v1-0/tuning/).

# InstantAuto Documentation

Welcome to the InstantAuto documentation. InstantAuto is a text-file-based autonomous programming system for FTC that integrates with RoadRunner (and adaptable to PedroPathing).

## Documentation Pages

| # | Title | Description |
|---|-------|-------------|
| 1 | [Parser (Auto Parser & Config Parser)](01-parser.md) | How text files are parsed into configs and actions |
| 2 | [Configuration (MetaField & MetaFieldRegistry)](02-configuration.md) | Variable system: static fields, dynamic suppliers, custom types |
| 3 | [Action System (MiniAction, UserAction & UserActionRegistry)](03-action-system.md) | Action hierarchy, factories, conditionals, composites |
| 4a | [Execution (Page 1): Accessing/Updating Fields](04-execution-page1.md) | Reading/writing MetaFieldRegistry from Java, actions, text |
| 4b | [Execution (Page 2): ActionManager & ActionUtils](04-execution-page2.md) | Custom actions, RoadRunner adaptation, trajectory fusion |
| 4c | [Execution (Page 3): RoadRunner Execution via Actions.runBlocking()](04-execution-page3.md) | Deep dive into RR integration, timing, debugging |
| 5 | [Contribution Guide](05-contribution.md) | Bug reports, PR process, adapting for PedroPathing |

---

## Quick Start

### 1. Text File Structure

```
textfiles/
├── GeneralRobotSettings.txt    # Base configuration (loaded first)
├── UserActionSettings.txt      # Composite "Big Action" definitions
└── ACTIVE_MyAuto.txt           # Autonomous routine (prefixed with ACTIVE)
```

### 2. Basic Auto File

```ini
# ACTIVE_MyAuto.txt
Title = My First Auto
Starting = pose2d(0, 0, 0)

# Config overrides (optional)
autoTimer = 30.0

# Action sequence
STRAFE.TO(30, 0, 0)
SPLINE.TO(scorePose, 90, -90)
WAIT(0.5)
SCORE_SAMPLE
```

### 3. Composite Action Definition

```ini
# UserActionSettings.txt
SCORE_SAMPLE = {
    SPLINE.TO(scorePose, 90, -90),
    WAIT(0.3),
    INTAKE.CLOSE
}
```

### 4. Java Registration (ConfigManager.java)

```java
public class ConfigManager {
    public static void init(OpMode opMode) {
        // Types
        registerType(new Pose2d(0, 0, 0));
        registerType(new IntakeSetting("", false, 0));

        // Static fields
        registerField("autoTimer", Double.class, 0.0);
        registerField("scorePose", Pose2d.class, new Pose2d(-72, -67, 0));

        // Dynamic suppliers (live sensors)
        registerField("distance", Double.class, 
            () -> opMode.hardwareMap.get(DistanceSensor.class, "sensor")
                .getDistance(DistanceUnit.CM));

        // Conditions for if/else
        UserActionRegistry.registerCondition("withinDistance", () -> 
            distance <= 20.0);
    }
}
```

### 5. Action Manager (ActionManager.java)

```java
public void init(MecanumDrive drive, Telemetry telemetry) {
    UserActionRegistry.register(new MiniAction("STRAFE.TO", this::strafeToFactory));
    UserActionRegistry.register(new MiniAction("SPLINE.TO", this::splineToFactory));
    UserActionRegistry.register(new MiniAction("WAIT", params -> 
        ActionUtils.wrap(new SleepAction(ActionUtils.asDoubles(params, 1)[0]))));
    // ...
}
```

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                        instantauto (core)                       │
├─────────────────────────────────────────────────────────────────┤
│  configs/                    │  actions/                        │
│  ├── MetaField               │  ├── Action                      │
│  ├── MetaFieldRegistry       │  ├── MetaAction                  │
│  ├── ConfigParser            │  ├── MiniAction                  │
│  └── (text parsing)          │  ├── UserAction                  │
│                              │  ├── UserActionRegistry          │
│                              │  └── AutoParser                  │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        TeamCode (robot)                         │
├─────────────────────────────────────────────────────────────────┤
│  configs/                    │  action/                         │
│  ├── Pose2d                  │  ├── ActionManager               │
│  ├── IntakeSetting           │  ├── ActionUtils                 │
│  └── ConfigManager           │  └── (RR integration)            │
│                              │                                  │
│  opmodes/                    │  roadrunner/                     │
│  ├── AutonomousBase          │  ├── MecanumDrive                │
│  └── TextFileAutos           │  └── Localizers                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## Key Concepts

| Concept | Description |
|---------|-------------|
| **MetaField** | Custom type parsable from text (e.g., `pose2d(x,y,h)`) |
| **ConfigEntry** | Registry entry holding static value OR dynamic supplier |
| **MiniAction** | Java-defined primitive (e.g., `STRAFE.TO`) |
| **UserAction** | Text-defined composite (e.g., `SCORE_SAMPLE`) |
| **BuilderAction** | Fusible trajectory action (chains via `.apply(builder)`) |
| **ActionMerger** | Callback to fuse consecutive BuilderActions |
| **Two-Phase Parse** | Config first (for Starting pose), then Actions (needs MecanumDrive) |

---

## File Locations

| Component | Path |
|-----------|------|
| Core Library | `instantauto/src/main/java/com/example/instantauto/` |
| Robot Configs | `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/configs/` |
| Action Layer | `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/action/` |
| OpModes | `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/opmodes/` |
| Text Files | `textfiles/` |
| Documentation | `doc/instantauto/` |

---

## Next Steps

1. Read [Parser](01-parser.md) to understand text file parsing
2. Read [Configuration](02-configuration.md) for variable system
3. Read [Action System](03-action-system.md) for action architecture
4. Read [Execution Pages](04-execution-page1.md) for runtime details
5. See [Contribution](05-contribution.md) for extending/adapting