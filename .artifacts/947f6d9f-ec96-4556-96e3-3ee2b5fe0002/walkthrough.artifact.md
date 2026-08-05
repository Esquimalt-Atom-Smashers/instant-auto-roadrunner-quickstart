# Fix: If/Else False Action Not Executing Properly

## Problem
When using if/else blocks in autonomous text files, actions in the `else` branch (like `STRAFE.TO()`) did not execute properly - the robot would start the movement but never complete it. The `if` branch worked fine when it used actions like `RACE` that create proper RoadRunner actions.

## Root Cause
- **Top-level actions** are processed by `ActionUtils.asActions()` which calls `merge()` to fuse consecutive `BuilderAction`s into a single RoadRunner trajectory action (`WrappedRRAction`). This fused action persists across loop iterations and runs to completion.

- **If/else branch actions** are parsed by `UserActionRegistry.parseActionsFromBlock()` which does **NOT** call `merge()`. The `STRAFE.TO` and `SPLINE.TO` actions in if/else branches remain as standalone `BuilderAction` instances.

- **Standalone `BuilderAction.run()`** rebuilt the trajectory from the current pose on **every loop iteration**:
  ```java
  @Override
  public boolean run() {
      return apply(mecanumDrive.actionBuilder(mecanumDrive.localizer.getPose())).build().run(new TelemetryPacket());
  }
  ```
  This caused the trajectory to restart from the beginning every loop, never completing.

- The `if` branch in tests worked because it used `RACE`/`PARALLEL` which create proper RoadRunner actions directly, not `BuilderAction`s.

## Solution
Modified `ActionManager.java` to wrap `STRAFE.TO` and `SPLINE.TO` `BuilderAction`s with a **caching wrapper** that builds the trajectory once on first `run()` and reuses it on subsequent calls.

### Changes Made
**File:** `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/action/ActionManager.java`

1. **Added `createCachedBuilderAction()` helper method** - Wraps a `BuilderAction` delegate to cache the built `TrajectoryAction` on first run.

2. **Modified `strafeToFactory()`** - Both code paths (Pose2d variable lookup and literal parameters) now return cached `BuilderAction`s.

3. **Modified `splineToFactory()`** - Both code paths (5-parameter literal and pose-name with tangents) now return cached `BuilderAction`s.

### How It Works
```java
private Action createCachedBuilderAction(ActionUtils.BuilderAction delegate) {
    return new ActionUtils.BuilderAction() {
        private com.acmerobotics.roadrunner.Action cachedAction;

        @Override
        public TrajectoryActionBuilder apply(TrajectoryActionBuilder builder) {
            return delegate.apply(builder);
        }

        @Override
        public boolean run() {
            if (cachedAction == null) {
                // Build trajectory ONCE using the pose at action start
                cachedAction = apply(mecanumDrive.actionBuilder(mecanumDrive.localizer.getPose())).build();
            }
            // Reuse the same trajectory action on subsequent runs
            return cachedAction.run(new TelemetryPacket());
        }
    };
}
```

### Compatibility with Top-Level Fusion
- When `STRAFE.TO`/`SPLINE.TO` are used at the top level consecutively, `ActionUtils.merge()` still fuses them into a single trajectory (the `apply()` method is used for fusion).
- The caching only affects the `run()` method, which is bypassed when actions are fused (the fused `WrappedRRAction` runs the combined trajectory directly).
- No behavioral change for existing top-level action sequences.

## Verification
1. ✅ Build successful: `./gradlew :TeamCode:assembleDebug`
2. Test with autonomous file containing if/else:
   ```
   if(someCondition){
       STRAFE.TO(10, 0, 0)
   }else{
       STRAFE.TO(0, 10, 90)
   }
   ```
3. Both branches should now execute the strafe to completion.

---

# Fix: Nested If/Else Actions Not Fused into Smooth Motions

## Problem
After fixing if/else execution, consecutive `STRAFE.TO`/`SPLINE.TO` actions inside if/else blocks ran sequentially with stops instead of as smooth fused trajectories.

## Root Cause
- **Top-level actions**: Fused by `ActionUtils.asActions()` → `merge()` in `AutonomousBase.start()`
- **If/else nested actions**: Parsed by `UserActionRegistry.parseActionsFromBlock()` during `init()` - **no fusion occurred**
- `UserActionRegistry` (in `instantauto` module) cannot access `ActionUtils.merge()` (in `TeamCode` module) due to module dependency direction (`instantauto` is a dependency of `TeamCode`)

## Solution
Added a **callback mechanism** in `UserActionRegistry` that allows `TeamCode` to provide a fusion function for nested actions.

### Files Modified

1. **`instantauto/src/main/java/com/example/instantauto/actions/UserActionRegistry.java`**
   - Added static `actionMerger` function field (default: identity function)
   - Added `setActionMerger(Function<List<Action>, List<Action>>)` method
   - Modified `parseActionsFromBlock()` to apply merger to parsed actions

2. **`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/action/ActionUtils.java`**
   - Added public static `mergeNestedActions(List<Action>, MecanumDrive)` method
   - Recursively traverses action tree and fuses consecutive `BuilderAction`s at any nesting level
   - Uses reflection to find and merge nested actions in if/else anonymous classes (`targetActions`, `trueActions` fields)

3. **`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/opmodes/AutonomousBase.java`**
   - Register merger callback in `init()`: `UserActionRegistry.setActionMerger(actions -> ActionUtils.mergeNestedActions(actions, mecanumDrive))`
   - Also apply nested merging to top-level actions in `start()`

### How It Works
```java
// In UserActionRegistry.parseActionsFromBlock():
private static List<Action> parseActionsFromBlock(String block) {
    List<Action> actions = new ArrayList<>();
    for (String sub : splitByTopLevelCommas(block)) {
        Action a = createAction(sub);
        if (a != null) actions.add(a);
    }
    // Apply fusion/merging for nested actions (e.g., consecutive BuilderActions)
    return actionMerger.apply(actions);
}

// In AutonomousBase.init():
UserActionRegistry.setActionMerger(actions -> ActionUtils.mergeNestedActions(actions, mecanumDrive));
```

The `mergeNestedActions` method:
1. Groups consecutive `BuilderAction` instances
2. Fuses each group into a single trajectory using `fuse()`
3. Recursively processes nested actions in composite actions (via reflection for if/else blocks)
4. Returns a new list with fused actions

## Verification
1. ✅ Build successful: `./gradlew :TeamCode:assembleDebug`
2. Test with autonomous file:
   ```
   if(withinDistance){
       STRAFE.TO(10, 0, 0)
       STRAFE.TO(10, 10, 90)
   }else{
       SPLINE.TO(0, 10, 0, 90, 0)
       SPLINE.TO(10, 0, 90, 0, 90)
   }
   ```
3. Both branches should execute as smooth continuous trajectories.

## Related Files
- `instantauto/src/main/java/com/example/instantauto/actions/UserActionRegistry.java` - If/else parsing + merger callback
- `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/action/ActionUtils.java` - `mergeNestedActions()` + reflection-based nested merging
- `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/opmodes/AutonomousBase.java` - Callback registration
- `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/action/ActionManager.java` - Cached BuilderAction wrapper