# Fix: Nested If/Else Actions Not Fused into Smooth Motions

## Problem
After fixing if/else execution, consecutive `STRAFE.TO`/`SPLINE.TO` actions inside if/else blocks run sequentially with stops instead of as smooth fused trajectories.

## Root Cause
- **Top-level actions**: Fused by `ActionUtils.asActions()` → `merge()` in `AutonomousBase.start()`
- **If/else nested actions**: Parsed by `UserActionRegistry.parseActionsFromBlock()` during `init()` - **no fusion occurs**
- `UserActionRegistry` (in `instantauto` module) cannot access `ActionUtils.merge()` (in `TeamCode` module) due to module dependency direction

## Solution
Add a **callback mechanism** in `UserActionRegistry` that allows `TeamCode` to provide a fusion function for nested actions.

### Files to Modify

1. **`instantauto/src/main/java/com/example/instantauto/actions/UserActionRegistry.java`**
   - Add static `actionMerger` function field (default: identity)
   - Add `setActionMerger()` method
   - Modify `parseActionsFromBlock()` to apply merger

2. **`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/action/ActionUtils.java`**
   - Add public static `mergeNestedActions(List<Action>, MecanumDrive)` method
   - This recursively traverses action tree and fuses consecutive `BuilderAction`s at any nesting level
   - Handle if/else actions (anonymous classes) by reflecting into their `targetActions` field OR use a marker interface

3. **`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/opmodes/AutonomousBase.java`**
   - Register merger callback in `init()`: `UserActionRegistry.setActionMerger(actions -> ActionUtils.mergeNestedActions(actions, mecanumDrive))`

### Technical Details

The if/else `Action` is an anonymous class in `UserActionRegistry.createAction()` with private `targetActions` field. Since we can't access it directly, the merger should be applied **when the block is parsed** (in `parseActionsFromBlock()`), not later.

This means `parseActionsFromBlock()` should return already-fused actions. The callback receives the list of parsed actions and returns a fused list.

### Verification
1. Build: `./gradlew :TeamCode:assembleDebug`
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
3. Verify both branches execute as smooth continuous trajectories