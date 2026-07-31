package api.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ClicksResponse {

    private Integer success;
    private PaginationData data;
    private Integer error;
    private Object msg;

    public Integer getSuccess() {
        return success;
    }

    public PaginationData getData() {
        return data;
    }

    public Integer getError() {
        return error;
    }

    public Object getMsg() {
        return msg;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static class PaginationData {

        @JsonProperty("current_page")
        private Integer currentPage;

        private List<Click> data;

        @JsonProperty("per_page")
        private Integer perPage;

        private Integer total;

        @JsonProperty("last_page")
        private Integer lastPage;

        @JsonProperty("next_page_url")
        private String nextPageUrl;

        @JsonProperty("prev_page_url")
        private String prevPageUrl;

        public Integer getCurrentPage() {
            return currentPage;
        }

        public List<Click> getData() {
            return data;
        }

        public Integer getPerPage() {
            return perPage;
        }

        public Integer getTotal() {
            return total;
        }

        public Integer getLastPage() {
            return lastPage;
        }

        public String getNextPageUrl() {
            return nextPageUrl;
        }

        public String getPrevPageUrl() {
            return prevPageUrl;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static class Click {

        private Long id;
        private String code;

        @JsonProperty("click_time")
        private String clickTime;

        @JsonProperty("store_id")
        private Long storeId;

        @JsonProperty("user_id")
        private Long userId;

        @JsonProperty("cashback_enabled")
        private Boolean cashbackEnabled;

        @JsonProperty("cashback_type")
        private String cashbackType;

        @JsonProperty("cashback_percent")
        private String cashbackPercent;

        @JsonProperty("can_claim")
        private Integer canClaim;

        @JsonProperty("network_campaign_id")
        private String networkCampaignId;

        private Store store;

        public Long getId() {
            return id;
        }

        public String getCode() {
            return code;
        }

        public String getClickTime() {
            return clickTime;
        }

        public Long getStoreId() {
            return storeId;
        }

        public Long getUserId() {
            return userId;
        }

        public Boolean getCashbackEnabled() {
            return cashbackEnabled;
        }

        public String getCashbackType() {
            return cashbackType;
        }

        public String getCashbackPercent() {
            return cashbackPercent;
        }

        public Integer getCanClaim() {
            return canClaim;
        }

        public String getNetworkCampaignId() {
            return networkCampaignId;
        }

        public Store getStore() {
            return store;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static class Store {

        private Long id;
        private StoreName name;
        private String slug;
        private String homepage;

        @JsonProperty("domain_name")
        private String domainName;

        @JsonProperty("cashback_enabled")
        private Integer cashbackEnabled;

        public Long getId() {
            return id;
        }

        public StoreName getName() {
            return name;
        }

        public String getSlug() {
            return slug;
        }

        public String getHomepage() {
            return homepage;
        }

        public String getDomainName() {
            return domainName;
        }

        public Integer getCashbackEnabled() {
            return cashbackEnabled;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static class StoreName {

        private String en;

        public String getEn() {
            return en;
        }
    }
}