package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderDTO;
import uk.gov.justice.laa.crime.crowncourt.entity.ReactivatedProsecutionCaseEntity;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;
import uk.gov.justice.laa.crime.crowncourt.repository.ReactivatedProsecutionCaseRepository;

@Service
@RequiredArgsConstructor
public class ReactivatedProsecutionCaseDetectionService {

  private final ReactivatedProsecutionCaseRepository reactivatedProsecutionCaseRepository;
  private final CourtDataAPIService courtDataAPIService;

  public void processCase(ProsecutionConcluded prosecutionConcluded) {

    RepOrderDTO repOrderDTO = courtDataAPIService.getRepOrder(prosecutionConcluded.getMaatId());
    if (isPreviouslyConcludedCase(repOrderDTO)) {
      reactivatedProsecutionCaseRepository.save(ReactivatedProsecutionCaseEntity.builder().build());
    }
  }

  private boolean isPreviouslyConcludedCase(RepOrderDTO repOrderDTO) {
    return repOrderDTO != null;
  }
}
