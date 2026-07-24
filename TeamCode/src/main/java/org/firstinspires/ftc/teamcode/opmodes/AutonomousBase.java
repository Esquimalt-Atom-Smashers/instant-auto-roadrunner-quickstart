package org.firstinspires.ftc.teamcode.opmodes;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.SequentialAction;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.example.instantauto.actions.Action;
import com.example.instantauto.actions.AutoParser;
import com.example.instantauto.actions.UserActionRegistry;
import com.example.instantauto.configs.MetaFieldRegistry;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.action.ActionManager;
import org.firstinspires.ftc.teamcode.action.ActionUtils;
import org.firstinspires.ftc.teamcode.configs.ConfigManager;
import org.firstinspires.ftc.teamcode.roadrunner.MecanumDrive;

import java.io.File;
import java.util.List;

public class AutonomousBase extends OpMode {
    private final AutoParser autoParser;
    private ActionManager actionManager;
    private final File autoFile;
    private List<com.acmerobotics.roadrunner.Action> actions;
    private MecanumDrive mecanumDrive;

    public AutonomousBase(AutoParser autoParser, File autoFile) {
        this.autoParser = autoParser;
        this.autoFile = autoFile;
    }

    @Override
    public void init() {
        autoParser.parse(autoFile);
        ConfigManager.init();
        actionManager = new ActionManager();

        Pose2d pose;
        try {
            pose = (Pose2d) MetaFieldRegistry.getEntry("Starting").value;
        } catch (Exception e) {
            throw new RuntimeException("Invalid Starting Pose: MUST BE POSE2D");
        }
        mecanumDrive = new MecanumDrive(hardwareMap, pose);
        actionManager.init(mecanumDrive, telemetry);

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
        telemetry.update();
    }

    @Override
    public void start() {
        for (Action action : autoParser.getActions()) {
            actions.add(ActionUtils.adapt(action));
        }
        Actions.runBlocking(
                new SequentialAction(actions)
        );
    }

    @Override
    public void loop() {
    }
}
