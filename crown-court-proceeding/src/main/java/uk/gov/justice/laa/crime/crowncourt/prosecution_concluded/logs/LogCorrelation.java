package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.logs;

import org.slf4j.MDC;
import org.springframework.messaging.MessageHeaders;

public final class LogCorrelation implements AutoCloseable {

    public static final String HEADER_MESSAGE_ID = "MessageId";

    private LogCorrelation() {}

    public static LogCorrelation fromHeaders(final MessageHeaders headers) {
        final Object messageId = headers.get(HEADER_MESSAGE_ID);
        if (messageId != null) {
            MDC.put(LogCorrelationField.MESSAGE_ID.getFieldName(), String.valueOf(messageId));
        }
        return new LogCorrelation();
    }

    public void enrichWith(CorrelationIds ids) {
        if (ids == null) {
            return;
        }
        if (ids.hasMaatId()) {
            MDC.put(LogCorrelationField.MAAT_ID.getFieldName(), String.valueOf(ids.maatId()));
        }
        if (ids.hasTxId()) {
            MDC.put(LogCorrelationField.LAA_TRANSACTION_ID.getFieldName(), ids.txId());
        }
    }

    @Override
    public void close() {
        MDC.remove(LogCorrelationField.MESSAGE_ID.getFieldName());
        MDC.remove(LogCorrelationField.MAAT_ID.getFieldName());
        MDC.remove(LogCorrelationField.LAA_TRANSACTION_ID.getFieldName());
    }
}
