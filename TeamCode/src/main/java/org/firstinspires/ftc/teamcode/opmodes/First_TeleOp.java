package org.firstinspires.ftc.teamcode.opmodes;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.roadrunner.MecanumDrive;
import org.firstinspires.ftc.teamcode.roadrunner.MecanumDrive;

@TeleOp
public class First_TeleOp extends OpMode {
    private Pose2d beginPose;
    private MecanumDrive drive;
    private boolean init = false;
    private boolean looping = false;

    @Override
    public void init() {
        init = true;
         beginPose = new Pose2d(0, 0, 0);
         drive = new MecanumDrive(hardwareMap, beginPose);
         telemetry.addData("Initiated", init);
    }

    @Override
    public void loop() {
        looping = true;
        telemetry.addData("Looping", looping);
        telemetry.addData("GamePad 1 Cross", gamepad1.cross);
        if(gamepad1.cross){
            Actions.runBlocking(
            drive.actionBuilder(beginPose)
                    .strafeToLinearHeading( new Vector2d(24,24), Math.toRadians(90) )
                    .build());
        }
        if(gamepad1.square){
            Actions.runBlocking(
                    drive.actionBuilder(beginPose)
                            .strafeToLinearHeading( new Vector2d(0,0), Math.toRadians(90) )
                            .build());
        }


    }
}
