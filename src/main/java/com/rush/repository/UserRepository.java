package com.rush.repository;

import com.rush.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * Created by aomine on 10/20/16.
 */
public interface UserRepository extends CrudRepository<User, Long> {

    Optional<User> findOneByUsername(String login);
    User findOneByUuid(String uuid);
}
