package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.helper;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface PersistableEnum<T> {
    @JsonIgnore
    T getValue();
}
