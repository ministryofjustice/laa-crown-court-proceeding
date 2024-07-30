package uk.gov.justice.laa.crime.crowncourt.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.laa.crime.crowncourt.entity.ReactivatedProsecutionCase;

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

    private static final String FILE_HEADER = "maat id, case URN, hearing id, previous outcome, previous outcome date, date of status change" + System.lineSeparator();
    private static final String FILE_PERMISSIONS = "rwx------";

    public File generateCsvFile(List<ReactivatedProsecutionCase> reactivatedCaseList, String fileName) throws IOException {
        File targetFile = createCsvFile(fileName);
        try (FileWriter fw = new FileWriter(targetFile, true)) {
            // Write the CSV header
            fw.append(FILE_HEADER);
            // Write each ReactivatedProsecutionCase to the CSV file
            writeCsvRows(reactivatedCaseList, fw);

        } catch (IOException exception) {
            log.error("Error creating CSV file - {}", exception.getMessage());
        }
        return targetFile;
    }

    private void writeCsvRows(List<ReactivatedProsecutionCase> reactivatedCaseList, FileWriter fw){
        reactivatedCaseList.stream()
                .map(reactivatedProsecutionCase -> reactivatedProsecutionCase.getMaatId() + ","
                        + reactivatedProsecutionCase.getCaseUrn() + ","
                        + reactivatedProsecutionCase.getHearingId() + ","
                        + reactivatedProsecutionCase.getPreviousOutcome() + ","
                        + reactivatedProsecutionCase.getPreviousOutcomeDate() + ","
                        + reactivatedProsecutionCase.getDateOfStatusChange() + System.lineSeparator())
                .forEach(reactivatedCase -> {
                    try {
                        fw.append(reactivatedCase);
                    } catch (IOException ioException) {
                        log.error("Error writing reactivated case to file  - {}", ioException.getMessage());
                    }
                });
    }

    private File createCsvFile(String fileName) throws IOException {
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString(FILE_PERMISSIONS));
        return Files.createTempFile(fileName, ".csv", attr).toFile();
    }
}
