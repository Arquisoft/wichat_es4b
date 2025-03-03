package com.uniovi.steps;

import com.uniovi.util.PropertiesExtractor;
import com.uniovi.*;
import com.uniovi.services.RoleService;
import com.uniovi.util.SeleniumUtils;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class NavigateHomeStep extends Wiq_IntegrationTests {

    @Autowired
    private RoleService roleService;

    @Given("I am in the home page")
    public void i_am_in_the_home_page() {
        driver.navigate().to(URL);
    }

    @Given("I am not logged in")
    public void iAmNotLoggedIn() {
        driver.manage().deleteAllCookies();
        driver.navigate().to(URL);
    }

    @When("I click the login button")
    public void iClickTheLoginButton() {
        List<WebElement> elems = SeleniumUtils.waitLoadElementsBy(driver, "@href", "login", 5);
        elems.get(0).click();
    }

    @When("I click the global ranking button")
    public void iClickTheGlobalRankingButton() {
        List<WebElement> elems = SeleniumUtils.waitLoadElementsBy(driver, "free", "//*[@id=\"navbarDropdown2\"]", 5);
        elems.get(0).click();
        elems = SeleniumUtils.waitLoadElementsBy(driver, "@href", "/ranking/globalRanking", 5);
        elems.get(0).click();
    }

    @When("I click the play button")
    public void iClickThePlayButton() {
        List<WebElement> elems = SeleniumUtils.waitLoadElementsBy(driver, "@href", "/game", 5);
        elems.get(0).click();
    }

    @When("I click the register button")
    public void i_click_the_register_button() {
        List<WebElement> elems = SeleniumUtils.waitLoadElementsBy(driver, "@href", "signup", 5);
        elems.get(0).click();
    }

    @When("I try to access a non existent page")
    public void iTryToAccessANonExistentPage() {
        driver.navigate().to(URL + "nonexistent");
    }

    @And("I am on the login page")
    public void iAmOnTheLoginPage() {
        iClickTheLoginButton();
    }

    @And("I go to the home page")
    public void iGoToTheHomePage() {
        driver.navigate().to(URL);
    }

    @Then("I should see the title {string}")
    public void i_should_see_the_title(String title) {
        Assertions.assertEquals(title, driver.getTitle());
    }

    @Then("I should see the register page")
    public void i_should_see_the_register_page() {
        SeleniumUtils.waitLoadElementsBy(driver, "h2", p.getString("signup.title", PropertiesExtractor.getSPANISH()), 5);
    }

    @Then("I should see the login page")
    public void iShouldSeeTheLoginPage() {
        SeleniumUtils.waitLoadElementsBy(driver, "h2", p.getString("login.title", PropertiesExtractor.getSPANISH()), 5);
    }

    @Then("I should see the global ranking page")
    public void iShouldSeeTheGlobalRankingPage() {
        SeleniumUtils.waitLoadElementsBy(driver, "h2", p.getString("ranking.title", PropertiesExtractor.getSPANISH()), 5);
    }

    @Then("I should not see the logout button")
    public void iShouldNotSeeTheLogoutButton() {
        SeleniumUtils.waitElementNotPresent(driver, "//*[@href=\"/Logout\"]", 5);
    }

    @Then("I should not see the profile button")
    public void iShouldNotSeeTheProfileButton() {
        SeleniumUtils.waitElementNotPresent(driver, "//*[@id=\"btnUser\"]", 5);
    }

    @Then("I should not see the personal ranking button")
    public void iShouldNotSeeThePersonalRankingButton() {
        List<WebElement> elems = SeleniumUtils.waitLoadElementsBy(driver, "free", "//*[@id=\"navbarDropdown2\"]", 5);
        elems.get(0).click();
        SeleniumUtils.waitElementNotPresent(driver, "//*[@id=\"navbarDropdown2\"]/div/a[@href=\"/ranking/playerRanking\"]", 5);
    }

    @Then("I should see the {int} page")
    public void iShouldSeeThePage(int errorCode) {
        List<WebElement> elems = SeleniumUtils.waitLoadElementsBy(driver, "free", "/html/body/div[1]/div[1]/strong", 5);
        Assertions.assertEquals(elems.get(0).getText(), String.valueOf(errorCode));
    }
}
