package uk.gov.justice.laa.crime.crowncourt.enums;

/**
 * <Code>WQType</Code> list of work queue types.
 */
public enum WQType {

    COMMITTAL_QUEUE(1),
    INDICTABLE_QUEUE(2),
    CONCLUSION_QUEUE(7),
    USER_INTERVENTIONS_QUEUE(8);

    private final int value;

    /**
     * @param value enum value.
     */
    WQType(final int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }

    public static boolean isActionableQueue(int wqNumber) {
        return INDICTABLE_QUEUE.value == wqNumber
                || CONCLUSION_QUEUE.value == wqNumber;
    }
}
