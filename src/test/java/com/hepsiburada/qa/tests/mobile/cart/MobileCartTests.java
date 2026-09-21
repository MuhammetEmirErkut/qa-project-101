package com.hepsiburada.qa.tests.mobile.cart;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.hepsiburada.qa.base.MobileBaseTest;
import com.hepsiburada.qa.pages.mobile.cart.MobileCartPage;
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
@Feature("Mobile Cart Process")
public class MobileCartTests extends MobileBaseTest {

    @Test(description = "Users should be able to add a product to the cart on mobile")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Mobile Add To Cart")
    @Description("1. Navigate to mobile website\n2. Search product\n3. Click first product\n4. Add product to cart\n5. Navigate to cart\n6. Verify product in cart")
    public void testAddProductToCart() {
        // 1. Mobile Home Page & Search
        MobileHomePage homePage = new MobileHomePage(getDriver());
        homePage.handleCookies();
        MobileSearchResultPage searchResultPage = homePage.searchForProduct("Bluetooth Kulaklık");

        // 2. Mobile PDP
        MobileProductDetailPage pdp = searchResultPage.clickFirstProduct();
        String expectedProductName = pdp.getProductName();
        Assert.assertFalse(expectedProductName.isEmpty(), "Product name on mobile PDP should not be empty");

        // 3. Mobile Cart
        MobileCartPage cartPage = pdp.addToCart().goToCart();

        // 4. Verification
        int itemCount = cartPage.getCartItemCount();
        Assert.assertTrue(itemCount > 0, "There should be at least one item in the mobile cart");

        String actualProductNameInCart = cartPage.getFirstCartItemName();
        Assert.assertFalse(actualProductNameInCart.isEmpty(), "Product name in mobile cart should not be empty!");
    }

    @Test(description = "Users should be able to remove an item from the cart on mobile")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Mobile Remove From Cart")
    @Description("1. Navigate to mobile website\n2. Search product\n3. Add product to cart\n4. Navigate to cart\n5. Remove item\n6. Verify cart is empty")
    public void testRemoveProductFromCart() {
        // 1. Search and Navigate to PDP
        MobileHomePage homePage = new MobileHomePage(getDriver());
        homePage.handleCookies();
        MobileSearchResultPage searchResultPage = homePage.searchForProduct("Bluetooth Kulaklık");

        MobileProductDetailPage pdp = searchResultPage.clickFirstProduct();

        // 2. Add to Cart & Go to Cart
        MobileCartPage cartPage = pdp.addToCart().goToCart();
        Assert.assertTrue(cartPage.getCartItemCount() > 0, "Mobile cart should contain at least 1 item before removal!");

        // 3. Remove Item
        cartPage.removeFirstProduct();

        // 4. Verify Cart is Empty
        Assert.assertTrue(cartPage.isCartEmpty(), "Mobile cart should be empty after deleting the product!");
    }
}
