package com.wallet.bs23.controller;

import com.wallet.bs23.dto.ApiResponse;
import com.wallet.bs23.dto.TransferRequest;
import com.wallet.bs23.service.TxnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallet")
public class TransactionController {

    private final TxnService txnService;

    @Autowired
    public TransactionController(TxnService txnService) {
        this.txnService = txnService;
    }

    @PostMapping("/transfer-to-bank")
    public ResponseEntity<?> doTxn(@Validated @RequestBody TransferRequest request) {
        ApiResponse response = txnService.doTxn(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/release")
    public ResponseEntity<?> release(@Validated @RequestBody TransferRequest request) {
        ApiResponse response = txnService.release(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reverse")
    public ResponseEntity<?> reverse(@Validated @RequestBody TransferRequest request) {
        ApiResponse response = txnService.reverse(request);
        return ResponseEntity.ok(response);
    }
}
