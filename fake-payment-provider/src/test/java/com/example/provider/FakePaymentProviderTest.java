package com.example.provider;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

@QuarkusTest
class FakePaymentProviderTest {
    private static final String PAYMENT = """
            {"orderId":"order-123","amount":10.50,"currency":"EUR"}
            """;

    @BeforeEach void reset() { given().post("/admin/reset").then().statusCode(200); }

    @Test void normalModeSucceeds() {
        given().contentType(ContentType.JSON).body(PAYMENT).post("/payments").then().statusCode(200).body("status", equalTo("APPROVED"));
    }

    @Test void alwaysFailModeReturnsServiceUnavailable() {
        given().put("/admin/mode/ALWAYS_FAIL").then().statusCode(200);
        given().contentType(ContentType.JSON).body(PAYMENT).post("/payments").then().statusCode(503);
    }

    @Test void failPercentageCanBeConfiguredAtRuntime() {
        given().put("/admin/failure-percentage/100").then().statusCode(200);
        given().put("/admin/mode/FAIL_PERCENTAGE").then().statusCode(200);
        given().contentType(ContentType.JSON).body(PAYMENT).post("/payments").then().statusCode(503);
    }

    @Test void slowModeUsesConfiguredDelay() {
        given().put("/admin/slow-delay/50").then().statusCode(200);
        given().put("/admin/mode/SLOW").then().statusCode(200);
        given().contentType(ContentType.JSON).body(PAYMENT).post("/payments").then().statusCode(200);
        given().get("/admin/stats").then().statusCode(200).body("receivedCalls", greaterThanOrEqualTo(1));
    }
}
