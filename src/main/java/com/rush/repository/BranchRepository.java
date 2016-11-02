package com.rush.repository;

import com.rush.model.Branch;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by aomine on 11/2/16.
 */
public interface BranchRepository extends CrudRepository<Branch, Long> {

    Branch findOneByUuid(String uuid);
}
