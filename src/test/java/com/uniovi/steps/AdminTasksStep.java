package com.uniovi.steps;

import com.uniovi.Wichat_IntegrationTests;
import com.uniovi.util.PropertiesExtractor;
import com.uniovi.util.SeleniumUtils;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

public class AdminTasksStep extends Wichat_IntegrationTests {
    @Then("I should see the admin zone button")
    public void iShouldSeeTheAdminZoneButton() {
        List<WebElement> elems = SeleniumUtils.waitLoadElements(driver, By.cssSelector("a.btn.btn-primary[href='/player/admin']"), 5);
        Assertions.assertEquals(1, elems.size());
    }

    @And("I enter the admin zone")
    public void iEnterTheAdminZone() {
        List<WebElement> elems = SeleniumUtils.waitLoadElements(driver, By.cssSelector("a.btn.btn-primary[href='/player/admin']"), 5);
        elems.get(0).click();
    }

    @And("I should see the message {string}")
    public void iShouldSeeTheAdminZone(String message) {
        SeleniumUtils.waitLoadElementsBy(driver, "text", p.getString(message, PropertiesExtractor.getSPANISH()), 5);
    }

    @Then("I click {string} button")
    public void iClickButton(String text) {
        List<WebElement> elems;
        if (!text.contains(".")) {
            elems = SeleniumUtils.waitLoadElementsBy(driver, "id", text, 5);
        } else {
            elems = SeleniumUtils.waitLoadElementsBy(driver, "free", "//button[contains(text(),'" + p.getString(text, PropertiesExtractor.getSPANISH()) + "')]", 5);
        }
        elems.get(0).click();
    }

    @And("I should see the modal dialog {string}")
    public void iShouldSeeTheModalDialog(String title) {
        SeleniumUtils.waitLoadElementsBy(driver, "text", p.getString(title, PropertiesExtractor.getSPANISH()), 5);
    }

    @And("I fill in the new password {string}")
    public void iFillInTheNewPassword(String pass) {
        WebElement password = driver.findElement(By.id("changePasswordInput"));
        password.click();
        password.clear();
        password.sendKeys(pass);
    }

    @And("I wait until modal dialog is closed")
    public void iWaitUntilModalDialogIsClosed() {
        SeleniumUtils.waitInvisibleElement(driver, By.id("changePasswordAdminModal"), 5);
        SeleniumUtils.waitInvisibleElement(driver, By.cssSelector(".modal-backdrop.fade"), 5);
    }

    @Then("I fill in the new role {string}")
    public void iFillInTheNewRole(String role) {
        WebElement password = driver.findElement(By.id("newRole"));
        password.click();
        password.clear();
        password.sendKeys(role);
    }

    @And("I see the text {string}")
    public void iSeeTheText(String text) {
        SeleniumUtils.waitLoadElementsBy(driver, "text", text, 5);
    }

    @And("I click {string} button for user {string}")
    public void iClickButtonForUser(String btn, String user) {
        List<WebElement> elems = SeleniumUtils.waitLoadElements(driver, By.cssSelector("button[data-bs-username='"+ user +"']"), 5).stream()
                .filter(e -> e.getText().equals(p.getString(btn, PropertiesExtractor.getSPANISH()))).toList();
        elems.get(0).click();
    }

    @Then("I should not see the text {string}")
    public void iShouldNotSeeTheText(String text) {
        SeleniumUtils.waitInvisibleElement(driver, By.xpath("//*[contains(text(),'" + text + "')]"), 5);
    }

    @When("I click question management button")
    public void iClickQuestionManagementButton() {
        List<WebElement> elems = SeleniumUtils.waitLoadElements(driver, By.id("tab2-tab"), 5);
        elems.get(0).click();
    }
}
