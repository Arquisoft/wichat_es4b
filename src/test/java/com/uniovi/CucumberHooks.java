package com.uniovi;

import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Scenario;

public class CucumberHooks extends Wiq_IntegrationTests {
    @After
    public void cleanUpAfterScenario(Scenario scenario) {
        driver.manage().deleteAllCookies();
    }

    @AfterAll
    public static void before_or_after_all() {
        driver.quit();
    }
}
