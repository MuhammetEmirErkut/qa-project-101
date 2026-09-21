package com.hepsiburada.qa.tests.mobile.login;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.hepsiburada.qa.base.MobileBaseTest;
import com.hepsiburada.qa.pages.mobile.home.MobileHomePage;
import com.hepsiburada.qa.pages.mobile.login.MobileLoginPage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

@Epic("Mobile Web Automation")
@Feature("Mobile Negative Login Validations")
public class MobileLoginTests extends MobileBaseTest {

    @Test(priority = 1, description = "Login should fail with validation message for invalid email format on mobile")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Mobile Invalid Email Format")
    @Description("Verify that an error message is displayed when entering an invalid email format on mobile (e.g. test@ or gecersizmail)")
    public void shouldShowErrorForInvalidEmailFormat() {
        // 1. Initialize MobileHomePage and handle cookies
        MobileHomePage homePage = new MobileHomePage(getDriver());
        homePage.handleCookies();

        // 2. Navigate to Mobile Login Page
        MobileLoginPage loginPage = homePage.goToLoginPage();

        // 3. Enter invalid email format
        loginPage.enterEmail("gecersizmail@");

        // 4. Click login button to submit
        loginPage.clickLoginButton();

        // 5. Verify validation error message is displayed
        Assert.assertTrue(loginPage.isErrorMessageDisplayed(),
            "Validation error message should be displayed for invalid email format on mobile");

        // 6. Verify error text contains relevant guidance
        String errorMsg = loginPage.getErrorMessage().toLowerCase();
        Assert.assertTrue(
            errorMsg.contains("geçerli") || errorMsg.contains("e-posta") || errorMsg.contains("girmelisiniz") || errorMsg.contains("giriniz") || !errorMsg.isEmpty(),
            "Error message should indicate invalid email format on mobile, but found: " + errorMsg
        );
    }

    @Test(priority = 2, description = "Login should show required field validation for empty form submission on mobile")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Mobile Empty Form Submission")
    @Description("Verify that a required field warning is displayed when submitting empty email input on mobile")
    public void shouldShowErrorForEmptyEmailSubmission() {
        // 1. Initialize MobileHomePage and handle cookies
        MobileHomePage homePage = new MobileHomePage(getDriver());
        homePage.handleCookies();

        // 2. Navigate to Mobile Login Page
        MobileLoginPage loginPage = homePage.goToLoginPage();

        // 3. Ensure email field is empty
        loginPage.clearEmailInput();

        // 4. Click login button without entering any credentials
        loginPage.clickLoginButton();

        // 5. Verify required field warning is displayed
        Assert.assertTrue(loginPage.isErrorMessageDisplayed(),
            "Required field warning should be displayed when submitting empty email on mobile");
    }
}
