package com.wallet.bs23.repository;

import com.wallet.bs23.entities.TransferEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface TransferRepository extends JpaRepository<TransferEntity, Long> {


    @Query("""
                select (
                    (select coalesce(sum(te1.amount), 0) from TransferEntity te1 where te1.toAccount = :walletId)
                    -
                    (select coalesce(sum(te2.amount), 0) from TransferEntity te2 where te2.fromAccount = :walletId)
                )
                from TransferEntity t
                where t.id = (select min(t2.id) from TransferEntity t2)
            """)
    BigDecimal findBalanceByAccountId(@Param("walletId") Integer walletId);

    Optional<TransferEntity> findByTransactionId(String transactionId);
}
