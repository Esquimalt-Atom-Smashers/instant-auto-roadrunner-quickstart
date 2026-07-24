package org.firstinspires.ftc.teamcode.opmodes;

import com.example.instantauto.actions.Action;
import com.example.instantauto.actions.AutoParser;
import com.example.instantauto.actions.UserActionRegistry;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpModeManager;
import com.qualcomm.robotcore.eventloop.opmode.OpModeRegistrar;

import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta;
import org.firstinspires.ftc.teamcode.TextFileLocationBook;
import org.firstinspires.ftc.teamcode.action.ActionManager;
import org.firstinspires.ftc.teamcode.configs.ConfigManager;

import java.io.File;
import java.util.List;

public class TextFileAutos {
    public static final String GROUP = "InstantAuto";

    @OpModeRegistrar
    public static void register(OpModeManager manager) {
        AutoParser autoParser = new AutoParser(TextFileLocationBook.robotSettingFilePath, TextFileLocationBook.userActionSettingFilePath);

        // 1. Scan for [ACTIVE] files
        List<File> activeAutos = autoParser.findActiveAutos(TextFileLocationBook.robotSettingFilePath);

        for (File auto: activeAutos) {
            manager.register(metaForClass(auto.getName()), new AutonomousBase(autoParser, auto));
        }
    }

    private static OpModeMeta metaForClass(String autoFileName) {
        return new OpModeMeta.Builder()
                .setName(autoFileName.replace("[ACTIVE]", ""))
                .setGroup(GROUP)
                .setFlavor(OpModeMeta.Flavor.AUTONOMOUS)
                .build();
    }


}
