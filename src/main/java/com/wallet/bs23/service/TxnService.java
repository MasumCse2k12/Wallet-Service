package com.wallet.bs23.service;

import com.wallet.bs23.dto.ApiResponse;
import com.wallet.bs23.dto.TransferRequest;

public interface TxnService {
    ApiResponse doTxn(TransferRequest request);
    ApiResponse release(TransferRequest request);
    ApiResponse reverse(TransferRequest request);
}
