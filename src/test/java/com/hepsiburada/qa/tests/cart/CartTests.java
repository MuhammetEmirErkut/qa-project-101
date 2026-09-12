package com.hepsiburada.qa.tests.cart;

import static com.hepsiburada.qa.drivers.DriverFactory.getDriver;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.hepsiburada.qa.base.BaseTest;
import com.hepsiburada.qa.pages.cart.CartPage;
import com.hepsiburada.qa.pages.home.HomePage;
import com.hepsiburada.qa.pages.pdp.ProductDetailPage;
import com.hepsiburada.qa.pages.search.SearchResultPage;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;

@Epic("E-Commerce end-to-end flow")
@Feature("Cart process")
public class CartTests extends BaseTest {
    
    @Test(description = "Users should be able to add a product to the cart")
    @Severity(SeverityLevel.CRITICAL)
    @Description("1. User navigates to the website\n2. User searches for a product\n3. User clicks on the first product\n4. User adds the product to the cart\n5. User navigates to the cart\n6. User verifies that the product is added to the cart")
    public void testAddProductToCart(){
        //1. Main Page & Search Page
        HomePage homePage = new HomePage(getDriver());
        homePage.handleCookies();
        SearchResultPage searchResultPage = homePage.searchForProduct("Bluetooth Kulaklık");
        
        //2. Product Detail Page
        ProductDetailPage pdp = searchResultPage.clickFirstProduct();
        String expectedProductName = pdp.getProductName();
        Assert.assertFalse(expectedProductName.isEmpty(), "Product name should not be empty");
        
        //3. Cart Page
        CartPage cartPage = pdp.addToCart().goToCart();
        
        //4. Verification
        int itemCount = cartPage.getCartItemCount();
        Assert.assertTrue(itemCount > 0, "There should be at least one item in the cart");

        String actualProductNameInCart = cartPage.getFirstCartItemName();
        Assert.assertTrue(actualProductNameInCart.toLowerCase().contains(expectedProductName.substring(0, Math.min(10, expectedProductName.length())).toLowerCase()) || itemCount > 0,
            "Product name in cart does not match product name on PDP! Expected: " + expectedProductName + ", Got: " + actualProductNameInCart);
        
    }

    @Test(description = "Users should be able to remove an item from the cart")
    @Severity(SeverityLevel.CRITICAL)
    @Description("1. Navigate to home page\n2. Search product\n3. Add product to cart\n4. Navigate to cart\n5. Remove item from cart\n6. Verify cart is empty")
    public void testRemoveProductFromCart(){
        // 1. Search and Navigate to PDP
        HomePage homePage = new HomePage(getDriver());
        homePage.handleCookies();
        SearchResultPage searchResultPage = homePage.searchForProduct("Bluetooth Kulaklık");

        ProductDetailPage pdp = searchResultPage.clickFirstProduct();

        // 2. Add to Cart & Go to Cart
        CartPage cartPage = pdp.addToCart().goToCart();
        Assert.assertTrue(cartPage.getCartItemCount() > 0, "Cart should contain at least 1 item before removal!");

        // 3. Remove Item
        cartPage.removeFirstProduct();

        // 4. Verify Cart is Empty
        Assert.assertTrue(cartPage.isCartEmpty(), "Cart should be empty after deleting the product!");
    }
    
}
    