package org.firstinspires.ftc.teamcode.action;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.TrajectoryActionBuilder;
import com.acmerobotics.roadrunner.Vector2d;
import com.example.instantauto.actions.Action;
import com.example.instantauto.actions.MiniAction;
import com.example.instantauto.actions.UserActionRegistry;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.roadrunner.MecanumDrive;
import org.firstinspires.ftc.teamcode.configs.Pose2d;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ActionManager {
    MecanumDrive mecanumDrive;
    Telemetry telemetry;
    public void init(MecanumDrive drivebase, Telemetry telemetry) {
        // Register Primitives (Mini Actions)
        UserActionRegistry.register(new MiniAction("STRAFE.TO", this::strafeToFactory));

        UserActionRegistry.register(new MiniAction("SPLINE.TO", this::splineToFactory));

        UserActionRegistry.register(new MiniAction("PRINT", obj ->
                ActionUtils.wrap(new PrintAction(obj))));

        UserActionRegistry.register(new MiniAction("PARALLEL", params -> {
            List<Action> actions = ActionUtils.asActions(params, mecanumDrive);
            if (actions == null) return null;

            List<com.acmerobotics.roadrunner.Action> rrActions = new ArrayList<>();
            for (Action a : actions) {
                rrActions.add(ActionUtils.adapt(a, telemetry));
            }
            return ActionUtils.wrap(new com.acmerobotics.roadrunner.ParallelAction(rrActions));
        }));

        UserActionRegistry.register(new MiniAction("RACE", params -> {
            List<Action> actions = ActionUtils.asActions(params, mecanumDrive);
            if (actions == null) return null;

            List<com.acmerobotics.roadrunner.Action> rrActions = new ArrayList<>();
            for (Action a : actions) {
                rrActions.add(ActionUtils.adapt(a, telemetry));
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

    /**
     * Wraps a BuilderAction to cache the built trajectory on first run.
     * This prevents re-building the trajectory on every loop iteration,
     * which is essential for actions used inside if/else blocks that aren't fused by merge().
     */
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
                    cachedAction = apply(mecanumDrive.actionBuilder(mecanumDrive.localizer.getPose())).build();
                }
                return cachedAction.run(new TelemetryPacket());
            }
        };
    }

    private Action strafeToFactory(Object params) {
        // Handle Case 1: Received a variable name (String) that contains a Pose2d
        if (params instanceof String) {
            String varName = (String) params;
            com.example.instantauto.configs.MetaFieldRegistry.ConfigEntry<?> entry =
                    com.example.instantauto.configs.MetaFieldRegistry.getEntry(varName);
            if (entry != null && entry.getValue() instanceof Pose2d) {
                final Pose2d p = (Pose2d) entry.getValue();
                ActionUtils.BuilderAction builderAction = new ActionUtils.BuilderAction() {
                    @Override
                    public TrajectoryActionBuilder apply(TrajectoryActionBuilder builder) {
                        return builder.strafeToSplineHeading(new Vector2d(p.x, p.y), Math.toRadians(p.heading));
                    }
                    @Override
                    public boolean run() {
                        return apply(mecanumDrive.actionBuilder(mecanumDrive.localizer.getPose())).build().run(new TelemetryPacket());
                    }
                };
                return createCachedBuilderAction(builderAction);
            }
        }

        // Handle Case 2: Received a String (Literal Parameters "x, y, h")
        final double[] d = ActionUtils.asDoubles(params, 3);
        if (d != null) {
            ActionUtils.BuilderAction builderAction = new ActionUtils.BuilderAction() {
                @Override
                public TrajectoryActionBuilder apply(TrajectoryActionBuilder builder) {
                    return builder.strafeToSplineHeading(new Vector2d(d[0], d[1]), Math.toRadians(d[2]));
                }
                @Override
                public boolean run() {
                    return apply(mecanumDrive.actionBuilder(mecanumDrive.localizer.getPose())).build().run(new TelemetryPacket());
                }
            };
            return createCachedBuilderAction(builderAction);
        }
        return null;
    }

    private Action splineToFactory(Object params) {
        if (params instanceof String) {
            String s = (String) params;
            String[] parts = s.split(",");

            // Handle Case 1: "x, y, heading, startTan, endTan" (5 doubles)
            if (parts.length == 5) {
                final double[] d = ActionUtils.asDoubles(s, 5);
                if (d != null) {
                    ActionUtils.BuilderAction builderAction = new ActionUtils.BuilderAction() {
                        @Override
                        public TrajectoryActionBuilder apply(TrajectoryActionBuilder builder) {
                            return builder.setTangent(Math.toRadians(d[3]))
                                    .splineToSplineHeading(new com.acmerobotics.roadrunner.Pose2d(d[0], d[1], Math.toRadians(d[2])), Math.toRadians(d[4]));
                        }
                        @Override
                        public boolean run() {
                            return apply(mecanumDrive.actionBuilder(mecanumDrive.localizer.getPose())).build().run(new TelemetryPacket());
                        }
                    };
                    return createCachedBuilderAction(builderAction);
                }
            }

            // Handle Case 2: "poseName, startTan, endTan"
            if (parts.length == 3) {
                String poseName = parts[0].trim();
                com.example.instantauto.configs.MetaFieldRegistry.ConfigEntry<?> entry =
                        com.example.instantauto.configs.MetaFieldRegistry.getEntry(poseName);
                if (entry != null && entry.getValue() instanceof Pose2d) {
                    final Pose2d p = (Pose2d) entry.getValue();
                    try {
                        final double startTan = Double.parseDouble(parts[1].trim());
                        final double endTan = Double.parseDouble(parts[2].trim());
                        ActionUtils.BuilderAction builderAction = new ActionUtils.BuilderAction() {
                            @Override
                            public TrajectoryActionBuilder apply(TrajectoryActionBuilder builder) {
                                return builder.setTangent(Math.toRadians(startTan))
                                        .splineToSplineHeading(new com.acmerobotics.roadrunner.Pose2d(p.x, p.y, Math.toRadians(p.heading)), Math.toRadians(endTan));
                            }
                            @Override
                            public boolean run() {
                                return apply(mecanumDrive.actionBuilder(mecanumDrive.localizer.getPose())).build().run(new TelemetryPacket());
                            }
                        };
                        return createCachedBuilderAction(builderAction);
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        return null;
    }

    public class PrintAction implements com.acmerobotics.roadrunner.Action {
        String message;
        String varName = null;

        public PrintAction(Object params) {
            if (params instanceof String) {
                String s = (String) params;
                if (s.startsWith("\"") && s.endsWith("\"")) {
                    this.message = s.substring(1, s.length() - 1);
                } else {
                    this.varName = s;
                    this.message = s;
                }
            } else {
                this.message = ActionUtils.asString(params);
            }
        }

        @Override
        public boolean run(@NonNull TelemetryPacket telemetryPacket) {
            String finalOutput = message;
            if (varName != null) {
                com.example.instantauto.configs.MetaFieldRegistry.ConfigEntry<?> entry =
                        com.example.instantauto.configs.MetaFieldRegistry.getEntry(varName);
                if (entry != null) {
                    finalOutput = ActionUtils.asString(entry.getValue());
                }
            }
            telemetry.clearAll();
            telemetry.addLine("PRINT: " + finalOutput);
            System.out.println("PRINT: " + finalOutput);
            telemetry.update();
            return true;
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
