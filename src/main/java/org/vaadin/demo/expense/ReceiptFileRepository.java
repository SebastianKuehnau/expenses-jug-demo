package org.vaadin.demo.expense;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceiptFileRepository extends JpaRepository<ReceiptFile, Long> {

    Optional<ReceiptFile> findByExpenseId(Long expenseId);

    void deleteByExpenseId(Long expenseId);
}
