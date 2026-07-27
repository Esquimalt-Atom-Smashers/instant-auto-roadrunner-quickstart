package org.firstinspires.ftc.teamcode.action;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.TrajectoryActionBuilder;
import com.acmerobotics.roadrunner.Vector2d;
import com.example.instantauto.actions.Action;
import com.example.instantauto.actions.MiniAction;
import com.example.instantauto.actions.UserActionRegistry;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.configs.Pose2d;
import org.firstinspires.ftc.teamcode.roadrunner.MecanumDrive;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ActionManager {
    TrajectoryActionBuilder builder;
    MecanumDrive mecanumDrive;
    Telemetry telemetry;
    public void init(MecanumDrive drivebase, Telemetry telemetry) {
        // Register Primitives (Mini Actions)
        UserActionRegistry.register(new MiniAction("STRAFE.TO", this::strafeToFactory));

        UserActionRegistry.register(new MiniAction("SPLINE.TO", this::splineToFactory));

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

        UserActionRegistry.register(new MiniAction("RACE", params -> {
            List<Action> actions = ActionUtils.asActions(params);
            if (actions == null) return null;

            List<com.acmerobotics.roadrunner.Action> rrActions = new ArrayList<>();
            for (Action a : actions) {
                rrActions.add(ActionUtils.adapt(a));
            }
            return ActionUtils.wrap(new RaceAction(rrActions));
        }));

        UserActionRegistry.register(new MiniAction("WAIT", params -> {
            double[] d = ActionUtils.asDoubles(params, 1);
            return d != null ? ActionUtils.wrap(new com.acmerobotics.roadrunner.SleepAction(d[0])) : null;
        }));

        UserActionRegistry.register(new MiniAction("HELLO.WORLD", params -> ActionUtils.wrap(new PrintAction("Hello World!"))));
        this.mecanumDrive = drivebase;
        this.telemetry = telemetry;
    }

    private Action strafeToFactory(Object params) {
        // Handle Case 1: Received a Pose2d object (Variable Lookup)
        if (params instanceof Pose2d) {
            Pose2d p = (Pose2d) params;
            return ActionUtils.wrap(this.strafeToAction(p.x, p.y, p.heading));
        }

        // Handle Case 2: Received a String (Literal Parameters "x, y, h")
        double[] d = ActionUtils.asDoubles(params, 3);
        if (d != null) {
            return ActionUtils.wrap(this.strafeToAction(d[0], d[1], d[2]));
        }
        return null;
    }

    private Action splineToFactory(Object params) {
        if (params instanceof String) {
            String s = (String) params;
            String[] parts = s.split(",");

            // Handle Case 1: "x, y, heading, startTan, endTan" (5 doubles)
            if (parts.length == 5) {
                double[] d = ActionUtils.asDoubles(s, 5);
                if (d != null) {
                    return ActionUtils.wrap(this.splineToAction(d[0], d[1], d[2], d[3], d[4]));
                }
            }

            // Handle Case 2: "poseName, startTan, endTan"
            if (parts.length == 3) {
                String poseName = parts[0].trim();
                com.example.instantauto.configs.MetaFieldRegistry.ConfigEntry<?> entry =
                        com.example.instantauto.configs.MetaFieldRegistry.getEntry(poseName);
                if (entry != null && entry.value instanceof Pose2d) {
                    Pose2d p = (Pose2d) entry.value;
                    try {
                        double startTan = Double.parseDouble(parts[1].trim());
                        double endTan = Double.parseDouble(parts[2].trim());
                        return ActionUtils.wrap(this.splineToAction(p.x, p.y, p.heading, startTan, endTan));
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        return null;
    }

    public com.acmerobotics.roadrunner.Action strafeToAction(double x, double y, double headingDegree) {
        if (builder == null) {
            builder = mecanumDrive.actionBuilder(mecanumDrive.localizer.getPose());
        }
        builder = builder.strafeToSplineHeading(new Vector2d(x, y), Math.toRadians(headingDegree));
        TrajectoryActionBuilder oldBuilder = builder;
        builder = builder.fresh(); // continue from last end
        return oldBuilder.build();
    }

    public com.acmerobotics.roadrunner.Action splineToAction(double x, double y, double headingDegree, double pathStartDeg, double pathEndDeg) {
        if (builder == null) {
            builder = mecanumDrive.actionBuilder(mecanumDrive.localizer.getPose());
        }
        
        builder = builder.setTangent(Math.toRadians(pathStartDeg))
        .splineToSplineHeading(new com.acmerobotics.roadrunner.Pose2d(x, y, Math.toRadians(headingDegree)), Math.toRadians(pathEndDeg));
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

    public static class RaceAction implements com.acmerobotics.roadrunner.Action {
        private final List<com.acmerobotics.roadrunner.Action> actions;

        public RaceAction(List<com.acmerobotics.roadrunner.Action> actions) {
            this.actions = actions;
        }

        @Override
        public boolean run(@NonNull TelemetryPacket packet) {
            boolean allRunning = true;
            for (com.acmerobotics.roadrunner.Action action : actions) {
                if (!action.run(packet)) {
                    allRunning = false;
                }
            }
            return allRunning && !actions.isEmpty();
        }
    }

}
