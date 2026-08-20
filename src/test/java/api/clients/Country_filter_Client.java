package api.clients;
import api.base.BaseAPITest;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class Country_filter_Client {

    public Response getCountryFilter(String countryCode) {
     


   return     given()
                        .log()
                        .all()
                        .header("Authorization", BaseAPITest.getToken())
                        .header("Content-Type", "application/json")
                        .header("country-code", countryCode).
                        queryParam("page", "all")
                        .when()
                        .get("/data/stores")
                        ;
    }



    
}