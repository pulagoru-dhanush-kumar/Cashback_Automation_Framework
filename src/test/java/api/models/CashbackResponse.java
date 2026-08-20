package api.models;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class CashbackResponse {

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

        private List<CashbackTransaction> data;

        @JsonProperty("per_page")
        private Integer perPage;

        private Integer total;

        @JsonProperty("last_page")
        private Integer lastPage;

        @JsonProperty("next_page_url")
        private String nextPageUrl;

        public Integer getCurrentPage() {
            return currentPage;
        }

        public List<CashbackTransaction> getData() {
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
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static class CashbackTransaction {

        private Long id;

        @JsonProperty("user_id")
        private Long userId;

        @JsonProperty("order_id")
        private String orderId;

        @JsonProperty("store_id")
        private Long storeId;

        @JsonProperty("order_amount")
        private BigDecimal orderAmount;

        // API commission amount
        private BigDecimal cashback;

        private String currency;
        private String status;

        public Long getId() {
            return id;
        }

        public Long getUserId() {
            return userId;
        }

        public String getOrderId() {
            return orderId;
        }

        public Long getStoreId() {
            return storeId;
        }

        public BigDecimal getOrderAmount() {
            return orderAmount;
        }

        public BigDecimal getCashback() {
            return cashback;
        }

        public String getCurrency() {
            return currency;
        }

        public String getStatus() {
            return status;
        }
    }
}