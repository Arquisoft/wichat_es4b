package com.uniovi;

import com.uniovi.util.FirefoxWebDriver;
import com.uniovi.util.PropertiesExtractor;
import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.jupiter.api.*;
import org.openqa.selenium.WebDriver;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Tag("integration")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
@CucumberContextConfiguration
public class Wiq_IntegrationTests {
    protected static final String URL = "http://localhost:3000/";

    protected PropertiesExtractor p = new PropertiesExtractor("messages");

    protected static WebDriver driver;

    public Wiq_IntegrationTests() {
        driver = webDriver();
    }

    public WebDriver webDriver() {
        if (driver != null) {
            return driver;
        }

        driver = new FirefoxWebDriver();
        return driver;
    }
}
