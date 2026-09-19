package com.hepsiburada.qa.api;

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
import static org.hamcrest.Matchers.notNullValue;

@Epic("REST API Automation")
@Feature("Authentication Endpoints")
public class AuthApiTests extends BaseApiTest {

    @Test(description = "Verify health check endpoint returns UP status")
    @Severity(SeverityLevel.BLOCKER)
    @Description("GET /api/health should return HTTP 200 and status UP")
    public void testHealthCheck() {
        given()
            .spec(requestSpec)
        .when()
            .get("/api/health")
        .then()
            .statusCode(200)
            .body("status", equalTo("UP"))
            .body("service", equalTo("qa-mock-ecommerce-api"));
    }

    @Test(description = "Verify successful authentication with valid credentials")
    @Severity(SeverityLevel.CRITICAL)
    @Description("POST /api/auth/login with valid tester credentials returns JWT token")
    public void testValidLogin() {
        String payload = "{\"username\":\"qa_tester\",\"password\":\"Pass1234!\"}";

        Response response = given()
            .spec(requestSpec)
            .body(payload)
        .when()
            .post("/api/auth/login")
        .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("token", notNullValue())
            .body("user.username", equalTo("qa_tester"))
            .extract()
            .response();

        String token = response.jsonPath().getString("token");
        assertThat(token).startsWith("mock-valid-jwt-token-qa-101");
    }

    @Test(description = "Verify authentication failure with invalid credentials")
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /api/auth/login with invalid password returns HTTP 401")
    public void testInvalidLogin() {
        String payload = "{\"username\":\"qa_tester\",\"password\":\"WrongPassword!\"}";

        given()
            .spec(requestSpec)
            .body(payload)
        .when()
            .post("/api/auth/login")
        .then()
            .statusCode(401)
            .body("success", equalTo(false))
            .body("error", equalTo("Geçersiz kullanıcı adı veya şifre."));
    }

    @Test(description = "Verify bad request on missing login credentials")
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /api/auth/login with empty body returns HTTP 400")
    public void testMissingCredentials() {
        String payload = "{}";

        given()
            .spec(requestSpec)
            .body(payload)
        .when()
            .post("/api/auth/login")
        .then()
            .statusCode(400)
            .body("success", equalTo(false));
    }
}
