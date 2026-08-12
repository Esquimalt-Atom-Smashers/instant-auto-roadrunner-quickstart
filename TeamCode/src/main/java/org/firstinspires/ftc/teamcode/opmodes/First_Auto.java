package org.firstinspires.ftc.teamcode.opmodes;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.ParallelAction;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.SequentialAction;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.roadrunner.MecanumDrive;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.Telemetry;



@Autonomous

public class First_Auto extends OpMode {

    private Pose2d beginPose;
    private MecanumDrive drive;
    private Servo servo1;
    private DistanceSensor distanceSensor;


    @Override
    public void init() {
        beginPose = new Pose2d(new Vector2d(0, 0), 0);
        drive = new MecanumDrive(hardwareMap, beginPose);
        servo1 = hardwareMap.get(Servo.class, "Servo1");
        servo1.setDirection(Servo.Direction.REVERSE);
        distanceSensor = hardwareMap.get(DistanceSensor.class, "distanceSensor");



    }

    @Override
    public void loop() {
        double distance = distanceSensor.getDistance(DistanceUnit.MM);
        telemetry.addData("Distance Sensor Distance", distance);
    }



    @Override
    public void start(){

        // Create servo action based on distance condition
        Action servoAction = new ServoAction(distanceSensor, servo1, 10.0);

        Actions.runBlocking(
                new ParallelAction(
                        drive.actionBuilder(beginPose)
                                .strafeToSplineHeading(new Vector2d(24, 24), Math.toRadians(270))
                                .strafeToSplineHeading(new Vector2d(36, 36), Math.toRadians(90))
                                .build(),
                        servoAction
                )
        );

        Actions.runBlocking(
                drive.actionBuilder(new Pose2d(new Vector2d(36, 36), Math.toRadians(90)))
                        .strafeToSplineHeading(new Vector2d(0, 0), 0)
                        .build());
    }

    /**
     * Custom action to control a servo based on distance sensor reading
     */
    public class ServoAction implements Action {
        private DistanceSensor distanceSensor;
        private Servo servo;
        private double threshold;

        public ServoAction(DistanceSensor distanceSensor, Servo servo, double threshold) {
            this.distanceSensor = distanceSensor;
            this.servo = servo;
            this.threshold = threshold;
        }

        @Override
        public boolean run(@NonNull TelemetryPacket telemetryPacket) {
            double distance = distanceSensor.getDistance(DistanceUnit.CM);
            if (distance > threshold) {
                servo.setPosition(1.0);  // Full open
            } else {
                servo.setPosition(0.5);  // half open
            }
            telemetry.addLine("Servo Control: Distance = " + distance + "cm");
            return true;  // Action completes immediately
        }
    }





}