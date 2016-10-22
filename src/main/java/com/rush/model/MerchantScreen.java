package com.rush.model;

import com.rush.model.enums.Screen;

import javax.persistence.*;

/**
 * Created by aomine on 10/22/16.
 */
@Entity
@Table(name = "merchant_screens")
public class MerchantScreen {

    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private long id;

    @ManyToOne
    @JoinColumn(name = "role")
    private Role role;

    @Enumerated(EnumType.STRING)
    private Screen screen;

    @ManyToOne
    @JoinColumn(name = "merchant")
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Screen getScreen() {
        return screen;
    }

    public void setScreen(Screen screen) {
        this.screen = screen;
    }
}
