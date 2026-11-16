package com.bank.banking_core.service;

import com.bank.banking_core.dto.AccountResponseDto;
import com.bank.banking_core.dto.CreateAccountRequestDto;
import com.bank.banking_core.dto.TransactionRequestDto;
import com.bank.banking_core.dto.TransactionResponseDto;
import com.bank.banking_core.dto.TransferRequestDto;

import java.util.List;

public interface BankingService {
    AccountResponseDto createAccount(CreateAccountRequestDto request);
    List<AccountResponseDto> getAllAccounts();
    AccountResponseDto getAccountById(Long id);
    TransactionResponseDto deposit(Long accountId, TransactionRequestDto request);
    TransactionResponseDto withdraw(Long accountId, TransactionRequestDto request);
    List<TransactionResponseDto> transfer(TransferRequestDto request);
}
