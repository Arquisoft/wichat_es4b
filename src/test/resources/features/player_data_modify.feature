Feature: I enter the webpage and I try to modify my user data

    Scenario: I Register with valid data and I modify my email data
        Given I am not registered or logged in
        And I am on the register page
        When I fill in the form with valid data username: "user1" email: "user1@gmail.com" password: "password" password_confirmation: "password"
        And I press the register button
        And I press the profile button
        When I edit my email
        Then I login with my new email
        Then I should see the message new email

    Scenario: I Register with valid data and I modify my username data
        Given I am not registered or logged in
        And I am on the register page
        When I fill in the form with valid data username: "user1" email: "user1@gmail.com" password: "password" password_confirmation: "password"
        And I press the register button
        And I press the profile button
        When I edit my username
        Then I login with my new username
        Then I should see the message new username

    Scenario: I try to edit my username with repited username
          Given I am not registered or logged in
          And I am on the register page
          When I fill in the form with valid data username: "user1" email: "user1@gmail.com" password: "password" password_confirmation: "password"
          And I press the register button
          And I logout
          And I press the signup button
          When I fill in the form with valid data username: "user2" email: "user2@gmail.com" password: "password" password_confirmation: "password"
          And I press the register button
          And I press the profile button
          When I edit my username with same username
          Then I should see the error message "edit.error.same.name"

        Scenario: I try to edit my username with repited email
        Given I am not registered or logged in
              And I am on the register page
              When I fill in the form with valid data username: "user1" email: "user1@gmail.com" password: "password" password_confirmation: "password"
              And I press the register button
              And I logout
              And I press the signup button
              When I fill in the form with valid data username: "user2" email: "user2@gmail.com" password: "password" password_confirmation: "password"
              And I press the register button
              And I press the profile button
              When I edit my username with same email
              Then I should see the error message "edit.error.same.email"