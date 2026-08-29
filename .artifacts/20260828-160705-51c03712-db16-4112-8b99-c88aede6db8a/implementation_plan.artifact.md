# Deep Clean: Remove Hardware-Specific Code

## Goal
Remove only the most hardware-specific autonomous/teleop files. Keep all roadrunner dependencies and most TeamCode structure intact.

## User Review Required
- Confirm only First_Auto.java and First_TeleOp.java should be deleted
- Confirm minimal cleaning for ConfigManager and AutonomousBase only

## Proposed Changes - File Cleaning Table

| File | Action | Hardware-Specific Code to Remove |
|------|--------|----------------------------------|
| **ConfigManager.java** | Clean | Remove: `intakeActive` field registration, `IntakeSetting` type registration. Keep: `sysTime`, `gamepadLeftY`, `distance`, `batteryVoltage`, `withinDistance`, `is_active`, `autoTimer`, `redGoalPose`, `blueGoalPose`, `motorName`, `Title`, `Starting`, `Pose2d` type registration |
| **IntakeSetting.java** | Keep + Comment | Add comment stating this is an example configuration class |
| **Pose2d.java** | **Keep As-Is** | Do not modify - keep `getRRPose2d()` method |
| **ActionUtils.java** | **Keep As-Is** | No changes |
| **ActionManager.java** | **Keep As-Is** | No changes |
| **TeleOp.java** | **Keep As-Is** | No changes |
| **First_Auto.java** | **DELETE** | Entire file - hardware-specific autonomous with servos, distance sensors, MecanumDrive |
| **First_TeleOp.java** | **DELETE** | Entire file - hardware-specific teleop with gamepad controls, MecanumDrive |
| **TextFileAutos.java** | **Keep As-Is** | No changes |
| **AutonomousBase.java** | Clean | Remove: `dumpAllFields()` telemetry call in `loop()`, `withinDistance` telemetry. Keep: everything else |
| **ControlGroupAuto.java** | **Keep As-Is** | No changes |
| **TextFileLocationBook.java** | **Keep As-Is** | No changes |
| **MecanumDrive.java** | **Keep As-Is** | No changes |
| **Localizer.java** | **Keep As-Is** | No changes |
| **Drawing.java** | **Keep As-Is** | No changes |
| **OTOSLocalizer.java** | **Keep As-Is** | No changes |
| **PinpointLocalizer.java** | **Keep As-Is** | No changes |
| **TwoDeadWheelLocalizer.java** | **Keep As-Is** | No changes |
| **ThreeDeadWheelLocalizer.java** | **Keep As-Is** | No changes |
| **TankDrive.java** | **Keep As-Is** | No changes |
| **roadrunner/messages/** | **Keep As-Is** | No changes |
| **roadrunner/tuning/** | **Keep As-Is** | No changes |

### instantauto Module - NO CHANGES
Already hardware-agnostic. Contains: Action, AutoParser, MetaAction, MiniAction, UserActionRegistry, ConfigParser, MetaField, MetaFieldRegistry

## Verification Plan

### Automated Tests
```bash
# Build instantauto module (should pass)
./gradlew :instantauto:build

# Build TeamCode module (should pass)
./gradlew :TeamCode:build

# Run any existing unit tests
./gradlew :instantauto:test
```

### Manual Verification
- Verify First_Auto.java and First_TeleOp.java are deleted
- Verify ConfigManager.java no longer registers IntakeSetting/intakeActive
- Verify IntakeSetting.java has example comment
- Verify AutonomousBase.java loop() doesn't dump telemetry
- Verify instantauto module compiles without TeamCode