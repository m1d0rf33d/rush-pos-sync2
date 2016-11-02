package com.rush.model;

import javax.persistence.*;

/**
 * Created by aomine on 11/2/16.
 */
@Entity
public class Branch {

    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private long id;

    @Column(name = "branch_uuid")
    private String uuid;

    @Column(name = "with_vk")
    private boolean withVk;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isWithVk() {
        return withVk;
    }

    public void setWithVk(boolean withVk) {
        this.withVk = withVk;
    }
}
