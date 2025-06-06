Feature: I play the hot game as a logged user
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
       When I click the hot game button
       Then I should see the hot game start
       When I answer with a hot answer
       Then I should see next hot question advance option
       When I go to next hot question
       Then I take a little nap
       And I should see one round advanced or finish
