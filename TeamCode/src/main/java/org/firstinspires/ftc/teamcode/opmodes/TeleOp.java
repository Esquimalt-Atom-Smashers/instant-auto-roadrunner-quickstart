package org.firstinspires.ftc.teamcode.opmodes;

import com.example.instantauto.actions.AutoParser;
import com.example.instantauto.actions.UserActionRegistry;
import com.example.instantauto.configs.ConfigParser;
import com.example.instantauto.configs.MetaFieldRegistry;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.TextFileLocationBook;
import org.firstinspires.ftc.teamcode.configs.ConfigManager;

import java.util.List;

@com.qualcomm.robotcore.eventloop.opmode.TeleOp
public class TeleOp extends OpMode {
    private AutoParser autoParser = new AutoParser(TextFileLocationBook.robotSettingFilePath, TextFileLocationBook.userActionSettingFilePath);
    private ConfigParser configParser = new ConfigParser();

    @Override
    public void init() {
        telemetry.addLine("Reading from " + TextFileLocationBook.GENERAL_ROBOT_SETTING_FILE_NAME);
        ConfigManager.init(this);
        autoParser.parseTeleOpConfig();
        telemetry.addLine("--- Config Parser Logs ---");
        List<String> logs = autoParser.getConfigLogs();
        if (logs.isEmpty()) {
            telemetry.addLine("No errors found.");
        } else {
            for (String log : logs) {
                telemetry.addLine("[ERROR] " + log);
            }
        }
        telemetry.update();
    }

    @Override
    public void loop() {
        dumpAllFields();
        telemetry.update();
    }
    @Override
    public void stop() {
         MetaFieldRegistry.clear(); // Removed variable persistence
        UserActionRegistry.clear();
    }

    private void printField(String name) {
        MetaFieldRegistry.ConfigEntry<?> entry = MetaFieldRegistry.getEntry(name);
        if (entry != null) {
            telemetry.addLine(entry.fieldName + ": " + entry.getValue());
        } else {
            telemetry.addLine(name + ": [Not Registered]");
        }
    }

    private void dumpAllFields() {
        List<String> registeredIdentifiers = MetaFieldRegistry.getAllRegisteredFieldNames();
        for (String identifier : registeredIdentifiers) {
            printField(identifier);
        }
    }
}
