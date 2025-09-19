// TariffTransactionDALM.java
package com.connection.transaction.model;

import java.time.ZonedDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/** Data Access Model для тарифной транзакции */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TariffTransactionDALM {
    @NonNull protected UUID uid;
    @NonNull protected UUID tariffUid;
    @NonNull protected UUID transactionUid;
    @NonNull protected ZonedDateTime transactionDate;
    @NonNull protected ZonedDateTime expiresAt;
}