package org.vaadin.demo.expense;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class ExpenseService {

    private final ExpenseRepository repository;

    public ExpenseService(ExpenseRepository repository) {
        this.repository = repository;
    }

    /** One page of expenses; the Grid asks for exactly the rows it currently shows. */
    public List<Expense> find(ExpenseFilter filter, Pageable page) {
        return repository.findAll(toSpecification(filter), page).getContent();
    }

    public long count(ExpenseFilter filter) {
        return repository.count(toSpecification(filter));
    }

    public List<Expense> findAll() {
        return repository.findAll();
    }

    public Expense save(Expense expense) {
        return repository.save(expense);
    }

    public void delete(Expense expense) {
        repository.delete(expense);
    }

    private Specification<Expense> toSpecification(ExpenseFilter filter) {
        List<Specification<Expense>> parts = new ArrayList<>();
        if (filter.text() != null && !filter.text().isBlank()) {
            String pattern = "%" + filter.text().trim().toLowerCase() + "%";
            parts.add((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("merchant")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)));
        }
        if (filter.category() != null) {
            parts.add((root, query, cb) -> cb.equal(root.get("category"), filter.category()));
        }
        if (filter.minAmount() != null) {
            parts.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("amount"), filter.minAmount()));
        }
        if (filter.maxAmount() != null) {
            parts.add((root, query, cb) -> cb.lessThanOrEqualTo(root.get("amount"), filter.maxAmount()));
        }
        if (filter.from() != null) {
            parts.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("date"), filter.from()));
        }
        if (filter.to() != null) {
            parts.add((root, query, cb) -> cb.lessThanOrEqualTo(root.get("date"), filter.to()));
        }
        return Specification.allOf(parts);
    }
}
