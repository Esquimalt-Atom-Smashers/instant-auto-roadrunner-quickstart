package org.firstinspires.ftc.teamcode.OpModes;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.MecanumDrive;
import org.firstinspires.ftc.teamcode.tuning.TuningOpModes;

@Autonomous

public class First_Auto extends OpMode {




    @Override
    public void init() {

    }

    @Override
    public void loop() {
    }

    @Override
    public void start() {
        Pose2d beginPose = new Pose2d(0, 0, 0);
        MecanumDrive drive = new MecanumDrive(hardwareMap, beginPose);

        Actions.runBlocking(
                drive.actionBuilder(beginPose)
                        .strafeToLinearHeading( new Vector2d(24,24), Math.toRadians(90) )
                        .build());
    }



}
