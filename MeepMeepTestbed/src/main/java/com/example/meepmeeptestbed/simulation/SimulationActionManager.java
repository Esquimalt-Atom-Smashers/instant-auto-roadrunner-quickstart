package com.example.meepmeeptestbed.simulation;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Vector2d;
import com.example.instantauto.actions.Action;
import com.example.instantauto.actions.MiniAction;
import com.example.instantauto.actions.UserActionRegistry;
import com.noahbres.meepmeep.roadrunner.entity.RoadRunnerBotEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SimulationActionManager {
    RoadRunnerBotEntity bot;

    public void init(RoadRunnerBotEntity bot) {
        this.bot = bot;

        UserActionRegistry.register(new MiniAction("STRAFE.TO", this::strafeToFactory));
        UserActionRegistry.register(new MiniAction("SPLINE.TO", this::splineToFactory));

        UserActionRegistry.register(new MiniAction("PRINT", obj ->
                SimulationActionUtils.wrap(new PrintAction(SimulationActionUtils.asString(obj)))));

        UserActionRegistry.register(new MiniAction("PARALLEL", params -> {
            List<Action> actions = SimulationActionUtils.asActions(params, bot);
            if (actions == null) return null;

            List<com.acmerobotics.roadrunner.Action> rrActions = new ArrayList<>();
            for (Action a : actions) {
                rrActions.add(SimulationActionUtils.adapt(a));
            }
            return SimulationActionUtils.wrap(new com.acmerobotics.roadrunner.ParallelAction(rrActions));
        }));

        UserActionRegistry.register(new MiniAction("WAIT", params -> {
            double[] d = SimulationActionUtils.asDoubles(params, 1);
            return d != null ? SimulationActionUtils.wrap(new com.acmerobotics.roadrunner.SleepAction(d[0])) : null;
        }));
    }

    private Action strafeToFactory(Object params) {
        if (params instanceof SimPose2d) {
            final SimPose2d p = (SimPose2d) params;
            return new SimulationActionUtils.BuilderAction() {
                @Override
                public com.acmerobotics.roadrunner.TrajectoryActionBuilder apply(com.acmerobotics.roadrunner.TrajectoryActionBuilder builder) {
                    return builder.strafeToSplineHeading(new Vector2d(p.x, p.y), Math.toRadians(p.heading));
                }
                @Override
                public boolean run() {
                    return apply(bot.getDrive().actionBuilder(bot.getPose())).build().run(new TelemetryPacket());
                }
            };
        }

        final double[] d = SimulationActionUtils.asDoubles(params, 3);
        if (d != null) {
            return new SimulationActionUtils.BuilderAction() {
                @Override
                public com.acmerobotics.roadrunner.TrajectoryActionBuilder apply(com.acmerobotics.roadrunner.TrajectoryActionBuilder builder) {
                    return builder.strafeToSplineHeading(new Vector2d(d[0], d[1]), Math.toRadians(d[2]));
                }
                @Override
                public boolean run() {
                    return apply(bot.getDrive().actionBuilder(bot.getPose())).build().run(new TelemetryPacket());
                }
            };
        }
        return null;
    }

    private Action splineToFactory(Object params) {
        if (params instanceof String) {
            String s = (String) params;
            String[] parts = s.split(",");

            if (parts.length == 5) {
                final double[] d = SimulationActionUtils.asDoubles(s, 5);
                if (d != null) {
                    return new SimulationActionUtils.BuilderAction() {
                        @Override
                        public com.acmerobotics.roadrunner.TrajectoryActionBuilder apply(com.acmerobotics.roadrunner.TrajectoryActionBuilder builder) {
                            return builder.setTangent(Math.toRadians(d[3]))
                                    .splineToSplineHeading(new com.acmerobotics.roadrunner.Pose2d(d[0], d[1], Math.toRadians(d[2])), Math.toRadians(d[4]));
                        }
                        @Override
                        public boolean run() {
                            return apply(bot.getDrive().actionBuilder(bot.getPose())).build().run(new TelemetryPacket());
                        }
                    };
                }
            }

            if (parts.length == 3) {
                String poseName = parts[0].trim();
                com.example.instantauto.configs.MetaFieldRegistry.ConfigEntry<?> entry =
                        com.example.instantauto.configs.MetaFieldRegistry.getEntry(poseName);
                if (entry != null && entry.value instanceof SimPose2d) {
                    final SimPose2d p = (SimPose2d) entry.value;
                    try {
                        final double startTan = Double.parseDouble(parts[1].trim());
                        final double endTan = Double.parseDouble(parts[2].trim());
                        return new SimulationActionUtils.BuilderAction() {
                            @Override
                            public com.acmerobotics.roadrunner.TrajectoryActionBuilder apply(com.acmerobotics.roadrunner.TrajectoryActionBuilder builder) {
                                return builder.setTangent(Math.toRadians(startTan))
                                        .splineToSplineHeading(new com.acmerobotics.roadrunner.Pose2d(p.x, p.y, Math.toRadians(p.heading)), Math.toRadians(endTan));
                            }
                            @Override
                            public boolean run() {
                                return apply(bot.getDrive().actionBuilder(bot.getPose())).build().run(new TelemetryPacket());
                            }
                        };
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return null;
    }

    public static class PrintAction implements com.acmerobotics.roadrunner.Action {
        String message;
        boolean isVariable = false;

        public PrintAction(String message) {
            this.message = message;
            if (!message.contains(" ") && !message.startsWith("\"")) {
                isVariable = true;
            } else if (message.startsWith("\"") && message.endsWith("\"")) {
                this.message = message.substring(1, message.length() - 1);
            }
        }

        @Override
        public boolean run(TelemetryPacket telemetryPacket) {
            String finalOutput = message;
            if (isVariable) {
                com.example.instantauto.configs.MetaFieldRegistry.ConfigEntry<?> entry =
                        com.example.instantauto.configs.MetaFieldRegistry.getEntry(message);
                if (entry != null) {
                    finalOutput = SimulationActionUtils.asString(entry.value);
                }
            }
            System.out.println("SIM PRINT: " + finalOutput);
            return false;
        }
    }
}
