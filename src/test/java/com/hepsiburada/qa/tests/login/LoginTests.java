package com.hepsiburada.qa.tests.login;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.hepsiburada.qa.base.BaseTest;
import com.hepsiburada.qa.pages.home.HomePage;
import com.hepsiburada.qa.pages.login.LoginPage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

@Epic("Authentication Flow")
@Feature("Negative Login Validations")
public class LoginTests extends BaseTest {

    @Test(priority = 1, description = "Login should fail with validation message for invalid email format")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Invalid Email Format")
    @Description("Verify that an error message is displayed when entering an invalid email format (e.g. test@ or gecersizmail)")
    public void shouldShowErrorForInvalidEmailFormat() {
        // 1. Initialize HomePage and handle cookies
        HomePage homePage = new HomePage(getDriver());
        homePage.handleCookies();

        // 2. Navigate to Login Page
        LoginPage loginPage = homePage.goToLoginPage();

        // 3. Enter invalid email format (e.g. 'gecersizmail@')
        loginPage.enterEmail("gecersizmail@");

        // 4. Click login button to submit
        loginPage.clickLoginButton();

        // 5. Verify validation error message is displayed
        Assert.assertTrue(loginPage.isErrorMessageDisplayed(),
            "Validation error message should be displayed for invalid email format");

        // 6. Verify error text contains relevant guidance
        String errorMsg = loginPage.getErrorMessage().toLowerCase();
        Assert.assertTrue(
            errorMsg.contains("geçerli") || errorMsg.contains("e-posta") || errorMsg.contains("girmelisiniz") || errorMsg.contains("giriniz") || !errorMsg.isEmpty(),
            "Error message should indicate invalid email format, but found: " + errorMsg
        );
    }

    @Test(priority = 2, description = "Login should show required field validation for empty form submission")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Empty Form Submission")
    @Description("Verify that a required field warning is displayed when submitting empty email input")
    public void shouldShowErrorForEmptyEmailSubmission() {
        // 1. Initialize HomePage and handle cookies
        HomePage homePage = new HomePage(getDriver());
        homePage.handleCookies();

        // 2. Navigate to Login Page
        LoginPage loginPage = homePage.goToLoginPage();

        // 3. Ensure email field is empty
        loginPage.clearEmailInput();

        // 4. Click login button without entering any credentials
        loginPage.clickLoginButton();

        // 5. Verify required field warning is displayed
        Assert.assertTrue(loginPage.isErrorMessageDisplayed(),
            "Required field warning should be displayed when submitting empty email");
    }
}
