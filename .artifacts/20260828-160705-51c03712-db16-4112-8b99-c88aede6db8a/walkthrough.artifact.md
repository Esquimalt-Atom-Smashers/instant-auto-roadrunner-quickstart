# Deep Clean Walkthrough

## Summary
Performed a targeted deep clean of the codebase to remove the most hardware-specific autonomous/teleop files while preserving all roadrunner dependencies and the core instantauto library.

## Changes Made

### Deleted Files
| File | Reason |
|------|--------|
| `First_Auto.java` | Hardware-specific autonomous with servos, distance sensors, MecanumDrive |
| `First_TeleOp.java` | Hardware-specific teleop with gamepad controls, MecanumDrive |

### Cleaned Files
| File | Changes |
|------|---------|
| `ConfigManager.java` | Removed `intakeActive` field registration and `IntakeSetting` type registration. Kept `sysTime`, `gamepadLeftY`, `batteryVoltage`, `is_active` condition, and all pose/field registrations. |
| `IntakeSetting.java` | Added Javadoc comment marking it as an example/template configuration class. |
| `AutonomousBase.java` | Cleaned `loop()` method - removed hardware-specific telemetry (`dumpAllFields()`, `withinDistance` condition). Added comment indicating override for custom telemetry. |

### Preserved (No Changes)
- **All roadrunner files**: MecanumDrive, TankDrive, Localizers (OTOS, Pinpoint, TwoDeadWheel, ThreeDeadWheel), Drawing, messages/, tuning/
- **All action/config infrastructure**: ActionUtils, ActionManager, TextFileAutos, TextFileLocationBook, TeleOp, ControlGroupAuto, Pose2d
- **instantauto module**: Completely untouched (already hardware-agnostic)

## Verification

### Build Results
```
:TeamCode:build - SUCCESS
:instantauto:build - SUCCESS
```

### Key Preserved Patterns
1. **AutoParser two-phase parsing** in AutonomousBase (config → hardware init → actions)
2. **UserActionRegistry merger** for nested if/else action fusion
3. **ConfigManager field/condition registration** pattern
4. **RoadRunner trajectory actions** (STRAFE.TO, SPLINE.TO, PARALLEL, RACE, WAIT)
5. **MecanumDrive/TankDrive** with all localizer implementations
6. **TextFileAutos** OpModeRegistrar pattern for dynamic auto loading

## Usage
The cleaned codebase now serves as a template where teams:
1. Create their own autonomous files in the ACTIVE folder
2. Configure `ConfigManager.init()` with their robot's sensors/hardware
3. Define custom actions in `UserActionSettings.txt`
4. Extend `AutonomousBase` or create new OpModes following the preserved patterns