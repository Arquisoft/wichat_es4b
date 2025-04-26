package com.uniovi.steps.config.test;

import com.uniovi.Wichat_IntegrationTests;
import com.uniovi.util.PropertiesExtractor;
import com.uniovi.util.SeleniumUtils;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoginAndSignUpSteps extends Wichat_IntegrationTests {
    @Given("I am not registered or logged in")
    public void i_am_not_registered_or_logged_in() {
        driver.manage().deleteAllCookies();
        driver.navigate().to(URL);
    }

    @And("I am on the register page")
    public void iAmOnTheRegisterPage() {
        List<WebElement> elements = SeleniumUtils.waitLoadElementsByXpath(driver, "//*[@id=\"gameBtn\"]", 10);
        elements.get(0).click();
        List<WebElement> elems = SeleniumUtils.waitLoadElementsBy(driver, "@href", "signup", 10);
        elems.get(0).click();
    }


    @And("I am on the logout register page")
    public void iAmOnTheLogoutRegisterPage() {
        List<WebElement> elems = SeleniumUtils.waitLoadElementsBy(driver, "@href", "signup", 10);
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

    @And("I press the profile button")
    public void iPressTheProfileButton() {
        driver.findElement(By.xpath("//*[@id=\"btnUser\"]")).click();
        driver.findElement(By.xpath("/html/body/nav/div/ul[2]/li[2]/div/a[3]")).click();
    }

    @When("I edit my email")
    public void whenIEditMyEmail() {
        WebElement emailInput = driver.findElement(By.id("email"));
        emailInput.clear();
        emailInput.sendKeys("nuevo@correo.com");
        WebElement saveButton = driver.findElement(By.xpath("/html/body/div/form/div[3]/div/button"));
        saveButton.click();
    }
    @When("I edit my username")
    public void whenIEditMyUsername() {
        WebElement emailInput = driver.findElement(By.id("username"));
        emailInput.clear();
        emailInput.sendKeys("nuevo");
        WebElement saveButton = driver.findElement(By.xpath("/html/body/div/form/div[3]/div/button"));
        saveButton.click();
    }

    @When("I edit my username with same username")
    public void whenIEditMyUsernameWithSameUsername() {
        WebElement emailInput = driver.findElement(By.id("username"));
        emailInput.clear();
        emailInput.sendKeys("user1");
        WebElement saveButton = driver.findElement(By.xpath("/html/body/div/form/div[3]/div/button"));
        saveButton.click();
    }

    @When("I edit my username with same email")
    public void whenIEditMyUsernameWithSameEmail() {
        WebElement emailInput = driver.findElement(By.id("email"));
        emailInput.clear();
        emailInput.sendKeys("user1@gmail.com");
        WebElement saveButton = driver.findElement(By.xpath("/html/body/div/form/div[3]/div/button"));
        saveButton.click();
    }

    @Then("I should see the message {string}")
    public void iShouldSeeTheMessage(String message) {
        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains(message), "The page should contain the text '" + message + "'");
    }
    @Then("I login with my new username")
    public void iLoginWithMyNewUsername(){
        WebElement username = driver.findElement(By.id("username"));
        username.clear();
        username.sendKeys("nuevo");
        WebElement password = driver.findElement(By.id("password"));
        password.clear();
        password.sendKeys("password");

        WebElement loginButton = driver.findElement(By.className("btn"));
        loginButton.click();


    }
    @Then("I should see the message new username")
    public void iShouldSeeTheMessageNewUsername() {
        driver.findElement(By.xpath("//*[@id=\"btnUser\"]")).click();
        driver.findElement(By.xpath("//*[@id=\"btnUserProfile\"]")).click();
        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("nuevo"), "The page should contain the text 'nuevo@correo.com'");

    }
    @Then("I login with my new email")
    public void iLoginWithMyNewEmail(){
        WebElement username = driver.findElement(By.id("username"));
        username.clear();
        username.sendKeys("user1");
        WebElement password = driver.findElement(By.id("password"));
        password.clear();
        password.sendKeys("password");

        WebElement loginButton = driver.findElement(By.className("btn"));
        loginButton.click();


    }

    @Then("I should see the message new email")
    public void iShouldSeeTheMessageNewEmail() {
        driver.findElement(By.xpath("//*[@id=\"btnUser\"]")).click();
        driver.findElement(By.xpath("//*[@id=\"btnUserProfile\"]")).click();
        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("nuevo@correo.com"), "The page should contain the text 'nuevo@correo.com'");

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
    @And("I press the signup button")
    public void iPressTheSignupButton() {
        List<WebElement> elems = driver.findElements(By.xpath("/html/body/div/div/nav/a"));
        elems.get(0).click();
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

    //Signup
    @When("I fill in the form with invalid data username: {string} email: {string} password: {string} password_confirmation: {string}")
    public void iFillInTheFormWithInvalidDataUsernameEmailPasswordPassword_confirmation(String username, String email, String pass, String pass2) {
        this.iFillInTheFormWithValidDataUsernameEmailPasswordPassword_confirmation(username, email, pass, pass2);
    }

}