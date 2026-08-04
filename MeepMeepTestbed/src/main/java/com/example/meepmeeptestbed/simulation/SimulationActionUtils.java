package com.example.meepmeeptestbed.simulation;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.example.instantauto.actions.Action;
import com.example.instantauto.actions.UserActionRegistry;
import com.noahbres.meepmeep.roadrunner.entity.RoadRunnerBotEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SimulationActionUtils {
    public interface BuilderAction extends Action {
        com.acmerobotics.roadrunner.TrajectoryActionBuilder apply(com.acmerobotics.roadrunner.TrajectoryActionBuilder builder);
    }

    public static com.acmerobotics.roadrunner.Action adapt(final com.example.instantauto.actions.Action action) {
        if (action instanceof WrappedRRAction) {
            return ((WrappedRRAction) action).getRRAction();
        }
        return new com.acmerobotics.roadrunner.Action() {
            @Override
            public boolean run(TelemetryPacket packet) {
                return action.run();
            }
        };
    }

    public static com.example.instantauto.actions.Action wrap(com.acmerobotics.roadrunner.Action rrAction) {
        return new WrappedRRAction(rrAction);
    }

    private static class WrappedRRAction implements com.example.instantauto.actions.Action {
        private final com.acmerobotics.roadrunner.Action rrAction;
        private final TelemetryPacket packet = new TelemetryPacket();

        WrappedRRAction(com.acmerobotics.roadrunner.Action rrAction) {
            this.rrAction = rrAction;
        }

        @Override
        public boolean run() {
            return rrAction.run(packet);
        }

        public com.acmerobotics.roadrunner.Action getRRAction() {
            return rrAction;
        }
    }

    public static double[] asDoubles(Object params, int count) {
        if (params instanceof String) {
            String s = (String) params;
            if (s.isEmpty()) return null;
            String[] parts = s.split(",");
            if (parts.length != count) return null;
            double[] result = new double[count];
            for (int i = 0; i < count; i++) {
                String part = parts[i].trim();
                try {
                    result[i] = Double.parseDouble(part);
                } catch (NumberFormatException e) {
                    com.example.instantauto.configs.MetaFieldRegistry.ConfigEntry<?> entry =
                            com.example.instantauto.configs.MetaFieldRegistry.getEntry(part);
                    if (entry != null && entry.value instanceof Number) {
                        result[i] = ((Number) entry.value).doubleValue();
                    } else {
                        return null;
                    }
                }
            }
            return result;
        }
        return null;
    }

    public static List<Action> asActions(Object params, RoadRunnerBotEntity bot) {
        if (params instanceof String) {
            List<String> subActionStrings = UserActionRegistry.splitByTopLevelCommas((String) params);
            List<Action> actions = new ArrayList<>();
            for (String sub : subActionStrings) {
                Action a = UserActionRegistry.createAction(sub);
                if (a != null) {
                    actions.add(a);
                } else if (!sub.trim().isEmpty()) {
                    UserActionRegistry.addError("Malformed action: " + sub);
                }
            }
            return merge(actions, bot);
        }
        return null;
    }

    private static List<Action> merge(List<Action> actions, RoadRunnerBotEntity bot) {
        if (actions.size() <= 1) return actions;
        
        List<Action> merged = new ArrayList<>();
        List<BuilderAction> group = new ArrayList<>();
        
        for (Action a : actions) {
            if (a instanceof BuilderAction) {
                group.add((BuilderAction) a);
            } else {
                if (!group.isEmpty()) {
                    merged.add(fuse(group, bot));
                    group = new ArrayList<>();
                }
                merged.add(a);
            }
        }
        
        if (!group.isEmpty()) {
            merged.add(fuse(group, bot));
        }
        
        return merged;
    }

    private static Action fuse(List<BuilderAction> group, RoadRunnerBotEntity bot) {
        if (group.isEmpty()) return null;
        com.acmerobotics.roadrunner.TrajectoryActionBuilder builder = bot.getDrive().actionBuilder(bot.getPose());
        for (BuilderAction ba : group) {
            builder = ba.apply(builder);
        }
        return wrap(builder.build());
    }

    public static String asString(Object obj) {
        if (obj == null) return "";
        if (obj instanceof Double) return String.format(Locale.US, "%.2f", (Double) obj);
        if (obj instanceof Integer) return String.format(Locale.US, "%d", (Integer) obj);
        if (obj instanceof Boolean) return String.format(Locale.US, "%b", (Boolean) obj);
        return obj.toString();
    }
}
