package org.firstinspires.ftc.teamcode.opmodes;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.roadrunner.MecanumDrive;
@Autonomous

public class ControlGroupAuto extends OpMode {
    private Pose2d beginPose;
    private MecanumDrive drive;

    @Override
    public void init() {
        beginPose = new Pose2d(new Vector2d(-24,0), 0);
        drive = new MecanumDrive(hardwareMap, beginPose);
    }

    @Override
    public void loop() {

    }

    @Override
    public void start(){
        Actions.runBlocking(
                drive.actionBuilder(new Pose2d(-24, 0, 0))
                        .setTangent(Math.toRadians(90))
                        .splineToSplineHeading(new Pose2d(0,0,0), Math.toRadians(270))
                        .setTangent(Math.toRadians(270))
                        .splineToSplineHeading(new Pose2d(40,0,0), Math.toRadians(90))
                        .build());
    }
}
