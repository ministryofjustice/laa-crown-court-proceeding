package uk.gov.justice.laa.crime.crowncourt.prosecutionconcluded.helper;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface PersistableEnum<T> {
    @JsonIgnore
    T getValue();
}
