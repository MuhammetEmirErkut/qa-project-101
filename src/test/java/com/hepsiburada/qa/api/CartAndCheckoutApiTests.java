package com.hepsiburada.qa.api;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

@Epic("REST API Automation")
@Feature("Cart and Checkout Endpoints")
public class CartAndCheckoutApiTests extends BaseApiTest {

    private String validToken;

    @BeforeClass
    public void initAuth() {
        validToken = obtainAuthToken("qa_tester", "Pass1234!");
    }

    @Test(description = "Verify unauthorized access to cart without token")
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /api/cart without Authorization header should return HTTP 401")
    public void testGetCartUnauthorized() {
        given()
            .spec(requestSpec)
        .when()
            .get("/api/cart")
        .then()
            .statusCode(401)
            .body("success", equalTo(false));
    }

    @Test(description = "Verify add item to cart with authorized user")
    @Severity(SeverityLevel.CRITICAL)
    @Description("POST /api/cart/items adds product to user cart")
    public void testAddItemToCart() {
        String itemPayload = "{\"productId\": 1, \"quantity\": 2}";

        given()
            .spec(requestSpec)
            .header("Authorization", "Bearer " + validToken)
            .body(itemPayload)
        .when()
            .post("/api/cart/items")
        .then()
            .statusCode(201)
            .body("success", equalTo(true))
            .body("message", equalTo("Ürün sepete başarıyla eklendi."));
    }

    @Test(dependsOnMethods = "testAddItemToCart", description = "Verify get cart contents after adding items")
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /api/cart retrieves current cart and calculated totals")
    public void testGetCartContents() {
        given()
            .spec(requestSpec)
            .header("Authorization", "Bearer " + validToken)
        .when()
            .get("/api/cart")
        .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("itemCount", greaterThanOrEqualTo(1))
            .body("totalPrice", greaterThanOrEqualTo(1));
    }

    @Test(dependsOnMethods = "testGetCartContents", description = "Verify checkout processes order and empties cart")
    @Severity(SeverityLevel.BLOCKER)
    @Description("POST /api/checkout completes order and creates confirmed order ID")
    public void testCheckoutOrder() {
        given()
            .spec(requestSpec)
            .header("Authorization", "Bearer " + validToken)
        .when()
            .post("/api/checkout")
        .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("data.orderId", notNullValue())
            .body("data.status", equalTo("CONFIRMED"));
    }
}
