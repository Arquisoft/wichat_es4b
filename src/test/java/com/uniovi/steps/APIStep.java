package com.uniovi.steps;

import com.uniovi.Wiq_IntegrationTests;
import com.uniovi.util.SeleniumUtils;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.WebElement;

import java.util.List;

public class APIStep extends Wiq_IntegrationTests {

    @And("I go to the API key page")
    public void iGoToTheAPIKeyPage() {
        List<WebElement> elems = SeleniumUtils.waitLoadElementsBy(driver, "free", "//*[@id=\"btnUser\"]", 5);
        elems.get(0).click();
        elems = SeleniumUtils.waitLoadElementsBy(driver, "@href", "/home/apikey", 5);
        elems.get(0).click();
    }

    @Then("I should see the API key button")
    public void iShouldSeeTheAPIKeyButton() {
        List<WebElement> elems = SeleniumUtils.waitLoadElementsBy(driver, "@href", "/home/apikey/create", 5);
        Assertions.assertTrue(elems.size() == 1);
    }

    @When("I press the API key button")
    public void iPressTheAPIKeyButton() {
        List<WebElement> elems = SeleniumUtils.waitLoadElementsBy(driver, "@href", "/home/apikey/create", 5);
        elems.get(0).click();
    }

    @Then("I should see the API key")
    public void iShouldSeeTheAPIKey() {
        List<WebElement> elems = SeleniumUtils.waitLoadElementsBy(driver, "free", "//*[@id=\"apiKeyDiv\"]/form/div/div", 5);
        Assertions.assertTrue(elems.size() == 1);
    }

    @And("I reenter the API key page")
    public void iReenterTheAPIKeyPage() {
        this.iGoToTheAPIKeyPage();
    }

    @Then("I should see the API key directly")
    public void iShouldSeeTheAPIKeyDirectly() {
        iShouldSeeTheAPIKey();
    }
}
