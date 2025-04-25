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
                "src/test/resources/features/signUpForm.feature"
                // NO incluyas la del juego aquí
        }
)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("test")
public class CucumberRunnerTests {
}

