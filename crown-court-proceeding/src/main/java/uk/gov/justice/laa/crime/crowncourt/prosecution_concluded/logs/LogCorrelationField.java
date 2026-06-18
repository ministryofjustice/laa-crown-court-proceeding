package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.logs;

public enum LogCorrelationField {
    MAAT_ID("maatId"),
    LAA_TRANSACTION_ID("laaTransactionId"),
    MESSAGE_ID("MessageId");

    private final String fieldName;

    LogCorrelationField(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
