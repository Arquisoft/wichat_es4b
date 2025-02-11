Feature: I play a multiplayer game
    Scenario: I start a multiplayer game alone
      Given I am not registered or logged in
      And I am on the register page
      When I fill in the form with valid data username: "userGame" email: "userGame@gmail.com" password: "password" password_confirmation: "password"
      And I press the register button
      And I go to the home page
      When I press Play With Friends
      And I create a code
      And I press start
      Then I should start playing until I see message "multi.info"
      And I see the multiplayer results

  Scenario: I start a multiplayer game
    Given I am not registered or logged in
    And I am on the register page
    When I fill in the form with valid data username: "userGame" email: "userGame@gmail.com" password: "password" password_confirmation: "password"
    And I press the register button
    And I go to the home page
    When I press Play With Friends
    And I create a code
    And I save the code
    Then I logout
    Given I am not registered or logged in
    And I am on the register page
    When I fill in the form with valid data username: "userGame2" email: "userGame2@gmail.com" password: "password" password_confirmation: "password"
    And I press the register button
    And I go to the home page
    And I press Play With Friends
    And I fill in the saved code
    And I press start
    And I reload the page
    Then I should start playing until I see message "multi.info"
    And I see the multiplayer results