package com.hepsiburada.qa.api;

import org.testng.annotations.BeforeClass;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

public abstract class BaseApiTest {

    protected RequestSpecification requestSpec;
    protected String authToken;

    @BeforeClass
    public void setUpApi() {
        String baseUrl = System.getProperty("apiBaseUrl", "http://localhost:3001");
        RestAssured.baseURI = baseUrl;

        requestSpec = new RequestSpecBuilder()
            .setContentType(ContentType.JSON)
            .setAccept(ContentType.JSON)
            .addFilter(new AllureRestAssured())
            .build();
    }

    protected String obtainAuthToken(String username, String password) {
        String body = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);

        Response response = given()
            .spec(requestSpec)
            .body(body)
            .when()
            .post("/api/auth/login")
            .then()
            .statusCode(200)
            .extract()
            .response();

        return response.jsonPath().getString("token");
    }
}
