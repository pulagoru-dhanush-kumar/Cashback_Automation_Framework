package api.clients;

import static io.restassured.RestAssured.given;

import api.base.BaseAPITest;
import io.restassured.response.Response;

public class CashbackApiClient {

    public Response getUserCashback(int page, int perPage) {
        return given()
                .header("Authorization", "Bearer " + BaseAPITest.getToken())
                .header("country-code", "IN")
                .header("locale", "en")
                .queryParam("uuid", BaseAPITest.CLID)
                .queryParam("page", page)
                .queryParam("perPage", perPage)
                .when()
                .get("/pull/user/cashback");
    }
}