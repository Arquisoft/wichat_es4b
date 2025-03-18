package com.uniovi.steps;

import com.uniovi.Wichat_IntegrationTests;
import com.uniovi.util.PropertiesExtractor;
import com.uniovi.util.SeleniumUtils;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.After;
import io.cucumber.datatable.DataTable;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoginSteps extends Wichat_IntegrationTests {
    @Given("I am not registered or logged in")
    public void i_am_not_registered_or_logged_in() {
        driver.manage().deleteAllCookies();
        driver.navigate().to(URL);
    }

    @And("I am on the register page")
    public void iAmOnTheRegisterPage() {
        List<WebElement> elements = SeleniumUtils.waitLoadElementsByXpath(driver, "//*[@id=\"gameBtn\"]", 5);
        elements.get(0).click();
        List<WebElement> elems = SeleniumUtils.waitLoadElementsBy(driver, "@href", "signup", 5);
        elems.get(0).click();
    }
    @When("I fill in the form with valid data username: {string} email: {string} password: {string} password_confirmation: {string}")
    public void iFillInTheFormWithValidDataUsernameEmailPasswordPassword_confirmation(String username, String email, String pass, String pass2) {
        WebElement mail = driver.findElement(By.id("username"));
        mail.click();
        mail.clear();
        mail.sendKeys(username);
        WebElement name = driver.findElement(By.id("email"));
        name.click();
        name.clear();
        name.sendKeys(email);
        WebElement password = driver.findElement(By.id("password"));
        password.click();
        password.clear();
        password.sendKeys(pass);
        WebElement passwordConfirm = driver.findElement(By.id("passwordConfirm"));
        passwordConfirm.click();
        passwordConfirm.clear();
        passwordConfirm.sendKeys(pass2);
    }
    @And("I press the register button")
    public void iPressTheRegisterButton() {
        By button = By.className("btn");
        driver.findElement(button).click();
    }

    @When("I logout")
    public void iLogout() {
        SeleniumUtils.waitLoadElementsBy(driver, "free", "//a[contains(@href, 'logout')]", 10).get(0).click();
    }
    @And("I am on the login page")
    public void iAmOnTheLoginPage() {
        iClickTheLoginButton();
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
    @And("I press the login button")
    public void iPressTheLoginButton() {
        By button = By.className("btn");
        driver.findElement(button).click();
    }
    @Then("I should see the profile page")
    public void iShouldSeeTheProfilePage() {
        // Check if the text "Modos de juego" appears on the page
        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("Modos de juego"), "Profile page should contain text 'Modos de juego'");
    }

    @When("I click the login button")
    public void iClickTheLoginButton() {
        List<WebElement> elems = SeleniumUtils.waitLoadElementsBy(driver, "@href", "login", 5);
        elems.get(0).click();
    }
    //caso 2
    @Then("I should see the error message {string}")
    public void iShouldSeeTheErrorMessage(String errorMessage) {
        SeleniumUtils.waitLoadElementsBy(driver, "text", p.getString(errorMessage, PropertiesExtractor.getSPANISH()), 5);
    }
    //caso 3
    @And("I access the personal ranking page")
    public void iAccessThePersonalRankingPage() {
        List<WebElement> elems = SeleniumUtils.waitLoadElementsBy(driver, "free", "/html/body/nav/div/ul[1]/li[3]", 5);
        elems.get(0).click();
        elems = SeleniumUtils.waitLoadElementsBy(driver, "free", "/html/body/nav/div/ul[1]/li[3]/ul/li[1]/a", 5);
        elems.get(0).click();
    }
    @Then("I should see the personal ranking page")
    public void iShouldSeeThePersonalRankingPage() {
        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("Ranking"), "Profile page should contain text 'Modos de juego'");

    }


}