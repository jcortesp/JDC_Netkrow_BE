package com.netkrow.backend.repository;

import com.netkrow.backend.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Query("""
           SELECT COALESCE(SUM(e.amount), 0)
           FROM Expense e
           WHERE e.expenseDate BETWEEN :from AND :to
           """)
    BigDecimal sumAmountBetween(LocalDateTime from, LocalDateTime to);

    List<Expense> findByExpenseDateBetweenOrderByExpenseDateDesc(
            LocalDateTime from,
            LocalDateTime to
    );
}
