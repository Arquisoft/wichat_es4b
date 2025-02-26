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

    @BeforeAll
    public static void setUp() {
        if (driver == null) {
            driver = FirefoxWebDriver.getDriver();
        }
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }
}
