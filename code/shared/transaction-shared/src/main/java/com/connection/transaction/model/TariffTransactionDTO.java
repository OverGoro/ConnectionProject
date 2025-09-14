// TariffTransactionDTO.java
package com.connection.transaction.model;

import java.time.ZonedDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/** DTO для тарифной транзакции */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TariffTransactionDTO {
    @NonNull protected String uid;
    @NonNull protected String tariffUid;
    @NonNull protected String transactionUid;
    @NonNull protected ZonedDateTime transactionDate;
    @NonNull protected ZonedDateTime expiresAt;
}