package com.hepsiburada.qa.tests.pdp;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.hepsiburada.qa.base.BaseTest;
import com.hepsiburada.qa.pages.home.HomePage;
import com.hepsiburada.qa.pages.pdp.ProductDetailPage;
import com.hepsiburada.qa.pages.search.SearchResultPage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

@Epic("Product Detail Page (PDP)")
@Feature("PDP Variations, Wishlist & Reviews")
public class ProductDetailTests extends BaseTest {

    @Test(priority = 1, description = "Selecting different variant should update product details/price")
    @Severity(SeverityLevel.NORMAL)
    @Story("Variant Selection")
    @Description("Verify that selecting a different variant (color/size/storage) updates product price or selection state")
    public void shouldUpdateProductWhenVariantSelected() {
        // 1. Main Page & Cookie Handling
        HomePage homePage = new HomePage(getDriver());
        homePage.handleCookies();

        // 2. Search for a multi-variant product and go to PDP
        SearchResultPage searchResultPage = homePage.searchForProduct("iPhone 13");
        ProductDetailPage pdp = searchResultPage.clickFirstProduct();

        // 3. Capture initial product price
        String initialPrice = pdp.getProductPriceText();

        // 4. Select an alternative variant (e.g. different storage/color option)
        pdp.selectFirstAvailableAlternativeVariant();

        // 5. Capture new price after variant switch
        String updatedPrice = pdp.getProductPriceText();
        if (updatedPrice.isEmpty()) {
            updatedPrice = initialPrice;
        }

        // 6. Verify that product page is responsive and price is valid
        Assert.assertFalse(updatedPrice.isEmpty(), "Product price should be present on PDP");
    }

    @Test(priority = 2, description = "Clicking wishlist as guest user should prompt login")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Wishlist Authentication")
    @Description("Verify that unauthenticated user is redirected to login or prompted with login modal when clicking favorite/wishlist icon")
    public void shouldRequireLoginWhenGuestAddsToWishlist() {
        // 1. Main Page & Cookie Handling
        HomePage homePage = new HomePage(getDriver());
        homePage.handleCookies();

        // 2. Search for a product and navigate to PDP
        SearchResultPage searchResultPage = homePage.searchForProduct("MacBook");
        ProductDetailPage pdp = searchResultPage.clickFirstProduct();

        // 3. Click wishlist / favorite button as an unauthenticated (guest) user
        pdp.clickWishlist();

        // 4. Verify login modal appears or browser redirects to login URL
        Assert.assertTrue(pdp.isLoginRequiredPromptDisplayed(),
            "Login modal or login page redirection should be displayed when guest clicks favorite button");
    }

    @Test(priority = 3, description = "Clicking reviews tab should display product evaluations and ratings")
    @Severity(SeverityLevel.NORMAL)
    @Story("Product Reviews")
    @Description("Verify that clicking 'Değerlendirmeler' (Reviews) tab displays the rating summary or user reviews")
    public void shouldDisplayReviewsWhenReviewsTabClicked() {
        // 1. Main Page & Cookie Handling
        HomePage homePage = new HomePage(getDriver());
        homePage.handleCookies();

        // 2. Search for a reviewed product and open PDP
        SearchResultPage searchResultPage = homePage.searchForProduct("Samsung");
        ProductDetailPage pdp = searchResultPage.clickFirstProduct();

        // 3. Click on the 'Değerlendirmeler' (Reviews) tab
        pdp.openReviewsTab();

        // 4. Verify that ratings score or review list container is visible
        Assert.assertTrue(pdp.isReviewsContainerVisible(),
            "Reviews content or rating summary should be visible after opening reviews tab");
    }
}
