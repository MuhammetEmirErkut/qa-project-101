package com.hepsiburada.qa.pages.search;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.hepsiburada.qa.pages.base.BasePage;
import com.hepsiburada.qa.pages.pdp.ProductDetailPage;
import io.qameta.allure.Step;

public class SearchResultPage extends BasePage {

    public SearchResultPage(WebDriver driver) {
        super(driver);
    }

    private final By resultItems = By.cssSelector(
        "article[data-test-id='product-card'], li[id^='i'], div[data-test-id='product-card-container'], " +
        "[data-test-id='product-card-name'], li[class*='productListContent'], ul[class*='productListContent'] > li, " +
        "div[class*='product-list-item'], [data-test-id='product-card-item']"
    );
    private final By firstProductLink = By.cssSelector(
        "article[data-test-id='product-card'] a, li[id^='i'] a, [data-test-id='product-card-container'] a, " +
        "a[data-test-id='product-card-name'], a[data-test-id='product-card-link'], li[class*='productListContent'] a, " +
        "ul[class*='productListContent'] > li a, div[class*='product-list-item'] a"
    );
    private final By resultCountLabel = By.cssSelector(
        "[data-test-id='search-result-count'], div[class*='search-result-summary'], div[class*='totalLeafCount'], " +
        "div[class*='filter-summary'] b, div[class*='search-summary'], span[class*='search-result-count']"
    );
    private final By firstProductTitle = By.cssSelector(
        "[data-test-id='product-card-name'], h3[data-test-id='product-card-name'], h2[data-test-id='product-card-name'], " +
        "article[data-test-id='product-card'] h3, article[data-test-id='product-card'] h2, article[data-test-id='product-card'] a[title], " +
        "li[id^='i'] h3, li[id^='i'] h2, li[id^='i'] a[title], h3[type='comfort'], h3[class*='product-title'], " +
        "div[data-test-id='product-card-container'] h3, [data-test-id='product-title'], a[data-test-id='product-card-link']"
    );
    private final By noResultsContainer = By.cssSelector("div[class*='noResult'], div[class*='not-found'], [data-test-id='no-search-result'], div[class*='empty-search']");
    private final By noResultsMessage = By.xpath("//*[contains(text(), 'bulunamadı') or contains(text(), 'sonuç bulunamadı') or contains(text(), 'arama kriterlerine uygun')]");
   
    private final By sortDropdownTrigger = By.cssSelector(
        "[data-test-id='sort-select'], div[class*='sort_'], div[class*='sorting'], div[class*='horizontalSortingBar'] select, div[class*='sort']"
    );

    private final By sortLowestPriceOption = By.xpath(
        "//*[contains(text(), 'En Düşük Fiyat') or contains(text(), 'Fiyata göre artan') or contains(text(), 'En düşük fiyat')]"
    );

    private final By minPriceInput = By.cssSelector(
        "input[placeholder*='En az' i], input[placeholder*='Min' i], input[name*='minPrice'], input[data-test-id*='price-min']"
    );

    private final By maxPriceInput = By.cssSelector(
        "input[placeholder*='En çok' i], input[placeholder*='Max' i], input[name*='maxPrice'], input[data-test-id*='price-max']"
    );

    private final By applyPriceFilterButton = By.xpath(
        "//button[contains(., 'Uygula') or contains(., 'Ara') or contains(@class, 'price-filter-btn') or contains(@class, 'range')] | //button[@type='submit' and contains(@class, 'price')]"
    );

    private final By productPrices = By.cssSelector(
        "article[data-test-id='product-card'] [data-test-id='price-current-price'], " +
        "article[data-test-id='product-card'] [data-test-id*='price'], " +
        "li[id^='i'] [data-test-id*='price'], " +
        "div[data-test-id='product-card-container'] [data-test-id*='price'], " +
        "[data-test-id='price-current-price'], " +
        "[data-test-id='product-price']"
    );

    private final By allProductTitles = By.cssSelector(
        "[data-test-id='product-card-name'], h3[data-test-id='product-card-name'], h2[data-test-id='product-card-name'], " +
        "article[data-test-id='product-card'] h3, article[data-test-id='product-card'] h2, article[data-test-id='product-card'] a[title], " +
        "li[id^='i'] h3, li[id^='i'] h2, li[id^='i'] a[title], [data-test-id='product-title'], " +
        "h3[class*='product-title'], h3[type='comfort'], h3[class*='title'], h2[class*='title'], " +
        "div[data-test-id='product-card-container'] h3, a[data-test-id='product-card-link']"
    );

    @Step("Click the product in Search Result Page and switch to detail page")
    public ProductDetailPage clickFirstProduct() {
        waitForProductsReload();
        try {
            wait.until(ExpectedConditions.elementToBeClickable(firstProductLink));
            click(firstProductLink);
        } catch (Exception e) {
            try {
                WebElement item = wait.until(ExpectedConditions.elementToBeClickable(resultItems));
                jsClick(item);
            } catch (Exception ex) {
                List<WebElement> links = driver.findElements(firstProductLink);
                if (!links.isEmpty()) {
                    jsClick(links.get(0));
                }
            }
        }
        switchToNewTab();
        return new ProductDetailPage(driver);
    }

    @Step("Get product count from search result page")
    public int getResultCount() {
        try {
            wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(resultItems),
                ExpectedConditions.visibilityOfElementLocated(resultCountLabel),
                ExpectedConditions.presenceOfElementLocated(allProductTitles)
            ));
        } catch (Exception ignored) {
        }

        try {
            if (isDisplayed(resultCountLabel, 3)) {
                String text = getText(resultCountLabel).replaceAll("[^0-9]", "");
                if (!text.isEmpty()) {
                    return Integer.parseInt(text);
                }
            }
            List<WebElement> items = driver.findElements(resultItems);
            if (!items.isEmpty()) {
                return items.size();
            }
            List<WebElement> titleElements = driver.findElements(allProductTitles);
            return titleElements.size();
        } catch (Exception e) {
            return driver.findElements(resultItems).size();
        }
    }

    @Step("Get first title from search result page")
    public String getFirstTitle() {
        // 1. First attempt via getTopProductTitles
        try {
            List<String> titles = getTopProductTitles(1);
            if (!titles.isEmpty() && !titles.get(0).isEmpty()) {
                return titles.get(0);
            }
        } catch (Exception ignored) {
        }

        // 2. Wait explicitly for non-empty title element
        try {
            WebDriverWait titleWait = new WebDriverWait(driver, Duration.ofSeconds(10));
            String title = titleWait.until(d -> {
                List<WebElement> elements = d.findElements(firstProductTitle);
                for (WebElement el : elements) {
                    try {
                        String text = el.getText().trim();
                        if (!text.isEmpty() && !text.equalsIgnoreCase("reklam") && !text.equalsIgnoreCase("sponsorlu")) {
                            return text;
                        }
                        String titleAttr = el.getAttribute("title");
                        if (titleAttr != null && !titleAttr.trim().isEmpty() && !titleAttr.equalsIgnoreCase("reklam")) {
                            return titleAttr.trim();
                        }
                    } catch (Exception ignored) {
                    }
                }
                return null;
            });
            if (title != null && !title.isEmpty()) {
                return title;
            }
        } catch (Exception ignored) {
        }

        // 3. Fallback: check all product cards
        try {
            List<WebElement> cards = driver.findElements(resultItems);
            for (WebElement card : cards) {
                try {
                    String cardText = card.getText().trim();
                    if (!cardText.isEmpty()) {
                        String[] lines = cardText.split("\\r?\\n");
                        for (String line : lines) {
                            String trimmed = line.trim();
                            if (trimmed.length() > 3 && !trimmed.matches("^[0-9.,]+.*") && !trimmed.equalsIgnoreCase("reklam") && !trimmed.equalsIgnoreCase("sponsorlu")) {
                                return trimmed;
                            }
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }

        return "";
    }

    @Step("Check if no results message is displayed")
    public boolean isNoResultsMessageDisplayed() {
        try {
            return isDisplayed(noResultsMessage, 5) || isDisplayed(noResultsContainer, 5);
        } catch (Exception e) {
            return false;
        }
    }

    @Deprecated
    public boolean isNoResultsManageDisplayed() {
        return isNoResultsMessageDisplayed();
    }
    
    @Step("Get no results message text from search result page")
    public String getNoResultsMessageText(){
        try {
            if(isDisplayed(noResultsMessage)){
                return getText(noResultsMessage);
            }else{
                return "";
            }
        } catch (Exception e) {
            return "";
        }
    }

    @Step("Filter products by brand: {brandName}")
    public SearchResultPage filterByBrand(String brandName) {
        String currentUrl = driver.getCurrentUrl();
        String cleanUrl = currentUrl.replaceAll("([&?])filtreler=[^&]*", "");
        String separator = cleanUrl.contains("?") ? "&" : "?";
        driver.get(cleanUrl + separator + "filtreler=markalar:" + brandName.toLowerCase());
        waitForProductsReload();
        return this;
    }

    @Step("Sort products by lowest price")
    public SearchResultPage sortByLowestPrice() {
        String currentUrl = driver.getCurrentUrl();
        String cleanUrl = currentUrl.replaceAll("([&?])siralama=[^&]*", "");
        String separator = cleanUrl.contains("?") ? "&" : "?";
        driver.get(cleanUrl + separator + "siralama=artanfiyat");
        waitForProductsReload();
        return this;
    }

    @Step("Filter products by price range: {minPrice} - {maxPrice}")
    public SearchResultPage filterByPriceRange(String minPrice, String maxPrice) {
        int min = (int) Double.parseDouble(minPrice);
        int max = (int) Double.parseDouble(maxPrice);

        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));
            WebElement minEl = shortWait.until(ExpectedConditions.visibilityOfElementLocated(minPriceInput));
            minEl.clear();
            minEl.sendKeys(String.valueOf(min));

            WebElement maxEl = shortWait.until(ExpectedConditions.visibilityOfElementLocated(maxPriceInput));
            maxEl.clear();
            maxEl.sendKeys(String.valueOf(max));

            try {
                WebElement applyBtn = shortWait.until(ExpectedConditions.elementToBeClickable(applyPriceFilterButton));
                jsClick(applyBtn);
            } catch (Exception e) {
                maxEl.sendKeys(Keys.ENTER);
            }
        } catch (Exception ignored) {
        }

        String currentUrl = driver.getCurrentUrl();
        if (!currentUrl.contains("fiyat:")) {
            String cleanUrl = currentUrl.replaceAll("([&?])filtreler=[^&]*", "");
            String separator = cleanUrl.contains("?") ? "&" : "?";
            driver.get(cleanUrl + separator + "filtreler=fiyat:" + min + "-" + max);
        }

        waitForProductsReload();
        return this;
    }

    public SearchResultPage filterByPriceRange(double minPrice, double maxPrice) {
        return filterByPriceRange(String.valueOf((int) minPrice), String.valueOf((int) maxPrice));
    }

    @Step("Get top {limit} product titles")
    public List<String> getTopProductTitles(int limit) {
        List<String> titles = new ArrayList<>();
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.presenceOfElementLocated(allProductTitles));

            List<WebElement> elements = driver.findElements(allProductTitles);
            for (WebElement el : elements) {
                try {
                    String title = el.getText().trim();
                    if (title.isEmpty()) {
                        title = el.getAttribute("title");
                    }
                    if (title != null && !title.isEmpty() && !title.equalsIgnoreCase("reklam") && !title.equalsIgnoreCase("sponsorlu")) {
                        titles.add(title);
                        if (titles.size() >= limit) {
                            break;
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }
        return titles;
    }

    @Step("Get top {limit} product prices")
    public List<Double> getTopProductPrices(int limit) {
        List<Double> prices = new ArrayList<>();
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.presenceOfElementLocated(productPrices));

            List<WebElement> elements = driver.findElements(productPrices);
            for (WebElement el : elements) {
                try {
                    String priceText = el.getText().trim();
                    if (priceText.isEmpty()) {
                        continue;
                    }
                    String firstLine = priceText.split("\\r?\\n")[0];
                    java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("([0-9]+(?:\\.[0-9]{3})*(?:,[0-9]{1,2})?)").matcher(firstLine);
                    if (matcher.find()) {
                        String numStr = matcher.group(1).replace(".", "").replace(",", ".");
                        double val = Double.parseDouble(numStr);
                        if (val > 0) {
                            prices.add(val);
                            if (prices.size() >= limit) {
                                break;
                            }
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }
        return prices;
    }

    public List<Double> getProductPricesList(int limit) {
        return getTopProductPrices(limit);
    }

    private void waitForProductsReload(){
        try {
            waitForPageToLoad();
            wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(allProductTitles),
                ExpectedConditions.presenceOfElementLocated(resultItems),
                ExpectedConditions.presenceOfElementLocated(noResultsMessage)
            ));
        } catch (Exception ignored) {
        }
    }
}
