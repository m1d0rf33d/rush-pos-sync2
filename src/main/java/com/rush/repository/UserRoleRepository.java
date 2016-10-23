package com.rush.repository;

import com.rush.model.Role;
import com.rush.model.User;
import com.rush.model.UserRole;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by aomine on 10/23/16.
 */
public interface UserRoleRepository extends CrudRepository<UserRole, Long>{
    List<UserRole> findByUser(User user);
    List<UserRole> findByRole(Role role);
}
