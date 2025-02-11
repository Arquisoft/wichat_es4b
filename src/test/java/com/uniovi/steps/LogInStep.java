package com.uniovi.steps;

import com.uniovi.Wiq_IntegrationTests;
import com.uniovi.util.SeleniumUtils;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

public class LogInStep extends Wiq_IntegrationTests {
    @And("I press the login button")
    public void iPressTheLoginButton() {
        By button = By.className("btn");
        driver.findElement(button).click();
    }

    @And("I fill in the form with valid data email: {string} password: {string}")
    public void iFillInTheFormWithValidDataEmailPassword(String username, String pass) {
        WebElement mail = driver.findElement(By.name("username"));
        mail.click();
        mail.clear();
        mail.sendKeys(username);
        WebElement password = driver.findElement(By.name("password"));
        password.click();
        password.clear();
        password.sendKeys(pass);
    }

    @And("I access the personal ranking page")
    public void iAccessThePersonalRankingPage() {
        List<WebElement> elems = SeleniumUtils.waitLoadElementsBy(driver, "free", "//*[@id=\"navbarDropdown2\"]", 5);
        elems.get(0).click();
        elems = SeleniumUtils.waitLoadElementsBy(driver, "free", "//*[@href=\"/ranking/playerRanking\"]", 5);
        elems.get(0).click();
    }

    @Then("I should see the personal ranking page")
    public void iShouldSeeThePersonalRankingPage() {
        List<WebElement> elems = SeleniumUtils.waitLoadElementsBy(driver, "@id", "playerRankingTable", 5);
        Assertions.assertFalse(elems.isEmpty());
    }
}
