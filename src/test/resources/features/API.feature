Feature: I try to get an API key

    Scenario: I get an API key without already having one
        Given I am not registered or logged in
        And I am on the register page
        When I fill in the form with valid data username: "user1" email: "user1@gmail.com" password: "password" password_confirmation: "password"
        And I press the register button
        And I go to the API key page
        Then I should see the API key button
        When I press the API key button
        Then I should see the API key

    Scenario: I get an API key with already having one
        Given I am not registered or logged in
        And I am on the register page
        When I fill in the form with valid data username: "user1" email: "user1@gmail.com" password: "password" password_confirmation: "password"
        And I press the register button
        And I go to the API key page
        And I press the API key button
        And I reenter the API key page
        Then I should see the API key directly
