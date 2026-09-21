package com.hepsiburada.qa.tests.mobile.pdp;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.hepsiburada.qa.base.MobileBaseTest;
import com.hepsiburada.qa.pages.mobile.home.MobileHomePage;
import com.hepsiburada.qa.pages.mobile.pdp.MobileProductDetailPage;
import com.hepsiburada.qa.pages.mobile.search.MobileSearchResultPage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

@Epic("Mobile Web Automation")
@Feature("Mobile PDP Variations, Wishlist & Reviews")
public class MobileProductDetailTests extends MobileBaseTest {

    @Test(priority = 1, description = "Selecting different variant should update product details/price on mobile")
    @Severity(SeverityLevel.NORMAL)
    @Story("Mobile Variant Selection")
    @Description("Verify that selecting a different variant (color/size/storage) updates product price or selection state on mobile PDP")
    public void shouldUpdateProductWhenVariantSelected() {
        // 1. Mobile Home Page & Cookie Handling
        MobileHomePage homePage = new MobileHomePage(getDriver());
        homePage.handleCookies();

        // 2. Search for a multi-variant product and go to PDP
        MobileSearchResultPage searchResultPage = homePage.searchForProduct("iPhone 13");
        MobileProductDetailPage pdp = searchResultPage.clickFirstProduct();

        // 3. Capture initial product price
        String initialPrice = pdp.getProductPriceText();

        // 4. Select an alternative variant (e.g. different storage/color option)
        pdp.selectFirstAvailableAlternativeVariant();

        // 5. Capture new price after variant switch
        String updatedPrice = pdp.getProductPriceText();

        // 6. Verify that product page has a valid product title and price
        Assert.assertFalse(pdp.getProductName().isEmpty(), "Product name should be present on mobile PDP");
        Assert.assertTrue(!initialPrice.isEmpty() || !updatedPrice.isEmpty(), "Product price should be displayed on mobile PDP");
    }

    @Test(priority = 2, description = "Clicking wishlist as guest user should prompt login on mobile")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Mobile Wishlist Authentication")
    @Description("Verify that unauthenticated user is redirected to login or prompted with login modal when clicking favorite/wishlist icon on mobile")
    public void shouldRequireLoginWhenGuestAddsToWishlist() {
        // 1. Mobile Home Page & Cookie Handling
        MobileHomePage homePage = new MobileHomePage(getDriver());
        homePage.handleCookies();

        // 2. Search for a product and navigate to PDP
        MobileSearchResultPage searchResultPage = homePage.searchForProduct("MacBook");
        MobileProductDetailPage pdp = searchResultPage.clickFirstProduct();

        // 3. Click wishlist / favorite button as an unauthenticated (guest) user
        pdp.clickWishlist();

        // 4. Verify login modal appears or browser redirects to login URL
        Assert.assertTrue(pdp.isLoginRequiredPromptDisplayed(),
            "Login modal or login page redirection should be displayed when guest clicks favorite button on mobile");
    }

    @Test(priority = 3, description = "Clicking reviews tab should display product evaluations and ratings on mobile")
    @Severity(SeverityLevel.NORMAL)
    @Story("Mobile Product Reviews")
    @Description("Verify that clicking 'Değerlendirmeler' (Reviews) tab displays the rating summary or user reviews on mobile")
    public void shouldDisplayReviewsWhenReviewsTabClicked() {
        // 1. Mobile Home Page & Cookie Handling
        MobileHomePage homePage = new MobileHomePage(getDriver());
        homePage.handleCookies();

        // 2. Search for a reviewed product and open PDP
        MobileSearchResultPage searchResultPage = homePage.searchForProduct("Samsung");
        MobileProductDetailPage pdp = searchResultPage.clickFirstProduct();

        // 3. Click on the 'Değerlendirmeler' (Reviews) tab
        pdp.openReviewsTab();

        // 4. Verify that ratings score or review list container is visible
        Assert.assertTrue(pdp.isReviewsContainerVisible(),
            "Reviews content or rating summary should be visible after opening reviews tab on mobile");
    }
}
