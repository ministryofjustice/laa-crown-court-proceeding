package uk.gov.justice.laa.crime.crowncourt.util;

import java.util.Optional;

import org.slf4j.MDC;
import org.springframework.messaging.MessageHeaders;

public final class LogCorrelation implements AutoCloseable {

    static final String MDC_MESSAGE_ID = "sqsMessageId";
    static final String MDC_MAAT_ID = "maatId";
    static final String MDC_TX_ID = "laaTransactionId";

    public static LogCorrelation fromHeadersAndPayload(
            final MessageHeaders headers, final Optional<Integer> maatId, final Optional<String> txId) {
        final LogCorrelation scope = new LogCorrelation();

        final Object messageId = headers.get("MessageId");
        if (messageId != null) {
            MDC.put(MDC_MESSAGE_ID, String.valueOf(messageId));
        }
        maatId.ifPresent(id -> MDC.put(MDC_MAAT_ID, String.valueOf(id)));
        txId.filter(v -> !v.isBlank()).ifPresent(v -> MDC.put(MDC_TX_ID, v));

        return scope;
    }

    @Override
    public void close() {
        MDC.remove(MDC_MESSAGE_ID);
        MDC.remove(MDC_MAAT_ID);
        MDC.remove(MDC_TX_ID);
    }
}
