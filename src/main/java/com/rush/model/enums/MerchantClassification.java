package com.rush.model.enums;

/**
 * Created by aomine on 3/15/17.
 */
public enum MerchantClassification {

    BASIC("basic"), GLOBE_SG("globe_sg");

    private String value;

    private MerchantClassification(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
