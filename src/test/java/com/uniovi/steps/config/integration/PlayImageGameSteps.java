package com.uniovi.steps.config.integration;

import com.uniovi.Wichat_IntegrationTests;
import com.uniovi.util.PropertiesExtractor;
import com.uniovi.util.SeleniumUtils;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.util.List;

public class PlayImageGameSteps extends Wichat_IntegrationTests {
    int current_question = 1;


    @When("I click the image game button")
    public void i_click_the_image_game_button() {
        List<WebElement> elements = SeleniumUtils.waitLoadElementsByXpath(driver, "/html/body/div[2]/div/div[3]/table/tbody/tr[2]/td[2]/a", 10);
        elements.get(0).click();
    }

    @Then("I should see the image game start")
    public void i_should_see_the_image_game_start() {
        SeleniumUtils.waitLoadElementsBy(driver, "h1", p.getString("imageGame.text", PropertiesExtractor.getSPANISH()), 10);
        List<WebElement> round = SeleniumUtils.waitLoadElementsByXpath(driver, "//*[@id=\"currentQuestion\"]", 10);
        Assertions.assertEquals(Integer.parseInt(round.get(0).getText()), current_question);
    }

    @When("I answer with a city")
    public void i_answer_a_city() {
        List<WebElement> elements=null;
        try {
            elements = SeleniumUtils.waitLoadElementsByXpath(driver, "/html/body/div/div/div[1]/div[2]/div/div[1]", 10);
        }catch (TimeoutException e){
            elements = SeleniumUtils.waitLoadElementsByXpath(driver, "/html/body/div/div[1]/div[2]/div/div[1]", 10);
        }
        elements.get(0).click();

    }

    @Then("I should see next question advance option")
    public void i_should_see_the_next_question_advance_option() {
        SeleniumUtils.waitLoadElementsBy(driver, "button", p.getString("game.continue", PropertiesExtractor.getSPANISH()), 10);
    }

    @When("I go to next question")
    public void i_go_to_next_question() {
        current_question++;
        List<WebElement> elements = SeleniumUtils.waitLoadElementsByXpath(driver, "//*[@id=\"continueBtn\"]", 10);
        elements.get(0).click();
    }

    @Then("I should see one round advanced")
    public void iShouldSeeTheNextRoundAdvance() {
        List<WebElement> round = SeleniumUtils.waitLoadElementsByXpath(driver, "//*[@id=\"currentQuestion\"]", 10);
        Assertions.assertEquals(Integer.parseInt(round.get(0).getText()), current_question);
    }

    @Then("I should see the game finished")
    public void iShouldSeeTheGameFinished() {
        SeleniumUtils.waitLoadElementsBy(driver, "h1", p.getString("game.finish", PropertiesExtractor.getSPANISH()), 10);
    }

}
