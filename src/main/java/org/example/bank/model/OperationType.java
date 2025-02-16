package org.example.bank.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum OperationType {
    DEPOSIT(1),
    WITHDRAW(2);

    private final Integer id;
}
