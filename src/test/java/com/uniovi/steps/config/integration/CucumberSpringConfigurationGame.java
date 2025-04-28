package com.uniovi.steps.config.integration;

import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@Tag("integration")
@CucumberContextConfiguration
@SpringBootTest
@ActiveProfiles("integration")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)// <-- Aquí sí usamos 'integration', CARGA sample data
public class CucumberSpringConfigurationGame {
}


