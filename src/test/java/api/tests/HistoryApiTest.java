package api.tests;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.HashSet;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

import api.base.BaseAPITest;
import api.clients.HistoryApiClient;
import api.models.ClicksResponse;
import api.models.ClicksResponse.Click;
import api.models.ClicksResponse.PaginationData;
import api.models.ClicksResponse.Store;
import io.restassured.response.Response;

public class HistoryApiTest extends BaseAPITest {

    private static final int PER_PAGE = 10;

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")
                    .withResolverStyle(ResolverStyle.STRICT);

    private final HistoryApiClient historyApiClient = new HistoryApiClient();

    @Test
    public void verifyCompleteClicksHistory() {
        Set<String> clickCodes = new HashSet<>();

        int page = 1;
        int collectedRecords = 0;
        Integer expectedTotal = null;
        Long expectedUserId = null;

        while (true) {
            Response response = historyApiClient.getUserClicksHistory(page, PER_PAGE);

            Assert.assertEquals(response.getStatusCode(), 200,
                    "Clicks History API should return HTTP status 200.");

            ClicksResponse clicksResponse = response.as(ClicksResponse.class);

            Assert.assertEquals(clicksResponse.getSuccess(), Integer.valueOf(1),
                    "Response success must be 1.");
            Assert.assertEquals(clicksResponse.getError(), Integer.valueOf(0),
                    "Response error must be 0.");
            Assert.assertNull(clicksResponse.getMsg(),
                    "Response msg must be null.");

            PaginationData pagination = clicksResponse.getData();

            Assert.assertNotNull(pagination, "Response data must not be null.");
            Assert.assertNotNull(pagination.getData(), "Click records must not be null.");
            Assert.assertEquals(pagination.getCurrentPage(), Integer.valueOf(page),
                    "current_page must match requested page.");
            Assert.assertEquals(pagination.getPerPage(), Integer.valueOf(PER_PAGE),
                    "per_page must match requested perPage.");
            Assert.assertNotNull(pagination.getTotal(), "total must not be null.");
            Assert.assertTrue(pagination.getTotal() > 0, "total must be greater than zero.");

            int expectedLastPage = (int) Math.ceil(
                    (double) pagination.getTotal() / pagination.getPerPage()
            );

            Assert.assertEquals(pagination.getLastPage(), Integer.valueOf(expectedLastPage),
                    "last_page must equal ceil(total / per_page).");

            if (page == 1) {
                Assert.assertNull(pagination.getPrevPageUrl(),
                        "prev_page_url must be null on the first page.");
            } else {
                Assert.assertNotNull(pagination.getPrevPageUrl(),
                        "prev_page_url must exist after the first page.");
            }

            if (page < pagination.getLastPage()) {
                Assert.assertNotNull(pagination.getNextPageUrl(),
                        "next_page_url must exist if another page is available.");
            } else {
                Assert.assertNull(pagination.getNextPageUrl(),
                        "next_page_url must be null on the last page.");
            }

            if (expectedTotal == null) {
                expectedTotal = pagination.getTotal();
            }

            for (Click click : pagination.getData()) {
                validateClick(click, clickCodes);

                if (expectedUserId == null) {
                    expectedUserId = click.getUserId();
                }

                Assert.assertEquals(click.getUserId(), expectedUserId,
                        "All returned click records must belong to the same user.");

                collectedRecords++;
            }

            if (pagination.getNextPageUrl() == null) {
                break;
            }

            page++;
        }

        Assert.assertEquals(collectedRecords, expectedTotal.intValue(),
                "Total collected click records must equal API total.");
    }

    private void validateClick(Click click, Set<String> clickCodes) {
        Assert.assertNotNull(click.getId(), "Click id must not be null.");
        Assert.assertTrue(click.getId() > 0, "Click id must be greater than zero.");

        Assert.assertNotNull(click.getCode(), "Click code must not be null.");
        Assert.assertFalse(click.getCode().trim().isEmpty(), "Click code must not be empty.");
        Assert.assertTrue(clickCodes.add(click.getCode()),
                "Duplicate click code found: " + click.getCode());

        Assert.assertNotNull(click.getClickTime(), "click_time must not be null.");
        validateDateTime(click.getClickTime());

        Assert.assertNotNull(click.getStoreId(), "store_id must not be null.");
        Assert.assertTrue(click.getStoreId() > 0, "store_id must be greater than zero.");

        Assert.assertNotNull(click.getUserId(), "user_id must not be null.");
        Assert.assertNotNull(click.getNetworkCampaignId(),
                "network_campaign_id must not be null.");

        Assert.assertEquals(click.getCashbackEnabled(), Boolean.TRUE,
                "cashback_enabled must be true.");
        Assert.assertEquals(click.getCashbackType(), "cashback",
                "cashback_type must be cashback.");
        Assert.assertNotNull(click.getCashbackPercent(),
                "cashback_percent must not be null.");

        Assert.assertTrue(click.getCanClaim() == 0 || click.getCanClaim() == 1,
                "can_claim must be either 0 or 1.");

        validateStore(click);
    }

    private void validateStore(Click click) {
        Store store = click.getStore();

        Assert.assertNotNull(store, "Store object must not be null.");
        Assert.assertEquals(store.getId(), click.getStoreId(),
                "store.id must match click.store_id.");

        Assert.assertNotNull(store.getName(), "store.name must not be null.");
        Assert.assertNotNull(store.getName().getEn(),
                "store.name.en must not be null.");
        Assert.assertFalse(store.getName().getEn().trim().isEmpty(),
                "store.name.en must not be empty.");

        Assert.assertNotNull(store.getSlug(), "store.slug must not be null.");
       

        validateUrl(store.getHomepage());
    }

    private void validateDateTime(String clickTime) {
        try {
            LocalDateTime.parse(clickTime, DATE_TIME_FORMAT);
        } catch (DateTimeParseException exception) {
            Assert.fail("Invalid click_time format: " + clickTime
                    + ". Expected yyyy-MM-dd HH:mm:ss");
        }
    }
private void validateUrl(String homepage) {
    Assert.assertNotNull(homepage, "store.homepage must not be null.");
    Assert.assertFalse(homepage.trim().isEmpty(),
            "store.homepage must not be empty.");

    try {
        java.net.URL url = new java.net.URL(homepage);

        Assert.assertTrue(
                "http".equalsIgnoreCase(url.getProtocol())
                        || "https".equalsIgnoreCase(url.getProtocol()),
                "store.homepage must use HTTP or HTTPS: " + homepage
        );

        Assert.assertNotNull(url.getHost(),
                "store.homepage must contain a domain/host: " + homepage);

        Assert.assertFalse(url.getHost().isEmpty(),
                "store.homepage host must not be empty: " + homepage);

    } catch (Exception exception) {
        Assert.fail("store.homepage cannot be read as a URL: " + homepage);
    }
}
 
}