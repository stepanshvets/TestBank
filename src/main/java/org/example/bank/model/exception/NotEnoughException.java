package org.example.bank.model.exception;

import java.util.UUID;

public class NotEnoughException extends CommonException{
    private static final String TEMPL_ERROR_MSG_NOT_ENOUGH =
            "There are not balance in account with id %s. Balance is %f but amount to withdraw is %f";

    public NotEnoughException(UUID accountId, Double balance, Double amount) {
        super(String.format(TEMPL_ERROR_MSG_NOT_ENOUGH, accountId, balance, amount));
    }
}
