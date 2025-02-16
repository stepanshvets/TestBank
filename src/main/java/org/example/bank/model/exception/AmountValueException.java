package org.example.bank.model.exception;


public class AmountValueException extends CommonException {
    private static final String TEMPL_ERROR_MSG_NOT_ENOUGH =
            "Amount should be greater than 0 but your is %f";
    
    public AmountValueException(Double amount) {
        super(String.format(TEMPL_ERROR_MSG_NOT_ENOUGH, amount));
    }
}
