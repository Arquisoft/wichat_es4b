package com.uniovi.steps.config.test;

import com.uniovi.Wichat_IntegrationTests;
import com.uniovi.util.PropertiesExtractor;
import com.uniovi.util.SeleniumUtils;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import org.openqa.selenium.WebElement;

import java.util.List;

public class ApiSteps extends Wichat_IntegrationTests {

    @And("I press the api key button")
    public void iPressTheApiKeyButton() {
        List<WebElement> elements = SeleniumUtils.waitLoadElementsByXpath(driver, "//*[@id=\"btnUser\"]", 10);
        elements.get(0).click();
        List<WebElement> elems = SeleniumUtils.waitLoadElementsByXpath(driver, "//*[@id=\"btnUserApiKey\"]", 10);
        elems.get(0).click();
    }

    @And("I generate my api key")
    public void iGenerateMyApiKey() {
        List<WebElement> elements = SeleniumUtils.waitLoadElementsByXpath(driver, "/html/body/div/div/a", 10);
        elements.get(0).click();
    }

    @Then("I should see the api key")
    public void iShouldSeeTheApiKey() {
        SeleniumUtils.waitLoadElementsBy(driver, "h2", p.getString("navbar.profile.apikey", PropertiesExtractor.getSPANISH()), 10);
    }

}
