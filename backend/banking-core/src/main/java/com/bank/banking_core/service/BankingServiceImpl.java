package com.bank.banking_core.service;

import com.bank.banking_core.dto.*;
import com.bank.banking_core.entity.AccountEntity;
import com.bank.banking_core.entity.LedgerEntryEntity;
import com.bank.banking_core.enums.AccountStatus;
import com.bank.banking_core.enums.TransactionType;
import com.bank.banking_core.event.mapper.TransactionEventMapper;
import com.bank.banking_core.event.producer.BankingEventProducer;
import com.bank.banking_core.exception.AccountInactiveException;
import com.bank.banking_core.exception.DuplicateResourceException;
import com.bank.banking_core.exception.InsufficientBalanceException;
import com.bank.banking_core.exception.ResourceNotFoundException;
import com.bank.banking_core.ledger.LedgerService;
import com.bank.banking_core.mapper.AccountMapper;
import com.bank.banking_core.repository.AccountRepository;
import com.bank.banking_core.util.DistributedLockUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankingServiceImpl implements BankingService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final LedgerService ledgerService;
    private final TransactionEventMapper transactionEventMapper;
    private final BankingEventProducer eventProducer;
    private final DistributedLockUtil lockUtil;

    @Override
    @Transactional
    public AccountResponseDto createAccount(CreateAccountRequestDto request) {
        log.info("Creating new account with account number: {}", request.getAccountNumber());
        if (accountRepository.existsByAccountNumber(request.getAccountNumber())) {
            throw new DuplicateResourceException("Account with number " + request.getAccountNumber() + " already exists");
        }

        AccountEntity entity = accountMapper.toEntity(request);
        AccountEntity saved = accountRepository.save(entity);

        if (request.getInitialDeposit() != null && request.getInitialDeposit().compareTo(BigDecimal.ZERO) > 0) {
            String txRef = "INIT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            LedgerEntryEntity ledgerEntry = ledgerService.recordEntry(
                    txRef,
                    saved.getId(),
                    TransactionType.DEPOSIT,
                    request.getInitialDeposit(),
                    saved.getBalance(),
                    "Initial account deposit"
            );
            eventProducer.publishTransactionEvent(transactionEventMapper.toEvent(ledgerEntry));
        }

        return accountMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponseDto> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(accountMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponseDto getAccountById(Long id) {
        AccountEntity account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + id));
        return accountMapper.toDto(account);
    }

    @Override
    public TransactionResponseDto deposit(Long accountId, TransactionRequestDto request) {
        return lockUtil.executeWithAccountLock(accountId, () -> processDeposit(accountId, request));
    }

    @Transactional
    protected TransactionResponseDto processDeposit(Long accountId, TransactionRequestDto request) {
        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + accountId));

        validateAccountActive(account);

        BigDecimal newBalance = account.getBalance().add(request.getAmount());
        account.setBalance(newBalance);
        accountRepository.save(account);

        String txRef = "DEP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        LedgerEntryEntity ledgerEntry = ledgerService.recordEntry(
                txRef,
                account.getId(),
                TransactionType.DEPOSIT,
                request.getAmount(),
                newBalance,
                request.getDescription() != null ? request.getDescription() : "Account deposit"
        );

        eventProducer.publishTransactionEvent(transactionEventMapper.toEvent(ledgerEntry));

        return TransactionResponseDto.builder()
                .transactionReference(txRef)
                .accountId(account.getId())
                .transactionType(TransactionType.DEPOSIT)
                .amount(request.getAmount())
                .balanceAfter(newBalance)
                .description(ledgerEntry.getDescription())
                .timestamp(ledgerEntry.getCreatedAt())
                .build();
    }

    @Override
    public TransactionResponseDto withdraw(Long accountId, TransactionRequestDto request) {
        return lockUtil.executeWithAccountLock(accountId, () -> processWithdraw(accountId, request));
    }

    @Transactional
    protected TransactionResponseDto processWithdraw(Long accountId, TransactionRequestDto request) {
        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + accountId));

        validateAccountActive(account);

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance in account: " + accountId);
        }

        BigDecimal newBalance = account.getBalance().subtract(request.getAmount());
        account.setBalance(newBalance);
        accountRepository.save(account);

        String txRef = "WDR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        LedgerEntryEntity ledgerEntry = ledgerService.recordEntry(
                txRef,
                account.getId(),
                TransactionType.WITHDRAWAL,
                request.getAmount(),
                newBalance,
                request.getDescription() != null ? request.getDescription() : "Account withdrawal"
        );

        eventProducer.publishTransactionEvent(transactionEventMapper.toEvent(ledgerEntry));

        return TransactionResponseDto.builder()
                .transactionReference(txRef)
                .accountId(account.getId())
                .transactionType(TransactionType.WITHDRAWAL)
                .amount(request.getAmount())
                .balanceAfter(newBalance)
                .description(ledgerEntry.getDescription())
                .timestamp(ledgerEntry.getCreatedAt())
                .build();
    }

    @Override
    public List<TransactionResponseDto> transfer(TransferRequestDto request) {
        if (request.getFromAccountId().equals(request.getToAccountId())) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        Long firstLockId = Math.min(request.getFromAccountId(), request.getToAccountId());
        Long secondLockId = Math.max(request.getFromAccountId(), request.getToAccountId());

        return lockUtil.executeWithAccountLock(firstLockId, () ->
                lockUtil.executeWithAccountLock(secondLockId, () ->
                        processTransfer(request)
                )
        );
    }

    @Transactional
    protected List<TransactionResponseDto> processTransfer(TransferRequestDto request) {
        AccountEntity fromAccount = accountRepository.findById(request.getFromAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Source account not found with ID: " + request.getFromAccountId()));

        AccountEntity toAccount = accountRepository.findById(request.getToAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Destination account not found with ID: " + request.getToAccountId()));

        validateAccountActive(fromAccount);
        validateAccountActive(toAccount);

        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient funds in source account: " + fromAccount.getId());
        }

        BigDecimal fromNewBalance = fromAccount.getBalance().subtract(request.getAmount());
        fromAccount.setBalance(fromNewBalance);
        accountRepository.save(fromAccount);

        BigDecimal toNewBalance = toAccount.getBalance().add(request.getAmount());
        toAccount.setBalance(toNewBalance);
        accountRepository.save(toAccount);

        String transferRef = "TRF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        LedgerEntryEntity debitLedger = ledgerService.recordEntry(
                transferRef,
                fromAccount.getId(),
                TransactionType.TRANSFER_DEBIT,
                request.getAmount(),
                fromNewBalance,
                "Transfer to Account " + toAccount.getAccountNumber() + (request.getDescription() != null ? ": " + request.getDescription() : "")
        );

        LedgerEntryEntity creditLedger = ledgerService.recordEntry(
                transferRef,
                toAccount.getId(),
                TransactionType.TRANSFER_CREDIT,
                request.getAmount(),
                toNewBalance,
                "Transfer from Account " + fromAccount.getAccountNumber() + (request.getDescription() != null ? ": " + request.getDescription() : "")
        );

        eventProducer.publishTransactionEvent(transactionEventMapper.toEvent(debitLedger));
        eventProducer.publishTransactionEvent(transactionEventMapper.toEvent(creditLedger));

        TransactionResponseDto debitDto = TransactionResponseDto.builder()
                .transactionReference(transferRef)
                .accountId(fromAccount.getId())
                .transactionType(TransactionType.TRANSFER_DEBIT)
                .amount(request.getAmount())
                .balanceAfter(fromNewBalance)
                .description(debitLedger.getDescription())
                .timestamp(debitLedger.getCreatedAt())
                .build();

        TransactionResponseDto creditDto = TransactionResponseDto.builder()
                .transactionReference(transferRef)
                .accountId(toAccount.getId())
                .transactionType(TransactionType.TRANSFER_CREDIT)
                .amount(request.getAmount())
                .balanceAfter(toNewBalance)
                .description(creditLedger.getDescription())
                .timestamp(creditLedger.getCreatedAt())
                .build();

        return List.of(debitDto, creditDto);
    }

    private void validateAccountActive(AccountEntity account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountInactiveException("Account " + account.getId() + " is not in ACTIVE status (" + account.getStatus() + ")");
        }
    }
}
