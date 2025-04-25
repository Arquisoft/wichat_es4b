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

                "src/test/resources/features/playing_normal_game.feature"
                // NO incluyas la del juego aquí
        }
)
@SpringBootTest
public class CucumberRunnerTests {
}

