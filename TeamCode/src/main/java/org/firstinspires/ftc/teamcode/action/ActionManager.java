package org.firstinspires.ftc.teamcode.action;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.ParallelAction;
import com.acmerobotics.roadrunner.SleepAction;
import com.acmerobotics.roadrunner.TrajectoryActionBuilder;
import com.acmerobotics.roadrunner.Vector2d;
import com.example.instantauto.actions.Action;
import com.example.instantauto.actions.MiniAction;
import com.example.instantauto.actions.UserActionRegistry;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.configs.Pose2d;
import org.firstinspires.ftc.teamcode.roadrunner.MecanumDrive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ActionManager {
    TrajectoryActionBuilder builder;
    MecanumDrive mecanumDrive;
    Telemetry telemetry;
    public void init(MecanumDrive drivebase, Telemetry telemetry) {
        // Register Primitives (Mini Actions)
        UserActionRegistry.register(new MiniAction("GO.TO.POSE2D", this::goToPoseFactory));

        UserActionRegistry.register(new MiniAction("PRINT", obj ->
                ActionUtils.wrap(new PrintAction(ActionUtils.asString(obj)))));

        UserActionRegistry.register(new MiniAction("PARALLEL", params -> {
            List<Action> actions = ActionUtils.asActions(params);
            if (actions == null) return null;

            List<com.acmerobotics.roadrunner.Action> rrActions = new ArrayList<>();
            for (Action a : actions) {
                rrActions.add(ActionUtils.adapt(a));
            }
            return ActionUtils.wrap(new com.acmerobotics.roadrunner.ParallelAction(rrActions));
        }));

//        UserActionRegistry.register(new MiniAction("RACE", params -> {
//            List<Action> actions = ActionUtils.asActions(params);
//            return actions != null ? new RaceAction(actions) : null;
//        }));

        UserActionRegistry.register(new MiniAction("WAIT", params -> {
            double[] d = ActionUtils.asDoubles(params, 1);
            return d != null ? ActionUtils.wrap(new com.acmerobotics.roadrunner.SleepAction(d[0])) : null;
        }));

        UserActionRegistry.register(new MiniAction("HELLO.WORLD", params -> ActionUtils.wrap(new PrintAction("Hello World!"))));
        this.mecanumDrive = drivebase;
        this.telemetry = telemetry;
    }

    private Action goToPoseFactory(Object params) {
        // Handle Case 1: Received a Pose2d object (Variable Lookup)
        if (params instanceof Pose2d) {
            Pose2d p = (Pose2d) params;
            return ActionUtils.wrap(this.goToPoseAction(p.x, p.y, p.heading));
        }

        // Handle Case 2: Received a String (Literal Parameters "x, y, h")
        double[] d = ActionUtils.asDoubles(params, 3);
        if (d != null) {
            return ActionUtils.wrap(this.goToPoseAction(d[0], d[1], d[2]));
        }
        return null;
    }


    public com.acmerobotics.roadrunner.Action goToPoseAction(double x, double y, double headingDegree) {
        if (builder == null) {
            builder = mecanumDrive.actionBuilder(mecanumDrive.localizer.getPose());
        }
        builder = builder.strafeToLinearHeading(new Vector2d(x, y), Math.toRadians(headingDegree));
        TrajectoryActionBuilder oldBuilder = builder;
        builder = builder.fresh(); // continue from last end
        return oldBuilder.build();
    }

    public class PrintAction implements com.acmerobotics.roadrunner.Action {
        String message;

        public PrintAction(String message) {
            this.message = message;
        }

        public PrintAction(double n) {
            this.message = String.format(Locale.US, "%.2f", n);
        }

        public PrintAction(int n) {
            this.message = String.format(Locale.US, "%d", n);
        }

        public PrintAction(boolean b) {
            this.message = String.format(Locale.US, "%b", b);
        }

        @Override
        public boolean run(@NonNull TelemetryPacket telemetryPacket) {
            telemetry.addData("PRINT", message);
            return false;
        }
    }

/*
    Too bad roadrunner does not have this feature.
 */
//    public static class RaceAction implements Action {
//        private final List<Action> actions;
//
//        public RaceAction(Action... actions) {
//            this.actions = new ArrayList<>(Arrays.asList(actions));
//        }
//
//        public RaceAction(List<Action> actions) {
//            this.actions = new ArrayList<>(actions);
//        }
//
//        @Override
//        public boolean run() {
//            boolean anyFinished = false;
//            for (Action action : actions) {
//                if (!action.run()) {
//                    anyFinished = true;
//                }
//            }
//            return !anyFinished;
//        }
//    }

}
