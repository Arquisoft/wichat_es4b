Feature: I try to log in
    Scenario: I log in with valid credentials
      Given I am not registered or logged in
      And I am on the register page
      When I fill in the form with valid data username: "user1" email: "user1@gmail.com" password: "password" password_confirmation: "password"
      And I press the register button
      And I logout
      And I am on the login page
      And I fill in the form with valid data email: "user1" password: "password"
      And I press the login button
      Then I should see the profile page

    Scenario: I log in with invalid credentials
          Given I am not registered or logged in
          And I am on the register page
          When I fill in the form with valid data username: "user1" email: "user1@gmail.com" password: "password" password_confirmation: "password"
          And I press the register button
          And I logout
          And I am on the login page
          And I fill in the form with valid data email: "user1" password: "pass"
          And I press the login button
          Then I should see the error message "login.error"

    Scenario: When im logged in i can access the personal ranking page
      Given I am not registered or logged in
      And I am on the register page
      When I fill in the form with valid data username: "user1" email: "user1@gmail.com" password: "password" password_confirmation: "password"
      And I press the register button
      And I logout
      And I am on the login page
      And I fill in the form with valid data email: "user1" password: "password"
      And I press the login button
      And I access the personal ranking page
      Then I should see the personal ranking page