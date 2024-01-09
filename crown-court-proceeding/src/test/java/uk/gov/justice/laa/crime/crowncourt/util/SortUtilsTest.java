package uk.gov.justice.laa.crime.crowncourt.util;


import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.crime.util.SortUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


class SortUtilsTest {
    @Test
    void testSortUtilConstructorIsPrivate() throws NoSuchMethodException {
        assertThat(SortUtils.class.getDeclaredConstructors()).hasSize(1);
        Constructor<SortUtils> constructor = SortUtils.class.getDeclaredConstructor();
        assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
    }

    @Test
    void testSort_whenNullIsPassed_NullIsReturned() {
        List<String> list = null;
        SortUtils.sortListWithComparing(list, null, null, null);
        assertThat(list).isNull();
    }
}
