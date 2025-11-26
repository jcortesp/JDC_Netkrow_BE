package com.netkrow.backend.service;

import com.netkrow.backend.model.Expense;
import com.netkrow.backend.repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class ExpenseService {

    private final ExpenseRepository repository;

    public ExpenseService(ExpenseRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Expense create(Expense expense) {
        // Si no viene fecha, ponemos ahora
        if (expense.getExpenseDate() == null) {
            expense.setExpenseDate(LocalDateTime.now());
        }
        return repository.save(expense);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalExpenses(LocalDateTime from, LocalDateTime to) {
        return repository.sumAmountBetween(from, to);
    }
}
