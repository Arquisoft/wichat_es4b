package com.uniovi;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;

@RunWith(Cucumber.class)
@CucumberOptions(
        plugin = {"pretty"},
        features = {
                "src/test/resources/features/playing_image_game.feature",
                "src/test/resources/features/playing_normal_game.feature"
                // Si quieres, separa las features de juego
        },
        glue = "com.uniovi.steps.config.integration" // <-- aquí pones tu paquete de Steps
)
@ActiveProfiles("integration")
public class CucumberIntegrationRunnerTests {
}

