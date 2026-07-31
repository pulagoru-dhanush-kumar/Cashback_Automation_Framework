package api.tests;

import api.base.BaseAPITest;
import api.clients.EarningsClient;
import api.models.Cashback;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import api.utils.EarningsDataStore;

public class EarningsApiTest extends BaseAPITest {
    private Cashback apicashback;

    @Test(groups = "api")
    public void verifyUserEarningsAPI() {
        EarningsClient client = new EarningsClient();

        Response response = client.getUserEarnings(CLID);
        System.out.println(response.asString());

        Assert.assertEquals(response.statusCode(), 200);

        Cashback cashback = new Cashback();
        cashback.setPending(response.jsonPath().getDouble("data.pending"));
        cashback.setConfirmed(response.jsonPath().getDouble("data.confirmed"));
        cashback.setDeclined(response.jsonPath().getDouble("data.declined"));

        apicashback = cashback;
        EarningsDataStore.apiCashback = cashback;

        System.out.println(cashback);
        Assert.assertNotNull(cashback);
    }

    @Test(groups = "compare", dependsOnGroups = {"api", "ui"}, alwaysRun = true)
    public void compare_UI_and_API_Earnings() {

        // If API data wasn't captured (e.g. this class ran alone), fetch it now
        if (EarningsDataStore.apiCashback == null) {
            System.out.println("API cashback not found — running verifyUserEarningsAPI() now");
            verifyUserEarningsAPI();
        }

        Assert.assertNotNull(EarningsDataStore.apiCashback,
                "API earnings could not be captured");

        Assert.assertNotNull(EarningsDataStore.uiPendingPayments,
                "UI pending payments not captured — TotalEarningsTests must run in this suite");

        Assert.assertNotNull(EarningsDataStore.uiConfirmedEarnings,
                "UI confirmed earnings not captured — TotalEarningsTests must run in this suite");

        Assert.assertNotNull(EarningsDataStore.uiDeclinedAmount,
                "UI declined amount not captured — TotalEarningsTests must run in this suite");

        double apiPending   = EarningsDataStore.apiCashback.getPending();
        double apiConfirmed = EarningsDataStore.apiCashback.getConfirmed();
        double apiDeclined  = EarningsDataStore.apiCashback.getDeclined();

        double uiPending   = EarningsDataStore.uiPendingPayments;
        double uiConfirmed = EarningsDataStore.uiConfirmedEarnings;
        double uiDeclined  = EarningsDataStore.uiDeclinedAmount;

        System.out.println("API Pending   = " + apiPending   + " | UI Pending   = " + uiPending);
        System.out.println("API Confirmed = " + apiConfirmed + " | UI Confirmed = " + uiConfirmed);
        System.out.println("API Declined  = " + apiDeclined  + " | UI Declined  = " + uiDeclined);

        Assert.assertEquals(apiPending, uiPending, 0.01,
                "Pending amount mismatch between API and UI");
        Assert.assertEquals(apiConfirmed, uiConfirmed, 0.01,
                "Confirmed amount mismatch between API and UI");
        Assert.assertEquals(apiDeclined, uiDeclined, 0.01,
                "Declined amount mismatch between API and UI");
    }
}
 

