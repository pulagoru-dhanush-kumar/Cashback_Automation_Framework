package api.tests;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import api.base.BaseAPITest;
import api.clients.StoreClient;
import api.models.*;
import io.restassured.response.Response;

public class StoreApiTest extends BaseAPITest {

    private static final Set<String> ALLOWED_AMOUNT_TYPES = new HashSet<>(Arrays.asList("fixed", "percent"));
    private static final Set<String> ALLOWED_RATE_TYPES = new HashSet<>(Arrays.asList("upto", "flat"));
    private static final Set<Integer> ALLOWED_CASHBACK_ENABLED = new HashSet<>(Arrays.asList(0, 1));
    

    @Test
    public void StoreS_Details_api_validation() {
        StoreClient storeClient = new StoreClient();

        // ---- 1. Response-level checks ----
        Response response = storeClient.getStoreDetails("US");

        Assert.assertEquals(response.getStatusCode(), 200,
                "Expected 200 OK. Body: " + response.getBody().asString());

        Integer success = response.jsonPath().getInt("success");
        Assert.assertEquals(success, Integer.valueOf(1),
                "API did not report success=1. Body: " + response.getBody().asString());

        // ---- 2. Deserialize + list-level checks ----
        List<Store> stores = storeClient.getStores("US");
        System.out.println("Total stores retrieved: " + stores.size());

        Assert.assertNotNull(stores, "Store list was null - response shape may have changed.");
        Assert.assertFalse(stores.isEmpty(), "Store list was empty.");

        Integer total = response.jsonPath().getInt("data.total");
        if (total != null) {
            Assert.assertEquals(stores.size(), total.intValue(),
                    "Deserialized store count does not match data.total from the response.");
        }

        // ---- 3. Per-store field validation (soft asserts so one bad record doesn't hide the rest) ----
        SoftAssert softAssert = new SoftAssert();
        int missingLogoCount = 0;
        int missingDomainCount = 0;
        int cashbackDisabledCount = 0;
        Set<Integer> seenIds = new HashSet<>();
        Set<Integer> duplicateIds = new TreeSet<>();

        for (Store store : stores) {
            String label = "Store[id=" + store.getId() + ", name=" + store.getName() + "] ";

            softAssert.assertNotNull(store.getId(), label + "id is null");
            if (store.getId() != null) {
                softAssert.assertTrue(store.getId() > 0, label + "id should be positive");
                if (!seenIds.add(store.getId())) {
                    duplicateIds.add(store.getId());
                }
            }

            softAssert.assertTrue(isNotBlank(store.getName()), label + "name is missing/blank");
            softAssert.assertTrue(isNotBlank(store.getSlug()), label + "slug is missing/blank");

            // Known to be occasionally missing in the source data - log only, don't fail the test.
            if (!isNotBlank(store.getLogo())) {
                missingLogoCount++;
                System.out.println("[WARN] " + label + "logo is missing/blank");
            }
            if (!isNotBlank(store.getDomainName())) {
                missingDomainCount++;
                System.out.println("[WARN] " + label + "domain_name is missing/blank");
            }

            softAssert.assertTrue(isNotBlank(store.getHomepage()), label + "homepage is missing/blank");
            if (isNotBlank(store.getHomepage())) {
                softAssert.assertTrue(
                        store.getHomepage().startsWith("http://") || store.getHomepage().startsWith("https://"),
                        label + "homepage is not a valid URL: " + store.getHomepage());
            }

            // cashback_enabled - mandatory, must be exactly 0 or 1
            softAssert.assertNotNull(store.getCashbackEnabled(), label + "cashback_enabled is missing (null)");
            if (store.getCashbackEnabled() != null) {
                softAssert.assertTrue(ALLOWED_CASHBACK_ENABLED.contains(store.getCashbackEnabled()),
                        label + "cashback_enabled should be 0 or 1, was: " + store.getCashbackEnabled());
                if (store.getCashbackEnabled() == 0) {
                    cashbackDisabledCount++;
                    System.out.println("[INFO] " + label + "cashback_enabled=0");
                }
            }

            // cashback_amount - validated independently of cashback_enabled
            softAssert.assertTrue(isNotBlank(store.getCashbackAmount()), label + "cashback_amount is missing/blank");
            if (isNotBlank(store.getCashbackAmount())) {
                softAssert.assertTrue(isNumeric(store.getCashbackAmount()),
                        label + "cashback_amount is not numeric: " + store.getCashbackAmount());
            }

            // amount_type - mandatory, must be one of: fixed, percent
            softAssert.assertTrue(isNotBlank(store.getAmountType()), label + "amount_type is missing/blank");
            if (isNotBlank(store.getAmountType())) {
                softAssert.assertTrue(ALLOWED_AMOUNT_TYPES.contains(store.getAmountType().toLowerCase()),
                        label + "amount_type invalid: '" + store.getAmountType() + "' (expected fixed/percent)");
            }

            // rate_type - mandatory, must be one of: upto, flat
            softAssert.assertTrue(isNotBlank(store.getRateType()), label + "rate_type is missing/blank");
            if (isNotBlank(store.getRateType())) {
                softAssert.assertTrue(ALLOWED_RATE_TYPES.contains(store.getRateType().toLowerCase()),
                        label + "rate_type invalid: '" + store.getRateType() + "' (expected upto/flat)");
            }

            // Mandatory fields with no fixed value set
            softAssert.assertTrue(isNotBlank(store.getCashbackType()), label + "cashback_type is missing/blank");
            softAssert.assertNotNull(store.getIsClaimable(), label + "is_claimable is missing (null)");
            softAssert.assertTrue(isNotBlank(store.getCashbackString()), label + "cashback_string is missing/blank");

            if (store.getVisits() != null) {
                softAssert.assertTrue(store.getVisits() >= 0, label + "visits cannot be negative");
            }
            if (store.getClicks() != null) {
                softAssert.assertTrue(store.getClicks() >= 0, label + "clicks cannot be negative");
            }
            if (store.getOffersCount() != null) {
                softAssert.assertTrue(store.getOffersCount() >= 0, label + "offers_count cannot be negative");
            }

            softAssert.assertTrue(store.getCountries() != null && !store.getCountries().isEmpty(),
                    label + "countries list is missing/empty");
        }

        System.out.println("[SUMMARY] " + missingLogoCount + " store(s) missing logo, "
                + missingDomainCount + " store(s) missing domain_name, "
                + cashbackDisabledCount + " store(s) with cashback_enabled=0 (all non-blocking).");

        if (!duplicateIds.isEmpty()) {
            softAssert.fail("Duplicate store id(s) found: " + duplicateIds);
        }

        softAssert.assertAll();
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private boolean isNumeric(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Test
    public void compare_stores_differentcountries()
    {
List<Store> storesUS = new StoreClient().getStores("US");
List<Store> storesCA = new StoreClient().getStores("IN");

        Assert.assertNotEquals(storesUS.size(), storesCA.size(), "Store counts for US and IN should differ.");
Set<Integer> usIds = storesUS.stream().map(Store::getId).collect(Collectors.toSet());
Set<Integer> caIds = storesCA.stream().map(Store::getId).collect(Collectors.toSet());
Assert.assertNotEquals(usIds, caIds, "Store id sets for US and IN should differ.");
    }


@Test
public void SingleStore_Details_api_validation() {

    StoreClient storeClient = new StoreClient();

    List<Store> stores = storeClient.getStores("IN");

    List<Integer> storeIds = stores.stream()
            .map(Store::getId)
            .toList();

    if (storeIds.isEmpty()) {
        Assert.fail("No stores found for country IN");
    }

    Integer storeID = storeIds.get(
            new Random().nextInt(storeIds.size()));

    Response singleStoreResponse =
            storeClient.getStoreDetailsById("IN", storeID);

    Assert.assertNotNull(singleStoreResponse,
            "Response for single store details should not be null.");

    Assert.assertEquals(singleStoreResponse.getStatusCode(),
            200,
            "Expected 200 OK for single store details.");

    Store singleStore = singleStoreResponse
            .jsonPath()
            .getObject("data.store", Store.class);

    Assert.assertNotNull(singleStore,
            "Single store object should not be null.");

    Assert.assertEquals(singleStore.getId(),
            storeID,
            "Store ID in response does not match requested ID.");

    Assert.assertTrue(isNotBlank(singleStore.getName()),
            "Single store name should not be blank.");

    Assert.assertTrue(isNotBlank(singleStore.getSlug()),
            "Single store slug should not be blank.");

    Assert.assertTrue(isNotBlank(singleStore.getAbout()),
            "Single store about should not be blank.");

    Assert.assertTrue(isNotBlank(singleStore.getHomepage()),
            "Single store homepage should not be blank.");

    Assert.assertTrue(isNotBlank(singleStore.getCashbackAmount()),
            "Single store cashback amount should not be blank.");

    Assert.assertTrue(isNotBlank(singleStore.getCashbackType()),
            "Single store cashback type should not be blank.");

    Assert.assertTrue(isNotBlank(singleStore.getAmountType()),
            "Single store amount type should not be blank.");

    Assert.assertNotNull(singleStore.getCashbackEnabled(),
            "Cashback enabled should not be null.");



    SoftAssert softAssert = new SoftAssert();

    softAssert.assertNotNull(singleStore.getCats(),
            "Categories should not be null.");

    if (singleStore.getCats() != null) {
        softAssert.assertFalse(singleStore.getCats().isEmpty(),
                "Categories should not be empty.");
    }

    softAssert.assertNotNull(singleStore.getCountries(),
            "Countries should not be null.");

    if (singleStore.getCountries() != null) {
        softAssert.assertFalse(singleStore.getCountries().isEmpty(),
                "Countries should not be empty.");
    }

    
    softAssert.assertAll();
}



}