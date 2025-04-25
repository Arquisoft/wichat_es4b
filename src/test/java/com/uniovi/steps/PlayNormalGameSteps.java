package com.uniovi.steps;

import com.uniovi.Wichat_IntegrationTests;
import com.uniovi.util.PropertiesExtractor;
import com.uniovi.util.SeleniumUtils;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.util.List;

public class PlayNormalGameSteps extends Wichat_IntegrationTests {
    int current_question = 1;


    @When("I click the hot game button")
    public void i_click_the_image_game_button() {
        List<WebElement> elements = SeleniumUtils.waitLoadElementsByXpath(driver, "/html/body/div[2]/div/div[3]/table/tbody/tr[1]/td[2]/a", 10);
        elements.get(0).click();
    }

    @Then("I should see the hot game start")
    public void i_should_see_the_image_game_start() {
        SeleniumUtils.waitLoadElementsBy(driver, "id", "totalQuestions", 10);
    }

    @When("I answer with a hot answer")
    public void i_answer_a_city() {
        List<WebElement> elements=SeleniumUtils.waitLoadElementsByXpath(driver, "/html/body/div/div[1]/div/div[1]", 10);
        elements.get(0).click();
    }

    @Then("I should see next hot question advance option")
    public void i_should_see_the_next_question_advance_option() {
        SeleniumUtils.waitLoadElementsBy(driver, "button", p.getString("game.continue", PropertiesExtractor.getSPANISH()), 10);
    }

    @When("I go to next hot question")
    public void i_go_to_next_question() {
        current_question++;
        List<WebElement> elements = SeleniumUtils.waitLoadElementsByXpath(driver, "//*[@id=\"continueBtn\"]", 15);
        elements.get(0).click();
    }

    @Then("I take a little nap")
    public void i_take_a_nap() {
        try {
            List<WebElement> elements = SeleniumUtils.waitLoadElementsByXpath(driver, "/html/body/div[2]/div/div[3]/table/tbody/tr[1]/td[2]/a", 5);
        }catch (Exception e){

        }
    }

    @And("I should see one round advanced or finish")
    public void iShouldSeeTheNextRoundAdvance() {
        try {
            List<WebElement> round = SeleniumUtils.waitLoadElementsByXpath(driver, "//*[@id=\"currentQuestion\"]", 10);
            Assertions.assertEquals(Integer.parseInt(round.get(0).getText()), current_question);
        }catch (Exception e){
            SeleniumUtils.waitLoadElementsBy(driver, "h1", p.getString("game.finish", PropertiesExtractor.getSPANISH()), 10);
        }
    }



}
