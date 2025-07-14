package com.wallet.bs23.controller;

import com.wallet.bs23.dto.ApiResponse;
import com.wallet.bs23.dto.TransferRequest;
import com.wallet.bs23.service.TxnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    @PutMapping("/release")
    public ResponseEntity<?> release(@RequestParam("transactionId") String  transactionId) {
        ApiResponse response = txnService.release(transactionId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/reverse")
    public ResponseEntity<?> reverse(@RequestParam("transactionId") String  transactionId) {
        ApiResponse response = txnService.reverse(transactionId);
        return ResponseEntity.ok(response);
    }
}
