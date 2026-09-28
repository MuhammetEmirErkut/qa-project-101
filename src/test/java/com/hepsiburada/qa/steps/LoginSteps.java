package com.hepsiburada.qa.steps;

import com.thoughtworks.gauge.Step;
import org.testng.Assert;

public class LoginSteps extends BaseStep {

    @Step("Navigate to login page")
    public void navigateToLoginPage() {
        homePage().goToLoginPage();
    }

    @Step("Enter email <email> and click login")
    public void enterEmailAndClickLogin(String email) {
        loginPage().enterEmail(email).clickLoginButton();
    }

    @Step("Leave email blank and click login")
    public void leaveEmailBlankAndClickLogin() {
        loginPage().clearEmailInput().clickLoginButton();
    }

    @Step("Verify validation error message is displayed")
    public void verifyErrorMessageDisplayed() {
        Assert.assertTrue(loginPage().isErrorMessageDisplayed(), "Validation error message was not displayed!");
    }
}
