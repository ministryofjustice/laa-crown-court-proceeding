package uk.gov.justice.laa.crime.crowncourt.util;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class SortUtilsTest {

    @Test
    void testSort_whenNullIsPassed_NullIsReturned() {
        List<String> list = null;
        SortUtils.sortListWithComparing(list, null, null, null);
        assertThat(list).isNull();
    }
}
