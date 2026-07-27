package org.firstinspires.ftc.teamcode.opmodes;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.roadrunner.MecanumDrive;



@Autonomous

public class First_Auto extends OpMode {

    private Pose2d beginPose;
    private MecanumDrive drive;


    @Override
    public void init() {
        beginPose = new Pose2d(new Vector2d(0,0), 0);
        drive = new MecanumDrive(hardwareMap, beginPose);
    }

    @Override
    public void loop() {

    }

    @Override
    public void start(){
        Actions.runBlocking(
                drive.actionBuilder(beginPose)
                        .strafeToSplineHeading( new Vector2d(24,24), Math.toRadians(270) )
                        .strafeToSplineHeading(new Vector2d(36, 36), Math.toRadians(90))
                        .build());
    }





}