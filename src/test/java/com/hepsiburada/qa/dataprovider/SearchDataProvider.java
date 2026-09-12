package com.hepsiburada.qa.dataprovider;

import org.testng.annotations.DataProvider;

public class SearchDataProvider {
    @DataProvider(name = "searchData")
    public static Object[][] getSearchData() {
        return new Object[][] {
            {"Laptop"},
            {"iPhone"},
            {"Headphones"}
        };
    }
}
