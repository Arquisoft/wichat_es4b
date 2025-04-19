package com.uniovi.steps;

import com.uniovi.Wichat_IntegrationTests;
import com.uniovi.util.SeleniumUtils;
import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebElement;

import java.util.List;

public class GameSteps extends Wichat_IntegrationTests {

    @And("I press the image game button")
    public void iPressTheImageGameButton() {
        List<WebElement> elements = SeleniumUtils.waitLoadElementsByXpath(driver, "//*[@id=\"navbarDropdown\"]", 5);
        elements.get(0).click();
        elements = SeleniumUtils.waitLoadElementsByXpath(driver, "/html/body/nav/div/ul[1]/li[2]/ul/li[3]/a", 5);
        elements.get(0).click();
    }

    @When("I play the game")
    public void iPlayTheGame() {
        List<WebElement> elements = SeleniumUtils.waitLoadElementsByXpath(driver, "//*[@id=\"btn133\"]", 5);
        elements.get(0).click();
        elements = SeleniumUtils.waitLoadElementsByXpath(driver, "//*[@id=\"continueBtn\"]", 5);
        elements.get(0).click();
        elements = SeleniumUtils.waitLoadElementsByXpath(driver, "//*[@id=\"btn133\"]", 5);
        elements.get(0).click();
        elements = SeleniumUtils.waitLoadElementsByXpath(driver, "//*[@id=\"continueBtn\"]", 5);
        elements.get(0).click();
        elements = SeleniumUtils.waitLoadElementsByXpath(driver, "//*[@id=\"btn133\"]", 5);
        elements.get(0).click();
        elements = SeleniumUtils.waitLoadElementsByXpath(driver, "//*[@id=\"continueBtn\"]", 5);
        elements.get(0).click();
        elements = SeleniumUtils.waitLoadElementsByXpath(driver, "//*[@id=\"btn133\"]", 5);
        elements.get(0).click();
        elements = SeleniumUtils.waitLoadElementsByXpath(driver, "//*[@id=\"continueBtn\"]", 5);
        elements.get(0).click();
    }
}
