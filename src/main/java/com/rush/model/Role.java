package com.rush.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/** @Author fsociety
 *
 */
@Entity
@Table(name = "role")
public class Role {

    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private long id;

    @ManyToOne
    @JoinColumn(name = "merchant_id")
    private Merchant merchant;

    public Merchant getMerchant() {
        return merchant;
    }

    public void setMerchant(Merchant merchant) {
        this.merchant = merchant;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
