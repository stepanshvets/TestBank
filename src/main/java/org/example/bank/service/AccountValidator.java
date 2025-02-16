package org.example.bank.service;

import lombok.RequiredArgsConstructor;
import org.example.bank.model.Account;
import org.example.bank.model.Operation;
import org.example.bank.model.OperationType;
import org.example.bank.model.exception.AmountValueException;
import org.example.bank.model.exception.NotEnoughException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountValidator {

    public void validateOnCreate(final Account account) {
        // todo
    }

    public void validateOnOperation(final Account account, final Operation operation) {
        if (operation.getAmount() <= 0) {
            throw new AmountValueException(operation.getAmount());
        }

        if (operation.getType().equals(OperationType.WITHDRAW) &&
                operation.getAmount() > account.getBalance()) {
            throw new NotEnoughException(account.getId(), account.getBalance(), operation.getAmount());
        }
    }
}
