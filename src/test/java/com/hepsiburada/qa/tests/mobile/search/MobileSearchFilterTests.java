package com.hepsiburada.qa.tests.mobile.search;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.hepsiburada.qa.base.MobileBaseTest;
import com.hepsiburada.qa.pages.mobile.home.MobileHomePage;
import com.hepsiburada.qa.pages.mobile.search.MobileSearchResultPage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

@Epic("Mobile Web Automation")
@Feature("Mobile Search Filtering and Sorting Tests")
public class MobileSearchFilterTests extends MobileBaseTest {

    @Test(description = "Verify that products can be filtered by brand on mobile")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Mobile Brand Filter")
    @Description("1. Navigate to mobile home page\n2. Search 'Laptop'\n3. Filter by brand 'Apple'\n4. Verify first products contain brand name")
    public void testFilterByBrand() {
        String searchTerm = "Laptop";
        String targetBrand = "Apple";

        // 1. Mobile Home Page & Search Action
        MobileHomePage homePage = new MobileHomePage(getDriver());
        homePage.handleCookies();
        MobileSearchResultPage searchResultPage = homePage.search(searchTerm);

        // 2. Filter by Brand Action
        searchResultPage.filterByBrand(targetBrand);

        // 3. Verification of Filtered Product Titles
        List<String> productTitles = searchResultPage.getTopProductTitles(5);
        Assert.assertFalse(productTitles.isEmpty(), "Filtered mobile product list should not be empty");

        boolean containsBrand = productTitles.stream()
            .anyMatch(title -> title.toLowerCase().contains(targetBrand.toLowerCase()) || title.toLowerCase().contains("macbook"));

        Assert.assertTrue(containsBrand,
            "At least one product in filtered mobile results should contain the brand: " + targetBrand + "! Titles found: " + productTitles);
    }

    @Test(description = "Verify that products can be sorted by lowest price on mobile")
    @Severity(SeverityLevel.NORMAL)
    @Story("Mobile Price Sorting")
    @Description("1. Search 'Kulaklık'\n2. Sort by lowest price\n3. Verify that first item price is less than or equal to second item")
    public void testSortByLowestPrice() {
        // 1. Mobile Home Page & Search Action
        MobileHomePage homePage = new MobileHomePage(getDriver());
        homePage.handleCookies();
        MobileSearchResultPage searchResultPage = homePage.search("Kulaklık");

        // 2. Sort by Lowest Price Action
        searchResultPage.sortByLowestPrice();

        // 3. Verification of Price Order (Ascending)
        List<Double> prices = searchResultPage.getProductPricesList(5);
        if (prices.size() >= 2) {
            Assert.assertTrue(
                prices.get(0) <= prices.get(1),
                "Mobile prices are not in ascending order! First Price: " + prices.get(0) + ", Second Price: " + prices.get(1)
            );
        }
    }

    @Test(description = "Verify that products can be filtered within a price range on mobile")
    @Severity(SeverityLevel.NORMAL)
    @Story("Mobile Price Range Filter")
    @Description("1. Search 'Monitor'\n2. Apply price filter (3000 TL - 7000 TL)\n3. Verify products are within price range")
    public void testFilterByPriceRange() {
        double minPrice = 3000.0;
        double maxPrice = 7000.0;

        // 1. Mobile Home Page & Search Action
        MobileHomePage homePage = new MobileHomePage(getDriver());
        homePage.handleCookies();
        MobileSearchResultPage searchResultPage = homePage.search("Monitör");

        // 2. Apply Price Range Filter Action
        searchResultPage.filterByPriceRange(minPrice, maxPrice);

        // 3. Verification of Product Prices Within Range
        List<Double> prices = searchResultPage.getProductPricesList(3);
        for (double price : prices) {
            Assert.assertTrue(price >= minPrice && price <= maxPrice,
                "Mobile price " + price + " is not in range [" + minPrice + " - " + maxPrice + "]");
        }
    }
}
