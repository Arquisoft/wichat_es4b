package com.uniovi;

import com.uniovi.util.FirefoxWebDriver;
import com.uniovi.util.PropertiesExtractor;
import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.events.WebDriverListener;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Tag("integration")

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//Hay que ver como hacer que solo se carga la data-sample con el test del juego unicamente
@CucumberContextConfiguration
public class Wichat_IntegrationTests {
    public static final String URL = "http://localhost:3000/";

    protected final PropertiesExtractor p = new PropertiesExtractor("messages");

    protected static WebDriver driver;

    public Wichat_IntegrationTests() {
        driver = webDriver(new CustomWebDriverListener());
    }

    public WebDriver webDriver(WebDriverListener listener) {
        if (driver != null) {
            return driver;
        }

        driver = FirefoxWebDriver.getWebDriver(listener);
        return driver;
    }

    private static class CustomWebDriverListener implements WebDriverListener {
        // Implement listener methods as needed
    }
}