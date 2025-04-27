package com.uniovi;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@RunWith(Cucumber.class)
@CucumberOptions(
        plugin = {"pretty"},
        features = {
                "src/test/resources/features/home_page.feature",
                "src/test/resources/features/loginForm.feature",
                "src/test/resources/features/player_data_modify.feature",
                "src/test/resources/features/signUpForm.feature",
                "src/test/resources/features/api.feature"
                // Si quieres, separa las features de juego
        },
        glue = "com.uniovi.steps.config.test" // <-- aquí pones tu paquete de Steps
)
@ActiveProfiles("test")
public class CucumberRunnerTests {
}

