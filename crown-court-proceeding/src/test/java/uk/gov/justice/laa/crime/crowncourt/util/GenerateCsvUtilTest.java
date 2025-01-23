package uk.gov.justice.laa.crime.crowncourt.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.Set;


import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class GenerateCsvUtilTest {
    
    @Test
    void testGenerateCsvFile() throws IOException {
        // Create mock data
        String headings = "maat id, case URN, hearing id, previous outcome, previous outcome date, date of status change";
        String case1 = "123,URN123,HID123,Outcome1,2024-05-05T05:05,2024-06-05T05:05";
        String case2 = "456,URN456,HID456,Outcome2,2024-02-01T05:05,2024-06-05T05:05";
        List<String> lines = List.of(headings, case1, case2);

        // Call the method to test
        File csvFile = GenerateCsvUtil.generateCsvFile(lines,"testfile");

        // Verify file exists
        assertTrue(csvFile.exists(), "CSV file should exist");

        // Verify file permissions
        Set<PosixFilePermission> expectedPermissions = PosixFilePermissions.fromString("rwx------");
        Set<PosixFilePermission> actualPermissions = Files.getPosixFilePermissions(csvFile.toPath());
        assertEquals(expectedPermissions, actualPermissions, "File permissions should match");

        // Verify file content
        List<String> rows = Files.readAllLines(csvFile.toPath());
        assertEquals(3, rows.size(), "CSV file should have three rows (header + 2 data rows)");
        assertEquals("maat id, case URN, hearing id, previous outcome, previous outcome date, date of status change", rows.get(0), "Header should match");
        assertEquals("123,URN123,HID123,Outcome1,2024-05-05T05:05,2024-06-05T05:05", rows.get(1), "First row should match");
        assertEquals("456,URN456,HID456,Outcome2,2024-02-01T05:05,2024-06-05T05:05", rows.get(2), "Second row should match");
        Files.deleteIfExists(csvFile.toPath());
    }


    @Test
    void testGenerateCsvFileWithIOException() {
        Mockito.mockStatic(GenerateCsvUtil.class).when(() -> GenerateCsvUtil.generateCsvFile(anyList(), anyString())).thenThrow(new IOException("Error creating CSV file - Test IO Exception"));
        assertThatThrownBy(() -> GenerateCsvUtil.generateCsvFile(anyList(), anyString()))
                .isInstanceOf(IOException.class)
                .hasMessage("Error creating CSV file - Test IO Exception");
    }
}
