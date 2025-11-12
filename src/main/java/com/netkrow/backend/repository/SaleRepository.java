package com.netkrow.backend.repository;

import com.netkrow.backend.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    @Query("""
       SELECT s FROM Sale s
       LEFT JOIN FETCH s.product
       LEFT JOIN FETCH s.customer
    """)
    List<Sale> findAllWithJoins();
}
