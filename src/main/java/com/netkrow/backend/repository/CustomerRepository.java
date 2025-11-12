package com.netkrow.backend.repository;

import com.netkrow.backend.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmailIgnoreCase(String email);

    Optional<Customer> findByDocumentIdIgnoreCase(String documentId);
}
