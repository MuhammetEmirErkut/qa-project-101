package com.hepsiburada.qa.steps;

import java.util.List;
import org.testng.Assert;
import com.thoughtworks.gauge.Step;

public class FilterAndSortSteps extends BaseStep {

    @Step("Apply brand filter <brandName>")
    public void applyBrandFilter(String brandName) {
        searchResultPage().filterByBrand(brandName);
    }

    @Step("Verify top <limit> product titles contain <keyword1> or <keyword2>")
    public void verifyTopProductTitlesContain(int limit, String keyword1, String keyword2) {
        List<String> titles = searchResultPage().getTopProductTitles(limit);
        Assert.assertFalse(titles.isEmpty(), "Filtered product list is empty!");
        long matchCount = titles.stream().filter(title -> 
            title.toLowerCase().contains(keyword1.toLowerCase()) || 
            title.toLowerCase().contains(keyword2.toLowerCase())
        ).count();
        Assert.assertTrue(matchCount > 0, "No product titles matched expected brand: " + keyword1 + " / " + keyword2 + ". Titles found: " + titles);
    }

    @Step("Sort products by lowest price")
    public void sortProductsByLowestPrice() {
        searchResultPage().sortByLowestPrice();
    }

    @Step("Verify top <limit> product prices are in ascending order")
    public void verifyPricesAscending(int limit) {
        List<Double> prices = searchResultPage().getProductPricesList(limit);
        Assert.assertTrue(prices.size() >= 2, "At least 2 prices needed to verify ascending order!");
        for (int i = 0; i < prices.size() - 1; i++) {
            Assert.assertTrue(prices.get(i) <= prices.get(i + 1),
                "Prices not in ascending order: " + prices.get(i) + " > " + prices.get(i + 1));
        }
    }

    @Step("Filter products with price range <minPrice> to <maxPrice>")
    public void filterPriceRange(String minPrice, String maxPrice) {
        searchResultPage().filterByPriceRange(minPrice, maxPrice);
    }

    @Step("Verify top <limit> product prices are between <minPrice> and <maxPrice>")
    public void verifyPriceRange(int limit, double minPrice, double maxPrice) {
        List<Double> prices = searchResultPage().getProductPricesList(limit);
        Assert.assertFalse(prices.isEmpty(), "Price list is empty!");
        for (Double price : prices) {
            Assert.assertTrue(price >= minPrice && price <= maxPrice,
                "Price out of range: " + price + " (Expected: " + minPrice + " - " + maxPrice + ")");
        }
    }
}
