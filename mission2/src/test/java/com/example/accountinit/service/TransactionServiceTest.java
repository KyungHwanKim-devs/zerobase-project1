package com.example.accountinit.service;

import com.example.accountinit.domain.Account;
import com.example.accountinit.domain.AccountUser;
import com.example.accountinit.domain.Transaction;
import com.example.accountinit.dto.TransactionDto;
import com.example.accountinit.exception.AccountException;
import com.example.accountinit.repository.AccountRepository;
import com.example.accountinit.repository.AccountUserRepository;
import com.example.accountinit.repository.TransactionRepository;
import com.example.accountinit.type.AccountStatus;
import com.example.accountinit.type.ErrorCode;
import com.example.accountinit.type.TransactionResultType;
import com.example.accountinit.type.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.example.accountinit.type.AccountStatus.IN_USE;
import static com.example.accountinit.type.ErrorCode.*;
import static com.example.accountinit.type.TransactionResultType.F;
import static com.example.accountinit.type.TransactionResultType.S;
import static com.example.accountinit.type.TransactionType.CANCEL;
import static com.example.accountinit.type.TransactionType.USE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    public static final Long CANCEL_AMOUNT = 1000L;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountUserRepository accountUserRepository;
    @Mock
    private AccountRepository accountRepository;


    @InjectMocks
    private TransactionService transactionService;

    @Test
    void successUseBalance() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("Pobi")
                .build();
        Account account = Account.builder()
                .accountUser(user)
                .accountStatus(IN_USE)
                .balance(10000L)
                .accountNumber("1000000015")
                .build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        given(transactionRepository.save(any()))
                .willReturn(Transaction.builder()
                        .account(account)
                        .transactionType(USE)
                        .transactionResultType(S)
                        .transactionId("tId")
                        .transactionAt(LocalDateTime.now())
                        .balanceSnapshot(9000L)
                        .amount(1000L)
                        .build());

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        // when
        TransactionDto transactionDto = transactionService.useBalance(
                1L,
                "1000000000",
                200L); // captor 안쓰면 이값은 실제로 영향을 안미침

        // then
        verify(transactionRepository, times(1)).save(captor.capture());
        assertEquals(200L, captor.getValue().getAmount());
        assertEquals(9800L, captor.getValue().getBalanceSnapshot());
        assertEquals(1000L, transactionDto.getAmount());
        assertEquals(9000L, transactionDto.getBalanceSnapshot());
        assertEquals(USE, transactionDto.getTransactionType());
        assertEquals(S, transactionDto.getTransactionResultType());
    }

    @Test
    @DisplayName("해당 유저 없음 - 잔액 사용 실패")
    void useBalance_userNotFound() {
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.useBalance(
                        1L,
                        "1000000000",
                        1000L
                ));

        // then
        assertEquals(USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("해당 계좌 없음 - 잔약 사용 실패")
    void deleteAccount_AccountNotFound() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("Pobi")
                .build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());
        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.useBalance(
                        1L,
                        "1000000000"
                        , 1000L
                ));

        // then
        assertEquals(ACCOUNT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌 소유주 다름 - 잔액 사용 실패")
    void deleteAccountFailed_userUnMatch() {
        // given
        AccountUser pobi = AccountUser.builder()
                .id(12L)
                .name("Pobi")
                .build();
        AccountUser harry = AccountUser.builder()
                .id(13L)
                .name("Harry")
                .build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(pobi));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(harry)
                        .balance(0L)
                        .accountNumber("1000000012")
                        .build()));

        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.useBalance(
                        1L,
                        "1000000015",
                        1000L
                ));

        // then
        assertEquals(USER_ACCOUNT_UN_MATCH, exception.getErrorCode());
    }

    @Test
    @DisplayName("해지 계좌는 사용할 수 없다.")
    void deleteAccountFailed_alreadyUnregistered() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("Pobi")
                .build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(user)
                        .accountStatus(AccountStatus.UNREGISTERED)
                        .balance(0L)
                        .accountNumber("1000000012")
                        .build()));
        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.useBalance(
                        1L,
                        "1000000015",
                        1000L
                ));
        // then
        assertEquals(ACCOUNT_ALREADY_UNREGISTERED, exception.getErrorCode());
    }

    @Test
    @DisplayName("거래 금액이 잔액보다 큰 경우")
    void exceedAmount_UseBalance() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("Pobi")
                .build();
        Account account = Account.builder()
                .accountUser(user)
                .accountStatus(IN_USE)
                .balance(100L)
                .accountNumber("1000000012")
                .build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));


        // when

        // then
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.useBalance(
                        1L,
                        "1000000000",
                        1000L
                ));
        assertEquals(AMOUNT_EXCEED_BALANCE, exception.getErrorCode());
        verify(transactionRepository, times(0)).save(any());
    }

    @Test
    @DisplayName("실패 트랜잭션 저장 성공")
    void saveFailedUseTransaction() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("Pobi")
                .build();
        Account account = Account.builder()
                .accountUser(user)
                .accountStatus(IN_USE)
                .balance(10000L)
                .accountNumber("1000000012")
                .build();

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));
        given(transactionRepository.save(any()))
                .willReturn(Transaction.builder()
                        .account(account)
                        .transactionType(USE)
                        .transactionResultType(S)
                        .transactionId("tId")
                        .transactionAt(LocalDateTime.now())
                        .amount(1000L)
                        .balanceSnapshot(9000L)
                        .build()
                );
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        // when
        transactionService.saveFailedUseTransaction(
                "1000000012",
                1000L
        );

        // then
        verify(transactionRepository, times(1)).save(captor.capture());
        assertEquals(1000L, captor.getValue().getAmount());
        assertEquals(10000L, captor.getValue().getBalanceSnapshot());
        assertEquals(F, captor.getValue().getTransactionResultType());
    }

    @Test
    void successCancelBalance() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("Pobi")
                .build();
        Account account = Account.builder()
                .accountUser(user)
                .accountStatus(IN_USE)
                .balance(10000L)
                .accountNumber("1000000015")
                .build();
        // 원래의 거래
        Transaction transaction = Transaction.builder()
                .account(account)
                .transactionType(USE)
                .transactionResultType(S)
                .transactionId("tId")
                .transactionAt(LocalDateTime.now())
                .amount(CANCEL_AMOUNT)
                .balanceSnapshot(9000L)
                .build();

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));
        given(transactionRepository.save(any()))
                .willReturn(Transaction.builder()
                        .account(account)
                        .transactionType(CANCEL)
                        .transactionResultType(S)
                        .transactionId("tIdFroCancel")
                        .transactionAt(LocalDateTime.now())
                        .amount(CANCEL_AMOUNT)
                        .balanceSnapshot(10000L)
                        .build());

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        // when
        TransactionDto transactionDto = transactionService.cancelBalance(
                "tId",
                "1000000000",
                CANCEL_AMOUNT); // captor 안쓰면 이값은 실제로 영향을 안미침

        // then
        verify(transactionRepository, times(1)).save(captor.capture());
        assertEquals(CANCEL_AMOUNT, captor.getValue().getAmount());
        assertEquals(10000L + CANCEL_AMOUNT, captor.getValue().getBalanceSnapshot());
        assertEquals(S, transactionDto.getTransactionResultType());
        assertEquals(CANCEL, transactionDto.getTransactionType());
        assertEquals(10000L, transactionDto.getBalanceSnapshot());
        assertEquals(CANCEL_AMOUNT, transactionDto.getAmount());
    }

    @Test
    @DisplayName("해당 계좌 없음 - 잔약 사용 취소 실패")
    void cancelTransaction_AccountNotFound() {
        // given
        // 원래의 거래
        Transaction transaction = Transaction.builder().build();

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());

        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.cancelBalance(
                        "tId",
                        "1000000000"
                        , 1000L
                ));

        // then
        assertEquals(ACCOUNT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("원래의 사용 거래 없음 - 잔약 사용 취소 실패")
    void cancelTransaction_TransactionNotFound() {
        // given
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.empty());


        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.cancelBalance(
                        "tId",
                        "1000000000"
                        , 1000L
                ));

        // then
        assertEquals(TRANSACTION_NOT_FOUND, exception.getErrorCode());
    }


    @Test
    @DisplayName("거래와 계좌가 매칭 실패 - 잔액 사용 취소 실패")
    void cancelTransaction_TransactionAccountUnMatch() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("Pobi")
                .build();
        Account account = Account.builder()
                .id(1L)
                .accountUser(user)
                .accountStatus(IN_USE)
                .balance(10000L)
                .accountNumber("1000000015")
                .build();
        Account accountNotUser = Account.builder()
                .id(2L)
                .accountUser(user)
                .accountStatus(IN_USE)
                .balance(10000L)
                .accountNumber("1000000019")
                .build();
        // 원래의 거래
        Transaction transaction = Transaction.builder()
                .account(account)
                .transactionType(USE)
                .transactionResultType(S)
                .transactionId("tId")
                .transactionAt(LocalDateTime.now())
                .amount(CANCEL_AMOUNT)
                .balanceSnapshot(9000L)
                .build();

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(accountNotUser));

        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.cancelBalance(
                        "tId",
                        "1000000000"
                        , CANCEL_AMOUNT
                ));

        // then
        assertEquals(TRANSACTION_ACCOUNT_UN_MATCH, exception.getErrorCode());
    }

    @Test
    @DisplayName("거래 금액과 취소 금액이 다름 - 잔액 사용 취소 실패")
    void cancelTransaction_cancelMustFully() {

        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("Pobi")
                .build();
        Account account = Account.builder()
                .id(1L)
                .accountUser(user)
                .accountStatus(IN_USE)
                .balance(10000L)
                .accountNumber("1000000015")
                .build();
        // 원래의 거래
        Transaction transaction = Transaction.builder()
                .account(account)
                .transactionType(USE)
                .transactionResultType(S)
                .transactionId("tId")
                .transactionAt(LocalDateTime.now())
                .amount(CANCEL_AMOUNT + 1_000L)
                .balanceSnapshot(9000L)
                .build();

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.cancelBalance(
                        "tId",
                        "1000000000"
                        , CANCEL_AMOUNT
                ));

        // then
        assertEquals(CANCEL_MUST_FULLY, exception.getErrorCode());
    }

    @Test
    @DisplayName("취소는 1년 까지만 가능 - 잔액 사용 취소 실패")
    void cancelTransaction_TOO_OLD_ORDER_TO_CANCEL() {

        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("Pobi")
                .build();
        Account account = Account.builder()
                .id(1L)
                .accountUser(user)
                .accountStatus(IN_USE)
                .balance(10000L)
                .accountNumber("1000000015")
                .build();
        // 원래의 거래
        Transaction transaction = Transaction.builder()
                .account(account)
                .transactionType(USE)
                .transactionResultType(S)
                .transactionId("tId")
                .transactionAt(LocalDateTime.now().minusYears(1).minusDays(1))
                .amount(CANCEL_AMOUNT)
                .balanceSnapshot(9000L)
                .build();

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.cancelBalance(
                        "tId",
                        "1000000000"
                        , CANCEL_AMOUNT
                ));

        // then
        assertEquals(TOO_OLD_ORDER_TO_CANCEL, exception.getErrorCode());
    }

    @Test
    void successQueryTransaction() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("Pobi")
                .build();
        Account account = Account.builder()
                .id(1L)
                .accountUser(user)
                .accountStatus(IN_USE)
                .balance(10000L)
                .accountNumber("1000000015")
                .build();
        // 원래의 거래
        Transaction transaction = Transaction.builder()
                .account(account)
                .transactionType(USE)
                .transactionResultType(S)
                .transactionId("txId")
                .transactionAt(LocalDateTime.now().minusYears(1).minusDays(1))
                .amount(CANCEL_AMOUNT)
                .balanceSnapshot(9000L)
                .build();

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));
        // when

        TransactionDto transactionDto = transactionService.queryTransaction("txId");

        // then
        assertEquals(USE, transactionDto.getTransactionType());
        assertEquals(S, transactionDto.getTransactionResultType());
        assertEquals(CANCEL_AMOUNT, transactionDto.getAmount());
        assertEquals("txId", transactionDto.getTransactionId());
    }

    @Test
    @DisplayName("원거래 없음 - 거래 조회 실패")
    void queryTransaction_TransactionNotFound() {
        // given
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.empty());


        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.queryTransaction(
                        "tId"
                ));

        // then
        assertEquals(TRANSACTION_NOT_FOUND, exception.getErrorCode());
    }
}