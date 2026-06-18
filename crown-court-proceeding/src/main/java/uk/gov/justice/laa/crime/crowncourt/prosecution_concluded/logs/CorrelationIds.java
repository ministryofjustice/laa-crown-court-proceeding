package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.logs;

public record CorrelationIds(Integer maatId, String txId) {

    public static CorrelationIds empty() {
        return new CorrelationIds(null, null);
    }

    public boolean hasMaatId() {
        return maatId != null;
    }

    public boolean hasTxId() {
        return txId != null && !txId.isBlank();
    }
}
