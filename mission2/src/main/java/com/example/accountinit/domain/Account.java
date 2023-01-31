package com.example.accountinit.domain;

import com.example.accountinit.exception.AccountException;
import com.example.accountinit.type.AccountStatus;
import com.example.accountinit.type.ErrorCode;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

import static com.example.accountinit.type.ErrorCode.AMOUNT_EXCEED_BALANCE;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Account extends BaseEntity{

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private AccountUser accountUser;

    private String accountNumber;

    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus;

    private Long balance;

    private LocalDateTime registeredAt;

    private LocalDateTime unRegisteredAt;

    public void useBalance(Long amount) {
        if (amount > balance) {
            throw new AccountException(AMOUNT_EXCEED_BALANCE);
        }
        balance -= amount;
    }

    public void cancelBalance(Long amount) {
        if (amount < 0) {
            throw new AccountException(ErrorCode.INVALID_REQUEST);
        }
        balance += amount;
    }
}
