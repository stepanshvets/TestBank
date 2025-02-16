package org.example.bank;

import lombok.extern.slf4j.Slf4j;
import org.example.bank.model.Account;
import org.example.bank.model.Operation;
import org.example.bank.model.OperationType;
import org.example.bank.model.exception.AmountValueException;
import org.example.bank.model.exception.NotEnoughException;
import org.example.bank.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.example.bank.model.OperationType.DEPOSIT;
import static org.example.bank.model.OperationType.WITHDRAW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Slf4j
class TestBankAccountApplicationTests {
    private final static Integer SEED = 42;

    @Autowired
    private AccountService accountService;

    @Test
    void testCreateAndGetById() {
        final Account createdAccount = accountService.create();
        final Account foundAccount = accountService.getById(createdAccount.getId());
        log.info("Created id {}, found id {}", createdAccount.getId(), foundAccount.getId());
        assertEquals(createdAccount.getBalance(), foundAccount.getBalance());
    }

    @Test
    void testDeposit() {
        final Account createdAccount = accountService.create();

        final Double incomeMoneyAmount = 20.5;

        final Operation depositOperation = new Operation();
        depositOperation.setAccountId(createdAccount.getId());
        depositOperation.setAmount(incomeMoneyAmount);
        depositOperation.setType(DEPOSIT);

        final Account accountAfterOperation = accountService.makeOperation(depositOperation);

        final Double correctBalance = createdAccount.getBalance() + incomeMoneyAmount;
        assertEquals(correctBalance, accountAfterOperation.getBalance());
    }

    @Test
    void testFailDepositWhenAmountInvalid() {
            final Account createdAccount = accountService.create();

        final Double incomeMoneyAmount = -1.0;

        final Operation depositOperation = new Operation();
        depositOperation.setAccountId(createdAccount.getId());
        depositOperation.setAmount(incomeMoneyAmount);
        depositOperation.setType(DEPOSIT);

        assertThrows(AmountValueException.class, () -> accountService.makeOperation(depositOperation));

        final Double correctBalance = createdAccount.getBalance();

        final Account accountAfterOperation = accountService.getById(createdAccount.getId());
        assertEquals(correctBalance, accountAfterOperation.getBalance());
    }

    @Test
    void testWithdraw() {
        final Account createdAccount = accountService.create();

        final Double incomeMoneyAmount = 20.5;
        final Double outcomeMoneyAmount = 12.5;

        // deposit before
        final Operation depositOperation = new Operation();
        depositOperation.setAccountId(createdAccount.getId());
        depositOperation.setAmount(incomeMoneyAmount);
        depositOperation.setType(DEPOSIT);
        accountService.makeOperation(depositOperation);

        // deposit after
        final Operation withdrawOperation = new Operation();
        withdrawOperation.setAccountId(createdAccount.getId());
        withdrawOperation.setAmount(outcomeMoneyAmount);
        withdrawOperation.setType(OperationType.WITHDRAW);
        accountService.makeOperation(withdrawOperation);

        final Double correctBalance = createdAccount.getBalance() + incomeMoneyAmount - outcomeMoneyAmount;

        final Account accountAfterOperation = accountService.getById(createdAccount.getId());
        assertEquals(correctBalance, accountAfterOperation.getBalance());
    }

    @Test
    void testFailWithdrawWhenNotEnough() {
        final Account createdAccount = accountService.create();

        final Double incomeMoneyAmount = 11.5;
        final Double outComeMoneyAmount = 12.5;

        // deposit before
        final Operation depositOperation = new Operation();
        depositOperation.setAccountId(createdAccount.getId());
        depositOperation.setAmount(incomeMoneyAmount);
        depositOperation.setType(DEPOSIT);
        accountService.makeOperation(depositOperation);

        // deposit after
        final Operation withdrawOperation = new Operation();
        withdrawOperation.setAccountId(createdAccount.getId());
        withdrawOperation.setAmount(outComeMoneyAmount);
        withdrawOperation.setType(OperationType.WITHDRAW);

        assertThrows(NotEnoughException.class, () -> accountService.makeOperation(withdrawOperation));

        // correctBalance count only income
        final Double correctBalance = createdAccount.getBalance() + incomeMoneyAmount;

        final Account accountAfterOperation = accountService.getById(createdAccount.getId());
        assertEquals(correctBalance, accountAfterOperation.getBalance());
    }

    /**
     * Create 10 operations for each of 3 threads.
     * Run them in multithreading mode and compare the final account balance.
     * @throws InterruptedException
     */
    @Test
    void testLockWithTenOperationPerThread() throws InterruptedException {
        final Account createdAccount = accountService.create();
        final List<Operation> firstThreadOperations = getFirstThreadOperations(createdAccount.getId());
        final List<Operation> secondThreadOperations = getSecondThreadOperations(createdAccount.getId());
        final List<Operation> thirdThreadOperations = getThirdThreadOperations(createdAccount.getId());

        final Thread firstThread = new Thread() {
            public void run(){
                firstThreadOperations.forEach(op ->  accountService.makeOperation(op));
            }
        };

        final Thread secondThread = new Thread() {
            public void run(){
                secondThreadOperations.forEach(op ->  accountService.makeOperation(op));
            }
        };

        final Thread thirdThread = new Thread() {
            public void run(){
                thirdThreadOperations.forEach(op ->  accountService.makeOperation(op));
            }
        };

        firstThread.start();
        secondThread.start();
        thirdThread.start();

        firstThread.join();
        secondThread.join();
        thirdThread.join();

        final Double correctBalance = countTotalBalance(new List[]{
                firstThreadOperations,
                secondThreadOperations,
                thirdThreadOperations
        });

        final Account accountAfterOperation = accountService.getById(createdAccount.getId());

        final Double acceptableError = 0.001;
        assertTrue( Math.abs(correctBalance - accountAfterOperation.getBalance()) < acceptableError);
    }

    /**
     * Generate 10.000 operations for each of 3 threads.
     * Run them in multithreading mode and compare the final account balance.
     * Generation occurs in such a way that when withdrawing there will little probability to be not enough balance.
     * @throws InterruptedException
     */
    @Test
    void testLockWithTenThousandsOperationPerThread() throws InterruptedException {
        final Account createdAccount = accountService.create();

        final Integer operationPerThread = 10000;
        final List<Operation> firstThreadOperations =
                generateOperations(1, operationPerThread, createdAccount.getId());
        final List<Operation> secondThreadOperations =
                generateOperations(2, operationPerThread,createdAccount.getId());
        final List<Operation> thirdThreadOperations =
                generateOperations(3, operationPerThread, createdAccount.getId());

        final Thread firstThread = new Thread() {
            public void run(){
                firstThreadOperations.forEach(op ->  accountService.makeOperation(op));
            }
        };

        final Thread secondThread = new Thread() {
            public void run(){
                secondThreadOperations.forEach(op ->  accountService.makeOperation(op));
            }
        };

        final Thread thirdThread = new Thread() {
            public void run(){
                thirdThreadOperations.forEach(op ->  accountService.makeOperation(op));
            }
        };

        firstThread.start();
        secondThread.start();
        thirdThread.start();

        firstThread.join();
        secondThread.join();
        thirdThread.join();

        final Double correctBalance = countTotalBalance(new List[]{
                firstThreadOperations,
                secondThreadOperations,
                thirdThreadOperations
        });

        final Account accountAfterOperation = accountService.getById(createdAccount.getId());

        final Double acceptableError = 0.001;
        log.info("Expected {}, actual id {}", correctBalance, accountAfterOperation.getBalance());
        assertTrue( Math.abs(correctBalance - accountAfterOperation.getBalance()) < acceptableError);
    }

    private Double countTotalBalance(List<Operation>[] array) {
        return Arrays.stream(array)
                .mapToDouble(this::countBalance)
                .sum();
    }

    private Double countBalance(List<Operation> operations) {
        return operations.stream()
                .mapToDouble(op -> op.getType() == OperationType.DEPOSIT ? op.getAmount() : -op.getAmount())
                .sum();
    }

    private List<Operation> generateOperations(Integer num, Integer operationCount, UUID accountId) {
        final Random random = new Random(SEED + num);

        final List<Operation> operations = new ArrayList<>(operationCount);
        Double balance = 0.0;

        operations.add(new Operation(accountId, DEPOSIT, 10.0));

        for (int i = 1; i < operationCount; i++) {
            // DEPOSIT occurs 4 times more often to avoid not enough, 0.2 - WITHDRAW, 0.8 - DEPOSIT
            final OperationType operationType = random.nextDouble() + 0.3 > 0.5 ? DEPOSIT : WITHDRAW;
            // little percent to avoid overflow
            final Double tmpPercent = random.nextDouble() * 0.01 + 0.01;
            final Double amount = tmpPercent * 0.01 * balance + 1;

            operations.add(new Operation(accountId, operationType, amount));

            balance = operationType.equals(DEPOSIT) ? balance + amount : balance - amount;
        }

        return operations;
    }

    private List<Operation> getFirstThreadOperations(UUID accountId) {
        return List.of(
                new Operation(accountId, DEPOSIT, 12.5),
                new Operation(accountId, DEPOSIT, 20.1),
                new Operation(accountId, DEPOSIT, 1.01),
                new Operation(accountId, DEPOSIT, 0.76),
                new Operation(accountId, DEPOSIT, 12.4),
                new Operation(accountId, WITHDRAW, 5.5),
                new Operation(accountId, WITHDRAW, 1.07),
                new Operation(accountId, DEPOSIT, 1.11),
                new Operation(accountId, DEPOSIT, 68.0),
                new Operation(accountId, WITHDRAW, 32.1)
        );
    }

    private List<Operation> getSecondThreadOperations(UUID accountId) {
        return List.of(
                new Operation(accountId, DEPOSIT, 1.5),
                new Operation(accountId, DEPOSIT, 6.1),
                new Operation(accountId, DEPOSIT, 5.01),
                new Operation(accountId, WITHDRAW, 0.76),
                new Operation(accountId, DEPOSIT, 12.4),
                new Operation(accountId, DEPOSIT, 5.5),
                new Operation(accountId, WITHDRAW, 1.07),
                new Operation(accountId, WITHDRAW, 1.11),
                new Operation(accountId, WITHDRAW, 1.01),
                new Operation(accountId, WITHDRAW, 2.4)
        );
    }

    private List<Operation> getThirdThreadOperations(UUID accountId) {
        return List.of(
                new Operation(accountId, DEPOSIT, 3.5),
                new Operation(accountId, WITHDRAW, 1.1),
                new Operation(accountId, DEPOSIT, 5.01),
                new Operation(accountId, WITHDRAW, 0.1),
                new Operation(accountId, WITHDRAW, 2.47),
                new Operation(accountId, DEPOSIT, 5.5),
                new Operation(accountId, WITHDRAW, 1.07),
                new Operation(accountId, WITHDRAW, 2.11),
                new Operation(accountId, WITHDRAW, 1.01),
                new Operation(accountId, DEPOSIT, 2.4)
        );
    }

}
