package uk.gov.justice.laa.crime.crowncourt.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.Set;

@UtilityClass
@Slf4j
public class GenerateCsvUtil {
    
    private static final String FILE_PERMISSIONS = "rwx------";

    public File generateCsvFile(List<String> lines, String fileName) throws IOException {
        File targetFile = createCsvFile(fileName);
        try (FileWriter fw = new FileWriter(targetFile, true)) {
            // Write each line to the CSV file
            writeCsvRows(lines, fw);

        } catch (IOException exception) {
            log.error("Error creating CSV file - {}", exception.getMessage());
        }
        return targetFile;
    }

    private void writeCsvRows(List<String> lines, FileWriter fw){
        lines.forEach(line -> {
                try {
                    fw.append(line + System.lineSeparator());
                } catch (IOException ioException) {
                    log.error("Error writing line to file  - {}", ioException.getMessage());
                }
            });
    }

    private File createCsvFile(String fileName) throws IOException {
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString(FILE_PERMISSIONS));
        return Files.createTempFile(fileName, ".csv", attr).toFile();
    }
}
