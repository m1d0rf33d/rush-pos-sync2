package com.rush.repository;

import com.rush.model.AppUpdate;
import com.rush.model.Merchant;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by aomine on 12/12/16.
 */
public interface AppUpdateRepository extends CrudRepository<AppUpdate, Long> {

    @Query(nativeQuery = true, value = "select * from app_update where merchant_id = :merchantId order by id desc limit 1")
    AppUpdate findLatestVersionEntry(@Param("merchantId") Long merchantId);
}
