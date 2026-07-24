package org.firstinspires.ftc.teamcode.opmodes;

import static org.firstinspires.ftc.teamcode.TextFileLocationBook.robotSettingFilePath;

import com.example.instantauto.configs.ConfigParser;
import com.example.instantauto.configs.MetaFieldRegistry;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.TextFileLocationBook;
import org.firstinspires.ftc.teamcode.configs.ConfigManager;

import java.util.List;

@com.qualcomm.robotcore.eventloop.opmode.TeleOp
public class TeleOp extends OpMode {
    public ConfigParser engine;
    private String configPath = robotSettingFilePath;

    public void setConfigPath(String path) {
        this.configPath = path;
    }

    @Override
    public void init() {
        engine = new ConfigParser();
        ConfigManager.init();
        engine.parseConfig(configPath);

        telemetry.addLine("Reading from " + configPath);
        telemetry.addLine("--- Config Parser Logs ---");
        List<String> logs = engine.getLogs();
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

    private void printField(String name) {
        MetaFieldRegistry.ConfigEntry<?> entry = MetaFieldRegistry.getEntry(name);
        if (entry != null) {
            telemetry.addLine(entry.fieldName + ": " + entry.value);
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
