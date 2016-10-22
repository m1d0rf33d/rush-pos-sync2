package com.rush.model;

import com.rush.model.enums.MerchantStatus;

import javax.persistence.*;

/**
 * Created by aomine on 10/18/16.
 */
@Entity
@Table(name = "merchant")
public class Merchant {

    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;

    @Column
    private String name;
    @Column
    private String uniqueKey;
    @Column
    private String merchantApiKey;
    @Column
    private String merchantApiSecret;
    @Column
    private String customerApiKey;
    @Column
    private String customerApiSecret;

    @Enumerated(EnumType.STRING)
    @Column
    private MerchantStatus status;

    public MerchantStatus getStatus() {
        return status;
    }

    public void setStatus(MerchantStatus status) {
        this.status = status;
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
}
