package com.uniovi;

import com.uniovi.util.FirefoxWebDriver;
import com.uniovi.util.PropertiesExtractor;
import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.jupiter.api.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.events.WebDriverListener;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Tag("integration")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
@CucumberContextConfiguration
public class Wichat_IntegrationTests {
    protected static final String URL = "http://localhost:3000/";

    protected PropertiesExtractor p = new PropertiesExtractor("messages");

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