package com.hepsiburada.qa.api;

import java.util.List;

import org.testng.annotations.Test;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

@Epic("REST API Automation")
@Feature("Product Catalog Endpoints")
public class ProductApiTests extends BaseApiTest {

    @Test(description = "Verify list all products returns catalog items")
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /api/products returns HTTP 200 with complete product list")
    public void testGetAllProducts() {
        Response response = given()
            .spec(requestSpec)
        .when()
            .get("/api/products")
        .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("total", greaterThan(0))
            .extract()
            .response();

        List<?> products = response.jsonPath().getList("data");
        assertThat(products).isNotEmpty();
    }

    @Test(description = "Verify search products by keyword")
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /api/products?search=iPhone filters products containing keyword")
    public void testSearchProductsByKeyword() {
        given()
            .spec(requestSpec)
            .queryParam("search", "iPhone")
        .when()
            .get("/api/products")
        .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("total", greaterThan(0))
            .body("data[0].name", org.hamcrest.Matchers.containsString("iPhone"));
    }

    @Test(description = "Verify filter products by category")
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /api/products?category=Telefon filters products in phone category")
    public void testFilterProductsByCategory() {
        Response response = given()
            .spec(requestSpec)
            .queryParam("category", "Telefon")
        .when()
            .get("/api/products")
        .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .extract()
            .response();

        List<String> categories = response.jsonPath().getList("data.category");
        assertThat(categories).allMatch(cat -> cat.equalsIgnoreCase("Telefon"));
    }

    @Test(description = "Verify get product detail by existing ID")
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /api/products/1 returns detailed info for iPhone 15 Pro")
    public void testGetProductByIdSuccess() {
        given()
            .spec(requestSpec)
        .when()
            .get("/api/products/1")
        .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("data.id", equalTo(1))
            .body("data.name", equalTo("Apple iPhone 15 Pro 128 GB"));
    }

    @Test(description = "Verify get product detail by non-existing ID returns 404")
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /api/products/9999 returns HTTP 404 Not Found")
    public void testGetProductByIdNotFound() {
        given()
            .spec(requestSpec)
        .when()
            .get("/api/products/9999")
        .then()
            .statusCode(404)
            .body("success", equalTo(false));
    }
}
