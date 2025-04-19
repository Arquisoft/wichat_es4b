Feature: New
    Scenario: I play the game
        Given I am not registered or logged in
        And I am on the register page
        When I fill in the form with valid data username: "user1" email: "user1@gmail.com" password: "password" password_confirmation: "password"
        And I press the register button
        And I press the image game button
        When I play the game
        Then I should see the message "game.points"