Feature: I try to start the api
    Scenario: I try to start the api
          Given I am not registered or logged in
          And I am on the register page
          When I fill in the form with valid data username: "user1" email: "user1@gmail.com" password: "password" password_confirmation: "password"
          And I press the register button
          And I press the api key button
          And I generate my api key
          Then I should see the api key
