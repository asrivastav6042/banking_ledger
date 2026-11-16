package com.bank.banking_core.controller;

import com.bank.banking_core.dto.AccountResponseDto;
import com.bank.banking_core.dto.CreateAccountRequestDto;
import com.bank.banking_core.dto.TransactionRequestDto;
import com.bank.banking_core.dto.TransactionResponseDto;
import com.bank.banking_core.response.ApiResponse;
import com.bank.banking_core.service.BankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Account Management", description = "Endpoints for creating and managing banking accounts and operations")
public class AccountController {

    private final BankingService bankingService;

    @PostMapping
    @Operation(summary = "Create a new bank account")
    public ResponseEntity<ApiResponse<AccountResponseDto>> createAccount(@Valid @RequestBody CreateAccountRequestDto request) {
        AccountResponseDto created = bankingService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Account created successfully"));
    }

    @GetMapping
    @Operation(summary = "List all accounts")
    public ResponseEntity<ApiResponse<List<AccountResponseDto>>> getAllAccounts() {
        List<AccountResponseDto> accounts = bankingService.getAllAccounts();
        return ResponseEntity.ok(ApiResponse.success(accounts, "Accounts retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get account details by ID")
    public ResponseEntity<ApiResponse<AccountResponseDto>> getAccountById(@PathVariable Long id) {
        AccountResponseDto account = bankingService.getAccountById(id);
        return ResponseEntity.ok(ApiResponse.success(account, "Account retrieved successfully"));
    }

    @PostMapping("/{id}/deposit")
    @Operation(summary = "Deposit funds into an account (Uses Distributed Lock, Ledger, and Kafka Event)")
    public ResponseEntity<ApiResponse<TransactionResponseDto>> deposit(
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequestDto request) {
        TransactionResponseDto transaction = bankingService.deposit(id, request);
        return ResponseEntity.ok(ApiResponse.success(transaction, "Deposit processed successfully"));
    }

    @PostMapping("/{id}/withdraw")
    @Operation(summary = "Withdraw funds from an account (Uses Distributed Lock, Ledger, and Kafka Event)")
    public ResponseEntity<ApiResponse<TransactionResponseDto>> withdraw(
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequestDto request) {
        TransactionResponseDto transaction = bankingService.withdraw(id, request);
        return ResponseEntity.ok(ApiResponse.success(transaction, "Withdrawal processed successfully"));
    }
}
