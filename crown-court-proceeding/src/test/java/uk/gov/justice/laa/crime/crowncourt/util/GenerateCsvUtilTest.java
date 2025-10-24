package uk.gov.justice.laa.crime.crowncourt.util;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.Set;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SoftAssertionsExtension.class)
class GenerateCsvUtilTest {

    @InjectSoftAssertions
    private SoftAssertions softly;

    @Test
    void testGenerateCsvFile() throws IOException {
        // Create mock data
        String headings =
                "maat id, case URN, hearing id, previous outcome, previous outcome date, date of status change";
        String case1 = "123,URN123,HID123,Outcome1,2024-05-05T05:05,2024-06-05T05:05";
        String case2 = "456,URN456,HID456,Outcome2,2024-02-01T05:05,2024-06-05T05:05";
        List<String> lines = List.of(headings, case1, case2);

        // Call the method to test
        File csvFile = GenerateCsvUtil.generateCsvFile(lines, "testfile");

        // Verify file exists
        assertThat(csvFile).exists();

        // Verify file permissions
        Set<PosixFilePermission> expectedPermissions = PosixFilePermissions.fromString("rwx------");
        Set<PosixFilePermission> actualPermissions = Files.getPosixFilePermissions(csvFile.toPath());

        assertThat(actualPermissions).isEqualTo(expectedPermissions);

        // Verify file content
        List<String> rows = Files.readAllLines(csvFile.toPath());

        assertThat(rows.size()).isEqualTo(3);

        softly.assertThat(rows.getFirst()).isEqualTo(headings);
        softly.assertThat(rows.get(1)).isEqualTo(case1);
        softly.assertThat(rows.get(2)).isEqualTo(case2);

        Files.deleteIfExists(csvFile.toPath());
    }

    @Test
    void testGenerateCsvFileWithIOException() {
        Mockito.mockStatic(GenerateCsvUtil.class)
                .when(() -> GenerateCsvUtil.generateCsvFile(anyList(), anyString()))
                .thenThrow(new IOException("Error creating CSV file - Test IO Exception"));
        assertThatThrownBy(() -> GenerateCsvUtil.generateCsvFile(anyList(), anyString()))
                .isInstanceOf(IOException.class)
                .hasMessage("Error creating CSV file - Test IO Exception");
    }
}
