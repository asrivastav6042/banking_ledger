package com.bank.banking_core.mapper;

import com.bank.banking_core.dto.AccountResponseDto;
import com.bank.banking_core.dto.CreateAccountRequestDto;
import com.bank.banking_core.entity.AccountEntity;
import com.bank.banking_core.enums.AccountStatus;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public AccountEntity toEntity(CreateAccountRequestDto dto) {
        if (dto == null) {
            return null;
        }
        return AccountEntity.builder()
                .accountNumber(dto.getAccountNumber())
                .accountHolderName(dto.getAccountHolderName())
                .balance(dto.getInitialDeposit())
                .status(AccountStatus.ACTIVE)
                .build();
    }

    public AccountResponseDto toDto(AccountEntity entity) {
        if (entity == null) {
            return null;
        }
        return AccountResponseDto.builder()
                .id(entity.getId())
                .accountNumber(entity.getAccountNumber())
                .accountHolderName(entity.getAccountHolderName())
                .balance(entity.getBalance())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
