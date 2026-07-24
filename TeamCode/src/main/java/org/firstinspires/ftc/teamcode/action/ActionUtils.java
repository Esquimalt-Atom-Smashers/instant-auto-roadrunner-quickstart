package org.firstinspires.ftc.teamcode.action;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.example.instantauto.actions.Action;
import com.example.instantauto.actions.UserActionRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ActionUtils {
    /**
     * Adapts an InstantAuto Action to a RoadRunner Action.
     * Useful for running via Actions.runBlocking(adapt(myAction)).
     */
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
            try {
                for (int i = 0; i < count; i++) {
                    result[i] = Double.parseDouble(parts[i].trim());
                }
                return result;
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Recursively parses a CSV string of actions into a List of Actions.
     * @param params The parameter object (usually a String).
     * @return A list of actions or null if input is not a string.
     */
    public static List<Action> asActions(Object params) {
        if (params instanceof String) {
            List<String> subActionStrings = UserActionRegistry.splitByTopLevelCommas((String) params);
            List<Action> actions = new ArrayList<>();
            for (String sub : subActionStrings) {
                Action a = UserActionRegistry.createAction(sub);
                if (a != null) actions.add(a);
            }
            return actions;
        }
        return null;
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
