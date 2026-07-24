package org.firstinspires.ftc.teamcode.opmodes;

import com.example.instantauto.configs.MetaFieldRegistry;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class TeleOpTest {
    private TeleOp teleOp;
    private Telemetry telemetry;

    @Before
    public void setUp() {
        teleOp = new TeleOp();
        telemetry = mock(Telemetry.class);
        // OpMode.telemetry is usually assigned by the system, we mock it
        teleOp.telemetry = telemetry;
        MetaFieldRegistry.clear();
        
        // Mock the Line returned by addLine to avoid NullPointerException if TeleOp chains calls
        when(telemetry.addLine(anyString())).thenReturn(mock(Telemetry.Line.class));
    }

    @Test
    public void testInitWithMissingFile() {
        String missingPath = "non_existent_file.txt";
        teleOp.setConfigPath(missingPath);

        teleOp.init();

        // Verify it attempted to read the missing file
        verify(telemetry).addLine(contains(missingPath));
        
        // Verify it logged the error
        verify(telemetry).addLine(contains("Error reading file"));
    }

    @Test
    public void testInitWithValidFile() throws IOException {
        File tempFile = File.createTempFile("robot_settings", ".txt");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("motorName = \"TestMotor\"\n");
            writer.write("maxPower = 0.8\n");
        }

        try {
            teleOp.setConfigPath(tempFile.getAbsolutePath());
            teleOp.init();

            // Run loop to trigger field display
            teleOp.loop();

            // Verify telemetry shows the parsed values
            verify(telemetry).addLine(eq("motorName" + ": " + "TestMotor"));
            verify(telemetry).addLine(eq("maxPower" + ": " + "0.8"));
        } finally {
            tempFile.delete();
        }
    }
}
