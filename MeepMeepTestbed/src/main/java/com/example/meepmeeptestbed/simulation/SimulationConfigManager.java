package com.example.meepmeeptestbed.simulation;

import static com.example.instantauto.actions.UserActionRegistry.registerCondition;
import static com.example.instantauto.configs.MetaFieldRegistry.registerField;
import static com.example.instantauto.configs.MetaFieldRegistry.registerType;

public class SimulationConfigManager {
    public static void init() {
        // Register type definitions
        registerType(new SimPose2d(0, 0, 0));

        // Register specific fields with default values
        registerField("autoTimer", Double.class, 0.0);
        registerField("redGoalPose", SimPose2d.class, new SimPose2d(-72, 48, 0));
        registerField("blueGoalPose", SimPose2d.class, new SimPose2d(72, 48, 0));
        registerField("motorName", String.class, "motorName");
        registerField("gamepadLeftY", Double.class, 0.0);

        // New required fields for Auto
        registerField("Title", String.class, "");
        registerField("Starting", SimPose2d.class, new SimPose2d(0,0,0));

        // Register custom conditions
        registerCondition("is_active", () -> true);
    }
}
