package com.rush.repository;

import com.rush.model.Merchant;
import com.rush.model.Role;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by aomine on 10/22/16.
 */
public interface RoleRepository extends CrudRepository<Role, Long> {
    Role findOneByName(String name);
    List<Role> findByMerchant(Merchant merchant);
}
