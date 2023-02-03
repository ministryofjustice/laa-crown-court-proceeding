package uk.gov.justice.laa.crime.crowncourt.prosecutionconcluded.helper;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.stream.Stream;

@Converter
public abstract class AbstractEnumConverter<T extends Enum<T> & PersistableEnum<E>, E> implements AttributeConverter<T, E> {
    private final Class<T> enumClass;

    public AbstractEnumConverter(Class<T> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public E convertToDatabaseColumn(T attribute) {
        return attribute != null ? attribute.getValue() : null;
    }

    @Override
    public T convertToEntityAttribute(E dbData) {
        if (dbData == null) return null;

        return Stream.of(enumClass.getEnumConstants())
                .filter(f -> f.getValue().equals(dbData))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
