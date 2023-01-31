package com.example.accountinit.controller;

import com.example.accountinit.aop.AccountLock;
import com.example.accountinit.dto.CancelBalance;
import com.example.accountinit.dto.QueryTransactionResponse;
import com.example.accountinit.dto.TransactionDto;
import com.example.accountinit.dto.UseBalance;
import com.example.accountinit.exception.AccountException;
import com.example.accountinit.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


/**
 * 잔액 관련 컨드롤러
 * 1. 잔액 사용
 * 2. 잔액 사용 취소
 * 3. 거래 확인
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping("/transaction/use")
    @AccountLock
    public UseBalance.Response useBalance(
            @Valid @RequestBody UseBalance.Request request
    ) throws InterruptedException {

        try {
            Thread.sleep(3000L);

            return UseBalance.Response.from(
                    transactionService.useBalance(
                            request.getUserId(),
                            request.getAccountNumber(),
                            request.getAmount()));
        } catch (AccountException e) {
            log.error("Failed to use balance.");

            transactionService.saveFailedUseTransaction(
                    request.getAccountNumber(),
                    request.getAmount()
            );
            throw e;
        }
    }

    @PostMapping("/transaction/cancel")
    @AccountLock
    public CancelBalance.Response cancelBalance(
            @Valid @RequestBody CancelBalance.Request request
    ) {

        try {
            return CancelBalance.Response.from(
                    transactionService.cancelBalance(
                            request.getTransactionId(),
                            request.getAccountNumber(),
                            request.getAmount()));
        } catch (AccountException e) {
            log.error("Failed to cancel balance.");

            transactionService.saveFailedCancelTransaction(
                    request.getAccountNumber(),
                    request.getAmount()
            );
            throw e;
        }
    }

    @GetMapping("/transaction/{transactionId}")
    public QueryTransactionResponse queryTransaction(
            @PathVariable String transactionId) {
        return QueryTransactionResponse.from(transactionService.queryTransaction(transactionId));
    }
}
