package com.wallet.bs23.service;

import com.wallet.bs23.dto.ApiResponse;
import com.wallet.bs23.dto.TransferRequest;

public interface TxnService {
    ApiResponse doTxn(TransferRequest request);
    ApiResponse release(String  transactionId);
    ApiResponse reverse(String transactionId);
}
