Feature: I play the image game as a logged user
    @sampleData
    Scenario: I see the title
       Given I am not registered or logged in
       And I am on the register page
       When I fill in the form with valid data username: "user1" email: "user1@gmail.com" password: "password" password_confirmation: "password"
       And I press the register button
       And I logout
       And I am on the login page
       And I fill in the form with valid data email: "user1" password: "password"
       And I press the login button
       Then I should see the profile page
       When I click the image game button
       Then I should see the image game start
       When I answer with a city
       Then I should see next question advance option
       When I go to next question
       Then I should see one round advanced
       When I answer with a city
       Then I should see next question advance option
       When I go to next question
       Then I should see one round advanced
       When I answer with a city
       Then I should see next question advance option
       When I go to next question
       Then I should see one round advanced
       When I answer with a city
       Then I should see next question advance option
       When I go to next question
       Then I should see the game finished