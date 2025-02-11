Feature: I do admin tasks
    Scenario: I can see the admin tasks
        Given I am in the home page
        When I click the login button
        And I fill in the form with valid data email: "test" password: "test"
        And I press the login button
        Then I should see the admin zone button
        And I enter the admin zone
        And I should see the message "navbar.section.admin"

    Scenario: I change user password
        Given I am in the home page
        When I click the login button
        And I fill in the form with valid data email: "test" password: "test"
        And I press the login button
        And I enter the admin zone
        And I should see the message "navbar.section.admin"
        And I click "user.details" button
        And I click "admin.changepassword" button
        And I should see the modal dialog "modal.password.title"
        And I fill in the new password "1234"
        And I click "changePasswordConfirm" button
        Then I wait until modal dialog is closed
        And I logout
        And I fill in the form with valid data email: "test" password: "1234"
        And I press the login button
        And I should see the admin zone button

    Scenario: I add a role to user
        Given I am in the home page
        When I click the login button
        And I fill in the form with valid data email: "test" password: "test"
        And I press the login button
        And I enter the admin zone
        And I should see the message "navbar.section.admin"
        And I click "user.details" button
        And I click "admin.changeroles" button
        And I should see the modal dialog "admin.roles.change"
        Then I fill in the new role "ROLE_CUCUMBER"
        And I click "changeRolesConfirm" button
        And I wait until modal dialog is closed
        And I see the text "ROLE_CUCUMBER"

    Scenario: A user signs up and I delete it
        Given I am not registered or logged in
        And I am on the register page
        When I fill in the form with valid data username: "user1" email: "user1@gmail.com" password: "password" password_confirmation: "password"
        And I press the register button
        Then I should see the profile page
        And I logout
        And I fill in the form with valid data email: "test" password: "test"
        And I press the login button
        And I enter the admin zone
        And I should see the message "navbar.section.admin"
        When I click "user.details" button for user "user1"
        And I click "admin.user.delete" button for user "user1"
        And I should see the modal dialog "admin.user.delete.title"
        And I click "deleteModalConfirm" button
        Then I should not see the text "user1"

    Scenario: I save question JSON
        Given I am in the home page
        When I click the login button
        And I fill in the form with valid data email: "test" password: "test"
        And I press the login button
        Then I should see the admin zone button
        And I enter the admin zone
        And I should see the message "navbar.section.admin"
        When I click question management button
        Then I click "saveButton" button