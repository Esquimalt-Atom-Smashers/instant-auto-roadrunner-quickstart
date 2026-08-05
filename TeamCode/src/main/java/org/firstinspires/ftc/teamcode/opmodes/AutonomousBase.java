package org.firstinspires.ftc.teamcode.opmodes;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.InstantAction;
import com.acmerobotics.roadrunner.ParallelAction;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.RaceAction;
import com.acmerobotics.roadrunner.SequentialAction;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.example.instantauto.actions.Action;
import com.example.instantauto.actions.AutoParser;
import com.example.instantauto.actions.UserActionRegistry;
import com.example.instantauto.configs.ConfigParser;
import com.example.instantauto.configs.MetaFieldRegistry;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DistanceSensor;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.roadrunner.MecanumDrive;
import org.firstinspires.ftc.teamcode.action.ActionManager;
import org.firstinspires.ftc.teamcode.action.ActionUtils;
import org.firstinspires.ftc.teamcode.configs.ConfigManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AutonomousBase extends OpMode {
    private final AutoParser autoParser;
    private ActionManager actionManager;
    private final File autoFile;
    private List<com.acmerobotics.roadrunner.Action> actions;
    private MecanumDrive mecanumDrive;
    private ConfigParser configParser = new ConfigParser();
    private DistanceSensor distanceSensor;

    public AutonomousBase(AutoParser autoParser, File autoFile) {
        this.autoParser = autoParser;
        this.autoFile = autoFile;
        actions = new ArrayList<>();
    }

    @Override
    public void init() {
        ConfigManager.init();
        actionManager = new ActionManager();
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
        distanceSensor = hardwareMap.get(DistanceSensor.class, "distanceSensor");

        // Phase 1: Parse configuration to get the starting pose
        autoParser.parseAutoConfig(autoFile);
        configParser.userUpdateEntry("sysTime", System.nanoTime());

        Pose2d pose;
        try {
            org.firstinspires.ftc.teamcode.configs.Pose2d pose_wrapped = (org.firstinspires.ftc.teamcode.configs.Pose2d) MetaFieldRegistry.getEntry("Starting").value;
            pose = pose_wrapped.getRRPose2d();
        } catch (Exception e) {
            throw new RuntimeException("Invalid Starting Pose: MUST BE POSE2D");
        }

        // Phase 2: Initialize hardware with the correct pose
        mecanumDrive = new MecanumDrive(hardwareMap, pose);
        actionManager.init(mecanumDrive, telemetry);

        // Register nested action merger for if/else blocks (must be before parseActions)
        UserActionRegistry.setActionMerger(actions -> ActionUtils.mergeNestedActions(actions, mecanumDrive));

        // Phase 3: Parse actions (now that primitives are registered by actionManager)
        autoParser.parseActions();

        MetaFieldRegistry.ConfigEntry<?> titleEntry = MetaFieldRegistry.getEntry("Title");
        if (titleEntry != null && titleEntry.value != null && !titleEntry.value.toString().trim().isEmpty()) {
            telemetry.addLine("Auto Title: " + titleEntry.value);
        }

        List<String> loadErrors = UserActionRegistry.getLoadErrors();
        if (!loadErrors.isEmpty()) {
            System.out.println("\n[USER ACTION ERRORS/WARNINGS]:");
            for (String log : loadErrors) telemetry.addLine("  " + log);
        }

        List<String> configLogs = autoParser.getConfigLogs();
        if (!configLogs.isEmpty()) {
            System.out.println("\n[CONFIG ERRORS/WARNINGS]:");
            for (String log : configLogs) telemetry.addLine("  " + log);
        }

        List<String> actionErrors = autoParser.getActionErrors();
        if (!actionErrors.isEmpty()) {
            System.out.println("\n[ACTION ERRORS]:");
            for (String err : actionErrors) telemetry.addLine("  " + err);
        }
        UserActionRegistry.registerCondition("withinDistance", () -> (distanceSensor.getDistance(DistanceUnit.CM) <= 20.0));

        telemetry.update();
    }
    @Override
    public void start() {
        // Clear actions before re-parsing with merging
        actions.clear();
        List<Action> mergedActions = ActionUtils.asActions(autoParser.getActionContent(), mecanumDrive);
        if (mergedActions != null) {
            // Also merge any nested actions in top-level actions (e.g., if/else at top level)
            mergedActions = ActionUtils.mergeNestedActions(mergedActions, mecanumDrive);
            for (Action action : mergedActions) {
                actions.add(ActionUtils.adapt(action, telemetry));
            }
        }
        Actions.runBlocking(
                new RaceAction(
                        new SequentialAction(actions),
                        telemetryAction(this)
                )
        );

    }

    @Override
    public void loop() {
        dumpAllFields();
        telemetry.addData("withinDistance", UserActionRegistry.evaluateCondition("withinDistance"));
        telemetry.update();
    }
    @Override
    public void stop() {
        MetaFieldRegistry.clear();
        UserActionRegistry.clear();
    }

    public class TelemetryAction implements com.acmerobotics.roadrunner.Action {
        private OpMode opMode;
        public TelemetryAction(OpMode opMode) {
            this.opMode = opMode;
        }
        @Override
        public boolean run(@NonNull TelemetryPacket telemetryPacket) {
            configParser.userUpdateEntry("sysTime", System.nanoTime());
            System.out.println("sysTime: " + MetaFieldRegistry.getEntry("sysTime").value);
            telemetry.update();
            return true;
        }
    }

    public com.acmerobotics.roadrunner.Action telemetryAction(OpMode opMode) {return new TelemetryAction(opMode);}
    private void printField(String name) {
        MetaFieldRegistry.ConfigEntry<?> entry = MetaFieldRegistry.getEntry(name);
        if (entry != null) {
            telemetry.addLine(entry.fieldName + ": " + entry.value);
        } else {
            telemetry.addLine(name + ": [Not Registered]");
        }
    }

    private void dumpAllFields() {
        List<String> registeredIdentifiers = MetaFieldRegistry.getAllRegisteredFieldNames();
        for (String identifier : registeredIdentifiers) {
            printField(identifier);
        }
    }
}
