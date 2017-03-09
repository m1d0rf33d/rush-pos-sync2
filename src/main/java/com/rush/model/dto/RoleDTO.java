package com.rush.model.dto;

import java.util.List;

/**
 * Created by aomine on 10/22/16.
 */
public class RoleDTO {

    private Long merchantId;
    private Long roleId;
    private String name;
    private List<ScreenDTO> screens;

    public Long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ScreenDTO> getScreens() {
        return screens;
    }

    public void setScreens(List<ScreenDTO> screens) {
        this.screens = screens;
    }
}
