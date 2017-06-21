package com.rush.model.enums;

/**
 * Created by aomine on 2/28/17.
 */
public enum MerchantType {
    LOYALTY("loyalty"), PUNCHCARD("punchcard");

    private String value;

    private MerchantType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
