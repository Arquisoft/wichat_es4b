package com.uniovi.steps;

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

public class SignUpStep extends Wichat_IntegrationTests {

    @Given("I am not registered or logged in")
    public void i_am_not_registered_or_logged_in() {
        driver.manage().deleteAllCookies();
        driver.navigate().to(URL);
    }

    @And("I am on the register page")
    public void iAmOnTheRegisterPage() {
        List<WebElement> elems = SeleniumUtils.waitLoadElementsBy(driver, "@href", "signup", 5);
        elems.get(0).click();
    }

    @When("I fill in the form with valid data username: {string} email: {string} password: {string} password_confirmation: {string}")
    public void iFillInTheFormWithValidDataUsernameEmailPasswordPassword_confirmation(String username, String email, String pass, String pass2) {
        WebElement mail = driver.findElement(By.name("username"));
        mail.click();
        mail.clear();
        mail.sendKeys(username);
        WebElement name = driver.findElement(By.name("email"));
        name.click();
        name.clear();
        name.sendKeys(email);
        WebElement password = driver.findElement(By.name("password"));
        password.click();
        password.clear();
        password.sendKeys(pass);
        WebElement passwordConfirm = driver.findElement(By.name("passwordConfirm"));
        passwordConfirm.click();
        passwordConfirm.clear();
        passwordConfirm.sendKeys(pass2);
    }

    @When("I fill in the form with invalid data username: {string} email: {string} password: {string} password_confirmation: {string}")
    public void iFillInTheFormWithInvalidDataUsernameEmailPasswordPassword_confirmation(String username, String email, String pass, String pass2) {
        this.iFillInTheFormWithValidDataUsernameEmailPasswordPassword_confirmation(username, email, pass, pass2);
    }

    @And("I press the register button")
    public void iPressTheRegisterButton() {
        By button = By.className("btn");
        driver.findElement(button).click();
    }

    @Then("I should see the profile page")
    public void iShouldSeeTheProfilePage() {
        SeleniumUtils.waitLoadElementsBy(driver, "span", p.getString("home.authenticated_as", PropertiesExtractor.getSPANISH()), 5);
    }

    @Then("I should see the error message {string}")
    public void iShouldSeeTheErrorMessage(String errorMessage) {
        SeleniumUtils.waitLoadElementsBy(driver, "text", p.getString(errorMessage, PropertiesExtractor.getSPANISH()), 5);
    }

    @When("I logout")
    public void iLogout() {
        SeleniumUtils.waitLoadElementsBy(driver, "free", "//a[contains(@href, 'logout')]", 10).get(0).click();
    }
}
