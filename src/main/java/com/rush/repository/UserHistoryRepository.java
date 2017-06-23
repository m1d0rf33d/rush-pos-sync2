package com.rush.repository;

import com.rush.model.UserHistory;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by aomine on 6/21/17.
 */
public interface UserHistoryRepository extends JpaRepository<UserHistory, Long>{
}
