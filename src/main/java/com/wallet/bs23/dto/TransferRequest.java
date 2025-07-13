package com.wallet.bs23.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.math.BigDecimal;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransferRequest implements Serializable {

    @NotBlank
    String transactionId;
    @NotBlank
    String walletId;
    @NotNull
    BigDecimal amount;
    @NotBlank
    String currency;
    @NotBlank
    String transactionType;
}
