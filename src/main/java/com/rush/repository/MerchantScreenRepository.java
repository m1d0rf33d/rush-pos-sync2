package com.rush.repository;

import com.rush.model.Merchant;
import com.rush.model.MerchantScreen;
import com.rush.model.Role;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by aomine on 10/22/16.
 */
public interface MerchantScreenRepository extends CrudRepository<MerchantScreen, Long> {

    List<MerchantScreen> findByRoleAndMerchant(Role role, Merchant merchant);
}
