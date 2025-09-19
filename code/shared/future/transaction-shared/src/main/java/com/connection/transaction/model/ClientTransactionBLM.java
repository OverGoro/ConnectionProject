// ClientTransactionBLM.java
package com.connection.transaction.model;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/** Business Logic Model для клиентской транзакции */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ClientTransactionBLM {
    @NonNull protected UUID uid;
    @NonNull protected UUID clientUid;
    @NonNull protected ZonedDateTime transactionDate;
    @NonNull protected BigDecimal amount;
    @NonNull protected String currencyCode;
    protected String description;
}