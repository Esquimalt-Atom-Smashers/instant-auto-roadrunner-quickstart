# Refactor WaitAction to use Roadrunner SleepAction

The goal is to replace the custom `WaitAction` implementation in `ActionManager` with the native `com.acmerobotics.roadrunner.SleepAction`.

## Proposed Changes

### [Action] Component

#### [ActionManager.java](file:///C:/Users/bco44/Documents/GitHub/instant-auto-roadrunner-quickstart/TeamCode/src/main/java/org/firstinspires/ftc/teamcode/action/ActionManager.java)

- Update the `WAIT` registration to use `com.acmerobotics.roadrunner.SleepAction`.
- Delete the internal `WaitAction` class.

```java
        UserActionRegistry.register(new MiniAction("WAIT", params -> {
            double[] d = ActionUtils.asDoubles(params, 1);
            return d != null ? ActionUtils.wrap(new com.acmerobotics.roadrunner.SleepAction(d[0])) : null;
        }));
```

## Verification Plan

### Automated Tests
- I will run a build to ensure type compatibility.
- Command: `./gradlew :TeamCode:assembleDebug`

### Manual Verification
- I will verify that `ActionManager.java` no longer contains the custom `WaitAction` class.
