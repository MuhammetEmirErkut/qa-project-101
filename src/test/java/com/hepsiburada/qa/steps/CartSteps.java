package com.hepsiburada.qa.steps;

import com.thoughtworks.gauge.Step;
import org.testng.Assert;

public class CartSteps extends BaseStep {

    @Step("Save expected product name")
    public void saveExpectedProductName() {
        String name = pdp().getProductName();
        scenarioStore().put("EXPECTED_PRODUCT_NAME", name);
        log.info("Saved expected product name: {}", name);
    }

    @Step("Add product to cart and go to cart page")
    public void addProductToCartAndGoToCart() {
        pdp().addToCart().goToCart();
    }

    @Step("Verify cart contains at least 1 item")
    public void verifyCartContainsItems() {
        int count = cartPage().getCartItemCount();
        Assert.assertTrue(count > 0, "Cart is empty! Item count: " + count);
    }

    @Step("Verify first cart item name is not empty")
    public void verifyFirstCartItemNameNotEmpty() {
        String itemName = cartPage().getFirstCartItemName();
        Assert.assertFalse(itemName.isEmpty(), "First cart item name is empty!");
    }

    @Step("Verify cart contains at least 1 item before removal")
    public void verifyCartContainsItemsBeforeRemoval() {
        Assert.assertTrue(cartPage().getCartItemCount() > 0, "Cart must have items prior to deletion!");
    }

    @Step("Remove first product from cart")
    public void removeFirstProductFromCart() {
        cartPage().removeFirstProduct();
    }

    @Step("Verify cart is empty")
    public void verifyCartIsEmpty() {
        Assert.assertTrue(cartPage().isCartEmpty(), "Cart should be empty after item removal!");
    }
}
