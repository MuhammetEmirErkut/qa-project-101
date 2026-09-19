package com.hepsiburada.qa.dataprovider;

import java.util.List;
import java.util.Map;

import org.testng.annotations.DataProvider;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hepsiburada.qa.utils.JsonDataReader;

public class SearchDataProvider {

    @DataProvider(name = "searchData")
    public static Object[][] getSearchData() {
        try {
            List<Map<String, Object>> data = JsonDataReader.readList(
                "testdata/search_terms.json",
                new TypeReference<List<Map<String, Object>>>() {}
            );

            Object[][] result = new Object[data.size()][1];
            for (int i = 0; i < data.size(); i++) {
                result[i][0] = data.get(i).get("searchTerm").toString();
            }
            return result;
        } catch (Exception e) {
            // Fallback default test data in case resource path is altered
            return new Object[][] {
                {"Laptop"},
                {"iPhone"},
                {"Kulaklık"}
            };
        }
    }
}
