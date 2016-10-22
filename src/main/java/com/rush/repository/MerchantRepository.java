package com.rush.repository;

import com.rush.model.Merchant;
import com.rush.model.enums.MerchantStatus;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by aomine on 10/18/16.
 */
public interface MerchantRepository extends JpaRepository<Merchant, Long> {

    Merchant findOneByUniqueKeyAndStatus(String uniqueKey, MerchantStatus status);
}
