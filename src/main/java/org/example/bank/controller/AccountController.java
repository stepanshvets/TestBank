package org.example.bank.controller;

import lombok.RequiredArgsConstructor;
import org.example.bank.model.Account;
import org.example.bank.model.Operation;
import org.example.bank.service.AccountService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @PostMapping("/wallet/create")
    public Account create() {
        return accountService.create();
    }

    @GetMapping("/wallet/{id}")
    public Account getById(@PathVariable("id") UUID id) {
        return accountService.getById(id);
    }

    @PostMapping("/wallet")
    public Account makeOperation(@RequestBody Operation operation) {
        return accountService.makeOperation(operation);
    }
}
