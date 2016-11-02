package com.rush.model.enums;

/**
 * Created by aomine on 10/22/16.
 */
public enum Screen {

    REGISTER("REGISTER"),
    MEMBER_PROFILE("MEMBER_PROFILE"),
    GIVE_POINTS("GIVE_POINTS"),
    GIVE_POINTS_OCR("GIVE_POINTS_OCR"),
    PAY_WITH_POINTS("PAY_WITH_POINTS"),
    REDEEM_REWARDS("REDEEM_REWARDS"),
    ISSUE_REWARDS("ISSUE_REWARDS"),
    TRANSACTIONS_VIEW("TRANSACTIONS_VIEW"),
    OCR_SETTINGS("OCR_SETTINGS"),
    OFFLINE_TRANSACTIONS("OFFLINE_TRANSACTIONS"),
    EXIT_MEMBER("EXIT_MEMBER");

    private String value;

    Screen(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
