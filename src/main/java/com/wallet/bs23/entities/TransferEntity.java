package com.wallet.bs23.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "transfers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferEntity extends BaseEntity {

    String transactionId;
    String currency;
    Integer fromAccount;
    Integer toAccount;
    BigDecimal amount;
    String status;
    Integer relatedTxnId;
    String transactionType;
}
