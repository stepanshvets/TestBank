package org.example.bank.service;

import lombok.RequiredArgsConstructor;
import org.example.bank.model.Account;
import org.example.bank.model.Operation;
import org.example.bank.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository repository;
    private final AccountValidator validator;

    @Transactional
    public Account create() {
        final Account accountToSave = new Account();
        accountToSave.setBalance(0.0);
        validator.validateOnCreate(accountToSave);
        return repository.save(accountToSave);
    }

    @Transactional(readOnly = true)
    public Account getById(UUID id) {
        return repository.findById(id).orElseThrow();
    }

    @Transactional
    public Account makeOperation(Operation operation) {
        final Account lockedAccount = repository.findByIdAndLock(operation.getAccountId())
                .orElseThrow();
        validator.validateOnOperation(lockedAccount, operation);

        final Double balanceAfterOperation;
        switch (operation.getType()) {
            case DEPOSIT:
                balanceAfterOperation = lockedAccount.getBalance() + operation.getAmount();
                break;
            case WITHDRAW:
                balanceAfterOperation = lockedAccount.getBalance() - operation.getAmount();
                break;
            default:
                throw new UnsupportedOperationException();
        }
        lockedAccount.setBalance(balanceAfterOperation);

        return repository.save(lockedAccount);
    }
}
