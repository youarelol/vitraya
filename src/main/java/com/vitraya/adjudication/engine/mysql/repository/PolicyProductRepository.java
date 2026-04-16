package com.vitraya.adjudication.engine.mysql.repository;

import com.vitraya.adjudication.engine.mysql.entity.PolicyProduct;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

public interface PolicyProductRepository extends CrudRepository<PolicyProduct, Long> {

    @Query("select * from policy_product where policy_name = :policyName")
    PolicyProduct findByPolicyName(String policyName);
}
