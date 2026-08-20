package api.tests;

import java.math.BigDecimal;
import java.util.Locale;

import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Test;

import api.base.BaseAPITest;
import api.clients.CashbackApiClient;
import api.models.Cashback;
import api.models.CashbackResponse;
import api.models.CashbackResponse.CashbackTransaction;
import api.models.CashbackResponse.PaginationData;
import io.restassured.response.Response;

public class CashbackApiTest extends BaseAPITest {

    private static final int PER_PAGE = 100;
    // This summary is available to your Earnings API test.
    public static Cashback cashback = new Cashback();

    private final CashbackApiClient cashbackApiClient = new CashbackApiClient();

    @Test
    public void verifyCashbackHistoryAndCalculateSummary() {

   
        cashback = new Cashback();

        int page = 1;
        int collectedTransactions = 0;
        Integer expectedTotal = null;

        while (true) {
            Response response = cashbackApiClient.getUserCashback(page, PER_PAGE);

            Assert.assertEquals(response.getStatusCode(), 200,
                    "Cashback API must return HTTP status 200.");

            CashbackResponse cashbackResponse = response.as(CashbackResponse.class);

            Assert.assertEquals(cashbackResponse.getSuccess(), Integer.valueOf(1),
                    "Response success must be 1.");
            Assert.assertEquals(cashbackResponse.getError(), Integer.valueOf(0),
                    "Response error must be 0.");
            Assert.assertNull(cashbackResponse.getMsg(),
                    "Response msg must be null.");

            PaginationData pagination = cashbackResponse.getData();

            Assert.assertNotNull(pagination, "Response data must not be null.");
            Assert.assertNotNull(pagination.getData(),
                    "Cashback transaction list must not be null.");

            Assert.assertEquals(pagination.getCurrentPage(), Integer.valueOf(page),
                    "current_page must match requested page.");

            Assert.assertEquals(pagination.getPerPage(), Integer.valueOf(PER_PAGE),
                    "per_page must match requested perPage.");

            Assert.assertNotNull(pagination.getTotal(), "total must not be null.");
            Assert.assertTrue(pagination.getTotal() >= 0,
                    "total must be greater than or equal to zero.");

            if (expectedTotal == null) {
                expectedTotal = pagination.getTotal();
            } else {
                Assert.assertEquals(pagination.getTotal(), expectedTotal,
                        "total must remain consistent across pages.");
            }

            for (CashbackTransaction transaction : pagination.getData()) {
                validateTransaction(transaction);
                addCashbackToSummary(transaction);
                collectedTransactions++;
            }

            if (pagination.getNextPageUrl() == null) {
                break;
            }

            page++;
        }

        Assert.assertEquals(collectedTransactions, expectedTotal.intValue(),
                "Collected cashback transactions must equal API total.");

        Assert.assertTrue(cashback.getPending() >= 0,
                "Total pending cashback must be greater than or equal to zero.");

        Assert.assertTrue(cashback.getConfirmed() >= 0,
                "Total confirmed cashback must be greater than or equal to zero.");

        Assert.assertTrue(cashback.getDeclined() >= 0,
                "Total declined cashback must be greater than or equal to zero.");

        Reporter.log(
                "Cashback Summary"
                        + " | Pending: " + cashback.getPending()
                        + " | Confirmed: " + cashback.getConfirmed()
                        + " | Declined: " + cashback.getDeclined(),
                true
        );
    }

    private void validateTransaction(CashbackTransaction transaction) {
        Assert.assertNotNull(transaction.getId(), "Transaction id must not be null.");
        Assert.assertTrue(transaction.getId() > 0,
                "Transaction id must be greater than zero.");

        Assert.assertNotNull(transaction.getUserId(), "user_id must not be null.");

        Assert.assertNotNull(transaction.getStoreId(), "store_id must not be null.");
        Assert.assertTrue(transaction.getStoreId() > 0,
                "store_id must be greater than zero.");

        // Zero order amount is valid.
        Assert.assertNotNull(transaction.getOrderAmount(),
                "order_amount must not be null.");
        Assert.assertTrue(transaction.getOrderAmount().compareTo(BigDecimal.ZERO) >= 0,
                "order_amount must be greater than or equal to zero. Transaction ID: "
                        + transaction.getId());

        // Zero cashback is valid.
        Assert.assertNotNull(transaction.getCashback(),
                "cashback must not be null.");
        Assert.assertTrue(transaction.getCashback().compareTo(BigDecimal.ZERO) >= 0,
                "cashback must be greater than or equal to zero. Transaction ID: "
                        + transaction.getId());

        Assert.assertNotNull(transaction.getStatus(), "status must not be null.");

        Assert.assertTrue(
                transaction.getStatus().equalsIgnoreCase("pending")
                        || transaction.getStatus().equalsIgnoreCase("confirmed")
                        || transaction.getStatus().equalsIgnoreCase("declined"),
                "Unexpected cashback status: " + transaction.getStatus()
                        + ". Transaction ID: " + transaction.getId()
        );
    }

    private void addCashbackToSummary(CashbackTransaction transaction) {
        double commission = transaction.getCashback().doubleValue();
        String status = transaction.getStatus().toLowerCase(Locale.ROOT);

        switch (status) {
            case "pending":
                cashback.setPending(cashback.getPending() + commission);
                break;

            case "confirmed":
                cashback.setConfirmed(cashback.getConfirmed() + commission);
                break;

            case "declined":
                cashback.setDeclined(cashback.getDeclined() + commission);
                break;

            default:
                Assert.fail("Unsupported cashback status: " + transaction.getStatus());
        }        
    }  
      
}

