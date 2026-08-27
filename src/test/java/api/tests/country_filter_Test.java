package api.tests;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import api.base.BaseAPITest;
import api.clients.Country_filter_Client;
import api.models.Store;
import io.restassured.response.Response;

public class country_filter_Test extends BaseAPITest {
    
    @Test
    public void testCountryFilter() {
        Country_filter_Client client = new Country_filter_Client();
        Response us_store_response = client.getCountryFilter("US");
        Response in_store_response = client.getCountryFilter("IN");
        country_filter_Test countryFilterTest = new country_filter_Test();
        Map<Integer,String> us_stores = countryFilterTest.getStoreIdAndName(us_store_response);
        Map<Integer,String> in_stores = countryFilterTest.getStoreIdAndName(in_store_response);
        boolean storeIdNotExists = countryFilterTest.check_IF_StoreID_Not_Exists(us_stores, in_stores);
        assertTrue(storeIdNotExists, "The response is not changing for the different countries /there are no much campaigns published");
      
    }

public Map<Integer,String> getStoreIdAndName(Response response) {
    List<Store> stores = response.jsonPath().getList("data.data", Store.class);
    Map<Integer,String> storeMap = new HashMap<>();
    for (Store store : stores) {
        storeMap.put(store.getId(), store.getName());
    }
    return storeMap;
}
public boolean check_IF_StoreID_Not_Exists(Map<Integer,String> us_stores,Map<Integer,String> in_stores)
{
Set<Integer> us_store_ids = us_stores.keySet();

for (Integer storeId : us_store_ids) {
    if (!in_stores.containsKey(storeId)) {
        System.out.println("Store ID " + storeId + " exists in US stores but not in IN stores."+"Name :"+us_stores.get(storeId)+" but this was not found in IN stores ");

        
        return true; 
    }
}

return false;
}

}