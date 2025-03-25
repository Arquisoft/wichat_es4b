package com.uniovi.steps;

import com.uniovi.Wichat_IntegrationTests;
import com.uniovi.util.PropertiesExtractor;
import com.uniovi.util.SeleniumUtils;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.WebElement;

import java.util.List;

public class HomeSteps extends Wichat_IntegrationTests {
    @Given("I am in the home page")
    public void i_am_in_the_home_page() {
        driver.navigate().to(URL);
    }

    @Given("I am not logged in")
    public void iAmNotLoggedIn() {
        driver.manage().deleteAllCookies();
        driver.navigate().to(URL);
    }

    @Then("I should see the title {string}")
    public void i_should_see_the_title(String title) {
        Assertions.assertEquals(title, driver.getTitle());
    }

    @When("I click the register button")
    public void i_click_the_register_button() {
        List<WebElement> elements = SeleniumUtils.waitLoadElementsByXpath(driver, "//*[@id=\"gameBtn\"]", 5);
        elements.get(0).click();
        List<WebElement> elems = SeleniumUtils.waitLoadElementsBy(driver, "@href", "signup", 5);
        elems.get(0).click();
    }

    @Then("I should see the register page")
    public void i_should_see_the_register_page() {
        SeleniumUtils.waitLoadElementsBy(driver, "h2", p.getString("signup.title", PropertiesExtractor.getSPANISH()), 5);
    }

    @Then("I should see the login page")
    public void iShouldSeeTheLoginPage() {
        SeleniumUtils.waitLoadElementsBy(driver, "h2", p.getString("login.title", PropertiesExtractor.getSPANISH()), 5);
    }
    @When("I click the play button")
    public void iClickThePlayButton() {
        List<WebElement> elements = SeleniumUtils.waitLoadElementsByXpath(driver, "//*[@id=\"gameBtn\"]", 5);
        elements.get(0).click();
    }

    @When("I click the global ranking button")
    public void iClickTheGlobalRankingButton() {
        List<WebElement> elems = SeleniumUtils.waitLoadElementsByXpath(driver, "//*[@id=\"navbarDropdown2\"]", 5);
        elems.get(0).click();
        elems = SeleniumUtils.waitLoadElementsByXpath(driver, "/html/body/nav/div/ul[1]/li[3]/ul/li[1]", 5);
        elems.get(0).click();
    }

    @When("I click the instructions button")
    public void iClickTheInstructionsButton() {
        List<WebElement> elems = SeleniumUtils.waitLoadElementsByXpath(driver, "/html/body/nav/div/ul[1]/li[4]/a", 5);
        elems.get(0).click();
    }
    @Then("I should see the global ranking page")
    public void iShouldSeeTheGlobalRankingPage() {
        SeleniumUtils.waitLoadElementsBy(driver, "h2", p.getString("ranking.title", PropertiesExtractor.getSPANISH()), 5);
    }
    @Then("I should see the instructions page")
    public void iShouldSeeTheInstructionsPage() {
        SeleniumUtils.waitLoadElementsBy(driver, "h2", p.getString("instructions.heading", PropertiesExtractor.getSPANISH()), 5);
    }

    @Then("I should not see the logout button")
    public void iShouldNotSeeTheLogoutButton() {
        SeleniumUtils.waitElementNotPresent(driver, "//*[@href=\"/Logout\"]", 5);
    }

    @Then("I should not see the profile button")
    public void iShouldNotSeeTheProfileButton() {
        SeleniumUtils.waitElementNotPresent(driver, "//*[@id=\"btnUser\"]", 5);
    }

    @When("I try to access a non existent page")
    public void iTryToAccessANonExistentPage() {
        driver.navigate().to(URL + "nonexistent");
    }

    @Then("I should see the {int} page")
    public void iShouldSeeThePage(int errorCode) {
        List<WebElement> elems = SeleniumUtils.waitLoadElementsBy(driver, "free", "/html/body/div[1]/div[1]/strong", 5);
        Assertions.assertEquals(elems.get(0).getText(), String.valueOf(errorCode));
    }

    @When("I click the about WIChat button")
    public void iClickTheAboutWIChatButton() {
        List<WebElement> elems = SeleniumUtils.waitLoadElementsByXpath(driver, "/html/body/nav/div/ul[1]/li[5]/a", 5);
        elems.get(0).click();
    }
    @Then("I should see the about WIChat page")
    public void iShouldSeeTheAboutWIChatPage() {
        SeleniumUtils.waitLoadElementsBy(driver, "h2","WIChat: Desarrollo y Objetivos", 5);
    }
}
