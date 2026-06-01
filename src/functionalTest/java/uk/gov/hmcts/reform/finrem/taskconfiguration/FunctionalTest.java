package uk.gov.hmcts.reform.finrem.taskconfiguration;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.HttpStatus.OK;

class FunctionalTest {

    private String testUrl = System.getenv().getOrDefault("TEST_URL", "http://localhost:4558");

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = testUrl;
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    void healthCheck() {
        given()
            .get("/health")
            .then()
            .statusCode(OK.value())
            .body("status", equalTo("UP"));
    }
}
