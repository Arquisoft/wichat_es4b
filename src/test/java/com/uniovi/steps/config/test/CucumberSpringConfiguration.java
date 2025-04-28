package com.uniovi.steps.config.test;

import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@Tag("integration")
@CucumberContextConfiguration
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)// <-- Usamos el perfil 'test', NO carga sample data
public class CucumberSpringConfiguration {
}


