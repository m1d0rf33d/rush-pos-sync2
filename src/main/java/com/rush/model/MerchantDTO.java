package com.rush.model;

/**
 * Created by aomine on 10/18/16.
 */
public class MerchantDTO {

    private Long id;

    private String name;
    private String uniqueKey;
    private String merchantApiKey;
    private String merchantApiSecret;

    private String customerApiKey;
    private String customerApiSecret;
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUniqueKey() {
        return uniqueKey;
    }

    public void setUniqueKey(String uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    public String getMerchantApiKey() {
        return merchantApiKey;
    }

    public void setMerchantApiKey(String merchantApiKey) {
        this.merchantApiKey = merchantApiKey;
    }

    public String getMerchantApiSecret() {
        return merchantApiSecret;
    }

    public void setMerchantApiSecret(String merchantApiSecret) {
        this.merchantApiSecret = merchantApiSecret;
    }

    public String getCustomerApiKey() {
        return customerApiKey;
    }

    public void setCustomerApiKey(String customerApiKey) {
        this.customerApiKey = customerApiKey;
    }

    public String getCustomerApiSecret() {
        return customerApiSecret;
    }

    public void setCustomerApiSecret(String customerApiSecret) {
        this.customerApiSecret = customerApiSecret;
    }
}
