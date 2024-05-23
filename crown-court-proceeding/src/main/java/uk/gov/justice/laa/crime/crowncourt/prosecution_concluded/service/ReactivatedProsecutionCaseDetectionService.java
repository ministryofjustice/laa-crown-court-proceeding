package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;
import uk.gov.justice.laa.crime.crowncourt.repository.ReactivatedProsecutionCaseRepository;

@Service
@RequiredArgsConstructor
public class ReactivatedProsecutionCaseDetectionService {

  private final ReactivatedProsecutionCaseRepository reactivatedProsecutionCaseRepository;
  private final CourtDataAPIService courtDataAPIService;

  public void processCase(ProsecutionConcluded prosecutionConcluded) {
    // TODO create logic to detect reactivated cases and record in the
    // reactivatedProsecutionCaseRepository if required.
  }
}
