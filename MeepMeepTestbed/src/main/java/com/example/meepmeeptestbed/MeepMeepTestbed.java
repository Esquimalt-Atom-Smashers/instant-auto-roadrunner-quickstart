package com.example.meepmeeptestbed;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.SequentialAction;
import com.example.instantauto.actions.AutoParser;
import com.example.instantauto.actions.UserActionRegistry;
import com.example.instantauto.configs.MetaFieldRegistry;
import com.example.meepmeeptestbed.simulation.SimPose2d;
import com.example.meepmeeptestbed.simulation.SimulationActionManager;
import com.example.meepmeeptestbed.simulation.SimulationActionUtils;
import com.example.meepmeeptestbed.simulation.SimulationConfigManager;
import com.noahbres.meepmeep.MeepMeep;
import com.noahbres.meepmeep.roadrunner.DefaultBotBuilder;
import com.noahbres.meepmeep.roadrunner.entity.RoadRunnerBotEntity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MeepMeepTestbed {
    public static void main(String[] args) {
        // Paths for the text files (Assuming running from project root)
        String configPath = "MeepMeepTestbed/src/main/java/com/example/meepmeeptestbed/textfiles/GeneralRobotSettings.txt";
        String userActionPath = "MeepMeepTestbed/src/main/java/com/example/meepmeeptestbed/textfiles/UserActionSettings.txt";
        File autoFile = new File("MeepMeepTestbed/src/main/java/com/example/meepmeeptestbed/textfiles/testAuto.txt");

        // 1. Initialize Configs and Parsers
        SimulationConfigManager.init();
        AutoParser autoParser = new AutoParser(configPath, userActionPath);

        // 2. Phase 1: Parse config to get starting pose
        autoParser.parseAutoConfig(autoFile);

        Pose2d startingPose;
        try {
            SimPose2d wrappedPose = (SimPose2d) MetaFieldRegistry.getEntry("Starting").value;
            startingPose = wrappedPose.getRRPose2d();
            System.out.println("Starting Pose: " + startingPose.toString());
        } catch (Exception e) {
            System.err.println("Warning: 'Starting' pose missing or invalid. Defaulting to (0,0,0)");
            startingPose = new Pose2d(0, 0, 0);
        }

        MetaFieldRegistry.ConfigEntry<?> titleEntry = MetaFieldRegistry.getEntry("Title");
        if (titleEntry != null && titleEntry.value != null && !titleEntry.value.toString().trim().isEmpty()) {
            System.out.println("Auto Title: " + titleEntry.value);
        }

        List<String> loadErrors = UserActionRegistry.getLoadErrors();
        if (!loadErrors.isEmpty()) {
            System.out.println("\n[USER ACTION ERRORS/WARNINGS]:");
            for (String log : loadErrors) System.out.println("  " + log);
        } else {
            System.out.println("\n[NO USER ACTION ERRORS/WARNINGS]");
        }

        List<String> configLogs = autoParser.getConfigLogs();
        if (!configLogs.isEmpty()) {
            System.out.println("\n[CONFIG ERRORS/WARNINGS]:");
            for (String log : configLogs) System.out.println("  " + log);
        } else {
            System.out.println("\n[NO CONFIG ERRORS/WARNINGS]");
        }

        List<String> actionErrors = autoParser.getActionErrors();
        if (!actionErrors.isEmpty()) {
            System.out.println("\n[ACTION ERRORS]:");
            for (String err : actionErrors) System.out.println("  " + err);
        } else {
            System.out.println("\n[NO ACTION ERRORS]");
        }

        // 3. Initialize MeepMeep
        MeepMeep meepMeep = new MeepMeep(800);

        RoadRunnerBotEntity myBot = new DefaultBotBuilder(meepMeep)
                .setConstraints(60, 60, Math.toRadians(180), Math.toRadians(180), 15)
                .setStartPose(startingPose)
                .build();

        // 4. Initialize Actions
        SimulationActionManager actionManager = new SimulationActionManager();
        actionManager.init(myBot);

        // 5. Phase 2: Parse actions
        autoParser.parseActions();

        // 6. Convert InstantAuto actions to RoadRunner actions
        List<com.example.instantauto.actions.Action> iaActions = SimulationActionUtils.asActions(autoParser.getActionContent(), myBot);
        
        List<com.acmerobotics.roadrunner.Action> rrActions = new ArrayList<>();
        if (iaActions != null) {
            for (com.example.instantauto.actions.Action ia : iaActions) {
                rrActions.add(SimulationActionUtils.adapt(ia));
                System.out.println("Action: " + ia.getClass().getSimpleName());
            }
        }

        // 7. Run the sequence
        myBot.runAction(new SequentialAction(rrActions));


        meepMeep.setBackground(MeepMeep.Background.FIELD_DECODE_OFFICIAL)
                .setDarkMode(true)
                .setBackgroundAlpha(0.95f)
                .addEntity(myBot)
                .start();
    }
}
