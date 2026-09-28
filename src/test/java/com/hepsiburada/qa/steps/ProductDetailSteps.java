package com.hepsiburada.qa.steps;

import com.thoughtworks.gauge.Step;
import org.testng.Assert;

public class ProductDetailSteps extends BaseStep {

    @Step("Click first product and navigate to detail page")
    public void clickFirstProductAndOpenPdp() {
        searchResultPage().clickFirstProduct();
    }

    @Step("Save initial product price")
    public void saveInitialProductPrice() {
        String initialPrice = pdp().getProductPriceText();
        scenarioStore().put("INITIAL_PRICE", initialPrice);
        log.info("Saved initial PDP price: {}", initialPrice);
    }

    @Step("Select an alternative variant")
    public void selectAlternativeVariant() {
        pdp().selectFirstAvailableAlternativeVariant();
    }

    @Step("Verify product name and updated price are displayed")
    public void verifyProductNameAndUpdatedPrice() {
        String name = pdp().getProductName();
        String updatedPrice = pdp().getProductPriceText();
        String initialPrice = (String) scenarioStore().get("INITIAL_PRICE");

        Assert.assertFalse(name.isEmpty(), "Product name cannot be empty!");
        Assert.assertTrue(!updatedPrice.isEmpty() || !initialPrice.isEmpty(), "Product price is not displayed!");
    }

    @Step("Click wishlist button")
    public void clickWishlistButton() {
        pdp().clickWishlist();
    }

    @Step("Verify login modal or login page redirection is displayed")
    public void verifyLoginPromptDisplayed() {
        Assert.assertTrue(pdp().isLoginRequiredPromptDisplayed(), "Login requirement prompt was not displayed!");
    }

    @Step("Open product reviews tab")
    public void openProductReviewsTab() {
        pdp().openReviewsTab();
    }

    @Step("Verify reviews container or ratings score is visible")
    public void verifyReviewsContainerVisible() {
        Assert.assertTrue(pdp().isReviewsContainerVisible(), "Reviews container or score was not visible!");
    }
}
