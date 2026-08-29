package org.firstinspires.ftc.teamcode.configs;

import static com.example.instantauto.actions.UserActionRegistry.registerCondition;
import static com.example.instantauto.configs.MetaFieldRegistry.registerField;
import static com.example.instantauto.configs.MetaFieldRegistry.registerSupplier;
import static com.example.instantauto.configs.MetaFieldRegistry.registerType;

import com.example.instantauto.actions.UserActionRegistry;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DistanceSensor;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class ConfigManager {
    public static void init(OpMode opMode) {
        // Register type definitions
        registerType(new Pose2d(0, 0, 0));

        // Register specific fields with default values
        registerField("autoTimer", Double.class, 0.0);
        registerField("redGoalPose", Pose2d.class, new Pose2d(-72, 48, 0));
        registerField("blueGoalPose", Pose2d.class, new Pose2d(72, 48, 0));
        registerField("motorName", String.class, "motorName");

        // Register sensor-related fields with suppliers
        registerField("gamepadLeftY", Double.class, (Supplier<Double>) () -> (double) opMode.gamepad1.left_stick_y);
        registerField("sysTime", Long.class, (Supplier<Long>) System::nanoTime);
        registerSupplier("batteryVoltage", Double.class,
                () -> opMode.hardwareMap.voltageSensor.iterator().next().getVoltage());

        // New required fields for Auto
        registerField("Title", String.class, "");
        registerField("Starting", Pose2d.class, new Pose2d(0,0,0));

        // Register custom conditions
        registerCondition("is_active", () -> true);
    }
}