# Fix: Parallel Action Errors in First_Auto.java

## Problem
The First_Auto.java file has several issues with its ParallelAction usage:
1. Invalid Java syntax: if/else statements directly in ParallelAction constructor
2. Invalid servo position: -0.5 (should be 0-1 range)
3. Missing Action wrapper for servo control operations
4. Incorrect ParallelAction structure

## Root Cause
The user is attempting to put Java control flow statements (if/else) directly inside a RoadRunner ParallelAction constructor, which is invalid syntax. Additionally, they're trying to execute direct hardware control without wrapping it in a proper Action implementation.

## Solution
1. Create a custom ServoAction class that implements com.acmerobotics.roadrunner.Action
2. Fix the ParallelAction usage to properly wrap RoadRunner trajectories and ServoActions
3. Correct servo position values to valid range (0-1)
4. Restructure the autonomous logic for proper parallel execution

## Files to Modify
- TeamCode/src/main/java/org/firstinspires/ftc/teamcode/opmodes/First_Auto.java

## Changes
1. Add ServoAction inner class implementing Action interface
2. Fix ParallelAction constructor to contain valid Action instances
3. Correct servo position values
4. Structure the autonomous sequence properly

## Verification
1. Build success: ./gradlew :TeamCode:assembleDebug
2. Verify no syntax errors in First_Auto.java
3. Test that parallel execution works as intended (driving + servo control)