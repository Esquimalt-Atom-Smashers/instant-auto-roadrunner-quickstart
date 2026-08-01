package org.firstinspires.ftc.teamcode.action;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.example.instantauto.actions.Action;
import com.example.instantauto.actions.UserActionRegistry;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.roadrunner.MecanumDrive;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ActionUtils {
    /**
     * Marker interface for actions that can be fused into a single Roadrunner trajectory.
     */
    public interface BuilderAction extends Action {
        com.acmerobotics.roadrunner.TrajectoryActionBuilder apply(com.acmerobotics.roadrunner.TrajectoryActionBuilder builder);
    }

    /**
     * Adapts an InstantAuto Action to a RoadRunner Action.
     * Useful for running via Actions.runBlocking(adapt(myAction)).
     */
    public static com.acmerobotics.roadrunner.Action adapt(final com.example.instantauto.actions.Action action, final Telemetry telemetry) {
        if (action instanceof WrappedRRAction) {
            final com.acmerobotics.roadrunner.Action rrAction = ((WrappedRRAction) action).getRRAction();
            return new com.acmerobotics.roadrunner.Action() {
                @Override
                public boolean run(TelemetryPacket packet) {
                    boolean result = rrAction.run(packet);
                    telemetry.update();
                    return result;
                }
            };
        }
        return new com.acmerobotics.roadrunner.Action() {
            @Override
            public boolean run(TelemetryPacket packet) {
                boolean result = action.run();
                telemetry.update();
                return result;
            }
        };
    }

    /**
     * Wraps a RoadRunner Action into an InstantAuto Action.
     * Useful for registering RoadRunner actions in UserActionRegistry.
     */
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

    /**
     * Parses a CSV string into an array of doubles.
     * Supports variable resolution.
     * @param params The parameter object (usually a String).
     * @param count The expected number of doubles.
     * @return A double array or null if parsing fails.
     */
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
                    // Try to resolve as a variable
                    com.example.instantauto.configs.MetaFieldRegistry.ConfigEntry<?> entry =
                            com.example.instantauto.configs.MetaFieldRegistry.getEntry(part);
                    if (entry != null && entry.value instanceof Number) {
                        result[i] = ((Number) entry.value).doubleValue();
                    } else {
                        return null; // Failed to resolve
                    }
                }
            }
            return result;
        }
        return null;
    }

    /**
     * Recursively parses a CSV string of actions into a List of Actions.
     * @param params The parameter object (usually a String).
     * @return A list of actions or null if input is not a string.
     */
    public static List<Action> asActions(Object params, MecanumDrive drive) {
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
            return merge(actions, drive);
        }
        return null;
    }

    /**
     * Merges consecutive BuilderAction actions into a single Roadrunner trajectory.
     */
    private static List<Action> merge(List<Action> actions, MecanumDrive drive) {
        if (actions.size() <= 1) return actions;
        
        List<Action> merged = new ArrayList<>();
        List<BuilderAction> group = new ArrayList<>();
        
        for (Action a : actions) {
            if (a instanceof BuilderAction) {
                group.add((BuilderAction) a);
            } else {
                if (!group.isEmpty()) {
                    merged.add(fuse(group, drive));
                    group = new ArrayList<>();
                }
                merged.add(a);
            }
        }
        
        if (!group.isEmpty()) {
            merged.add(fuse(group, drive));
        }
        
        return merged;
    }

    private static Action fuse(List<BuilderAction> group, MecanumDrive drive) {
        if (group.size() == 0) return null;
        com.acmerobotics.roadrunner.TrajectoryActionBuilder builder = drive.actionBuilder(drive.localizer.getPose());
        for (BuilderAction ba : group) {
            builder = ba.apply(builder);
        }
        return wrap(builder.build());
    }

    /**
     * Converts an object to a string with consistent formatting for primitives.
     * @param obj The object to convert.
     * @return A formatted string.
     */
    public static String asString(Object obj) {
        if (obj == null) return "";
        if (obj instanceof Double) return String.format(Locale.US, "%.2f", (Double) obj);
        if (obj instanceof Integer) return String.format(Locale.US, "%d", (Integer) obj);
        if (obj instanceof Boolean) return String.format(Locale.US, "%b", (Boolean) obj);
        return obj.toString();
    }
}
