package com.wallet.bs23.service;

import com.wallet.bs23.dto.ApiResponse;
import com.wallet.bs23.dto.TransferRequest;
import com.wallet.bs23.entities.Accounts;
import com.wallet.bs23.entities.TransferEntity;
import com.wallet.bs23.repository.AccountsRepository;
import com.wallet.bs23.repository.TransferRepository;
import com.wallet.bs23.utils.Constants;
import lombok.extern.log4j.Log4j2;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Log4j2
public class TxnServiceImpl implements TxnService {

    private final TransferRepository transferRepository;
    private final AccountsRepository accountsRepository;
    private final RedissonClient redissonClient;

    @Autowired
    public TxnServiceImpl(TransferRepository transferRepository,
                          AccountsRepository accountsRepository,
                          RedissonClient redissonClient) {
        this.transferRepository = transferRepository;
        this.accountsRepository = accountsRepository;
        this.redissonClient = redissonClient;
    }

    @Override
    public ApiResponse doTxn(TransferRequest request) {

        log.info("doTxn request = {} ", request.toString());

        //TODO: wallet limit, txn permissions

        boolean isLocked = false;
        RLock redisLock = redissonClient.getLock(Constants.WALLET_DEBIT_LOCK);

        try {
            isLocked = redisLock.tryLock(30, TimeUnit.SECONDS);

            Optional<Accounts> fromAccounts = accountsRepository.findByAccountName(request.getWalletId());
            Optional<Accounts> toAccounts = accountsRepository.findByAccountName(Constants.SYSTEM);

            if (!fromAccounts.isPresent() || !toAccounts.isPresent())
                return buildApiResponse("Accounts not found!", "404", Constants.FAILED);

            //check user balance
            BigDecimal balance = transferRepository.findBalanceByAccountId(fromAccounts.get().getId());
            if (balance != null && balance.compareTo(request.getAmount()) > 0) {
                initiateEscrow(request, fromAccounts.get(), toAccounts.get());
            } else {
                log.error("balance is less than request amount ");
                return buildApiResponse("Insufficient Balance", "500", Constants.FAILED);
            }
        } catch (Exception e) {
            log.error("doTxn exception = {} ", e.getMessage());
            return buildApiResponse("Internal Server error!", "500", Constants.FAILED);
        } finally {
            if (isLocked && redisLock.isHeldByCurrentThread()) {
                redisLock.unlock();
            }
        }

        return buildApiResponse("Success", "200", Constants.SUCCESS);
    }

    private ApiResponse buildApiResponse(String msg, String code, String status) {

        return ApiResponse.builder()
                .message(msg)
                .code(code)
                .status(status)
                .build();
    }


    private void initiateEscrow(TransferRequest request, Accounts fromAccounts, Accounts toAccounts) {

        TransferEntity transferEntity = buildTransferEntity(request.getTransactionId(), fromAccounts.getId(), toAccounts.getId(),
                request.getAmount(), Constants.PENDING, request.getCurrency(),  request.getTransactionType(), null);
        transferEntity.setCreatedDate(Date.valueOf(LocalDate.now()));
        transferRepository.save(transferEntity);
        log.info("initiateEscrow successfully!");
    }

    private TransferEntity buildTransferEntity(String  transactionId,
                                               Integer fromAccount,
                                               Integer toAccount,
                                               BigDecimal amount,
                                               String status,
                                               String currency,
                                               String transactionType,
                                               Integer relatedTxnId) {
        return TransferEntity.builder()
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .amount(amount)
                .currency(currency)
                .transactionId(transactionId)
                .status(status)
                .relatedTxnId(relatedTxnId)
                .transactionType(transactionType)
                .build();
    }

    @Override
    public ApiResponse release(String  transactionId) {

        try {
            Optional<TransferEntity> transfer = transferRepository.findByTransactionId(transactionId);
            if (transfer.isPresent()) {
                if (Constants.COMPLETED.equals(transfer.get().getStatus())) {
                    log.error("transfer is already released!");
                    return buildApiResponse("Transfer is already released!", "500", Constants.FAILED);
                }

                Optional<Accounts> toAccounts = accountsRepository.findByAccountName(Constants.BANK_SETTLEMENT_ACCOUNT);

                if (!toAccounts.isPresent())
                    return buildApiResponse("Accounts not found!", "404", Constants.FAILED);

                releaseTxn(transfer.get(), transactionId, transfer.get().getToAccount(), toAccounts.get().getId());
            } else {
                log.error("no transaction found for given transaction id {}", transactionId);
                return buildApiResponse("No transaction found", "500", Constants.FAILED);
            }
        } catch (Exception ex) {
            log.error("release transaction exception = {} ", ex.getMessage());
            return buildApiResponse("Internal Server error!", "500", Constants.FAILED);
        }

        return buildApiResponse("Success", "200", Constants.SUCCESS);
    }

    @Transactional
    public void releaseTxn(TransferEntity transfer, String  transactionId, Integer fromAccount, Integer toAccount) {
        TransferEntity entity = buildTransferEntity(transactionId, fromAccount, toAccount,
                transfer.getAmount(), Constants.COMPLETED, transfer.getCurrency(), transfer.getTransactionType(), transfer.getId());
        entity.setCreatedDate(Date.valueOf(LocalDate.now()));
        transferRepository.save(entity);

        log.info("Released txn id {} ", entity.getId());

        //update main transaction status
        transfer.setStatus(Constants.RELEASED);
    }


    @Override
    public ApiResponse reverse(String  transactionId) {
        try {
            Optional<TransferEntity> transfer = transferRepository.findByTransactionId(transactionId);
            if (transfer.isPresent()) {
                if (Constants.COMPLETED.equals(transfer.get().getStatus())) {
                    log.error("transfer is already reversed!");
                    return buildApiResponse("Transfer is already reversed!", "500", Constants.FAILED);
                }


                reverseTxn(transfer.get(), transactionId, transfer.get().getFromAccount(), transfer.get().getToAccount());
            } else {
                log.error("No reverse transaction found for given transaction id {}", transactionId);
                return buildApiResponse("No transaction found", "500", Constants.FAILED);
            }
        } catch (Exception ex) {
            log.error("Reverse transaction exception = {} ", ex.getMessage());
            return buildApiResponse("Internal Server error!", "500", Constants.FAILED);
        }

        return buildApiResponse("Success", "200", Constants.SUCCESS);
    }


    @Transactional
    public void reverseTxn(TransferEntity transfer, String  transactionId, Integer fromAccount, Integer toAccount) {
        TransferEntity entity = buildTransferEntity(transactionId, fromAccount, toAccount,
                transfer.getAmount(), Constants.COMPLETED, transfer.getCurrency(), transfer.getTransactionType(), transfer.getId());
        entity.setCreatedDate(Date.valueOf(LocalDate.now()));

        transferRepository.save(entity);

        log.info("REVERED txn id {} ", entity.getId());

        //update main transaction status
        transfer.setStatus(Constants.REVERSED);
    }

}
