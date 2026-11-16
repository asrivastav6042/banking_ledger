package com.bank.banking_core.controller;

import com.bank.banking_core.dto.TransactionResponseDto;
import com.bank.banking_core.dto.TransferRequestDto;
import com.bank.banking_core.response.ApiResponse;
import com.bank.banking_core.service.BankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
@Tag(name = "Transfer Management", description = "Endpoints for funds transfer between accounts")
public class TransferController {

    private final BankingService bankingService;

    @PostMapping
    @Operation(summary = "Transfer funds between accounts (Uses Distributed Lock, Ledger entries, and Kafka Events)")
    public ResponseEntity<ApiResponse<List<TransactionResponseDto>>> transfer(@Valid @RequestBody TransferRequestDto request) {
        List<TransactionResponseDto> transactions = bankingService.transfer(request);
        return ResponseEntity.ok(ApiResponse.success(transactions, "Transfer completed successfully"));
    }
}
