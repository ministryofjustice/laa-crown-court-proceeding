package uk.gov.justice.laa.crime.crowncourt.prosecutionconcluded.helper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.OffenceDTO;
import uk.gov.justice.laa.crime.crowncourt.enums.WQType;
import uk.gov.justice.laa.crime.crowncourt.prosecutionconcluded.model.OffenceSummary;
import uk.gov.justice.laa.crime.crowncourt.repository.*;
import uk.gov.justice.laa.crime.crowncourt.service.MaatCourtDataService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static uk.gov.justice.laa.crime.crowncourt.constants.CourtDataConstants.*;


@RequiredArgsConstructor
@Component
public class OffenceHelper {

    private final WQResultRepository wqResultRepository;
    private final ResultRepository resultRepository;
    private final XLATResultRepository xlatResultRepository;
    private final WQOffenceRepository wqOffenceRepository;
    private final MaatCourtDataService maatCourtDataService;

    public List<OffenceSummary> getTrialOffences(List<OffenceSummary> offenceList, int maatId) {

        List<OffenceSummary> list = new ArrayList<>();
        int caseId = maatCourtDataService.findWQLinkRegisterByMaatId(maatId);

        if (caseId != 0) {
            List<OffenceDTO> offenceEntities = maatCourtDataService.findOffenceByCaseId(caseId);
            List<Integer> committalForTrialRefResults = xlatResultRepository.findResultsByWQType(WQType.COMMITTAL_QUEUE.value(),
                    COMMITTAL_FOR_TRIAL_SUB_TYPE);
            List<Integer> committalForSentenceRefResults = xlatResultRepository.findResultsByWQType(WQType.COMMITTAL_QUEUE.value(),
                    COMMITTAL_FOR_SENTENCE_SUB_TYPE);

            for (OffenceSummary offence : offenceList) {
                if ((hasCommittalResults(offence, offenceEntities, committalForTrialRefResults))
                        || isNewCCOffence(offence, offenceEntities, committalForSentenceRefResults, caseId)) {
                    list.add(offence);
                }
            }
        }
        return list;
    }


    private boolean isNewCCOffence(OffenceSummary offence, List<OffenceDTO> offenceEntities, List<Integer> committalForSentenceRefResults, int caseId) {
        boolean isNewCCOffence = false;

        int newOffenceCount = maatCourtDataService.getNewOffenceCount(caseId, offence.getOffenceId().toString())
                + wqOffenceRepository.getNewOffenceCount(caseId, offence.getOffenceId().toString());
        //TODO

        if (newOffenceCount > 0 &&
                !hasCommittalResults(offence, offenceEntities, committalForSentenceRefResults)) {
            isNewCCOffence = true;
        }
        return isNewCCOffence;
    }


    private boolean hasCommittalResults(OffenceSummary offence, List<OffenceDTO> offenceEntities, List<Integer> committalRefResults) {
        OffenceDTO offenceEntity = offenceEntities
                .stream()
                .filter(o -> o.getOffenceId() != null
                        && o.getOffenceId().equalsIgnoreCase(offence.getOffenceId().toString()))
                .findFirst().orElse(null);

        return hasResults(offenceEntity, committalRefResults);
    }


    private boolean hasResults(OffenceDTO offenceEntity, List<Integer> committalRefResults) {
        boolean isCommittal = false;
        if (offenceEntity != null) {
            String asnSeq = getAsnSeq(offenceEntity);
            List<Integer> resultList = resultRepository
                    .findResultCodeByCaseIdAndAsnSeq(offenceEntity.getCaseId(), asnSeq);
            //TODO
            List<Integer> wqResultList = wqResultRepository
                    .findResultCodeByCaseIdAndAsnSeq(offenceEntity.getCaseId(), asnSeq);
            //TODO

            isCommittal = Stream.concat(resultList.stream(), wqResultList.stream())
                    .anyMatch(committalRefResults::contains);
        }
        return isCommittal;
    }

    private String getAsnSeq(OffenceDTO offenceEntity) {
        int asnSeq = Integer.parseInt(offenceEntity.getAsnSeq());
        return asnSeq < 9 ? offenceEntity.getAsnSeq().substring(2) : offenceEntity.getAsnSeq().substring(1);
    }


    public boolean isNewOffence(Integer caseId, String asaSeq) {
        List<OffenceDTO> offenceEntities = maatCourtDataService.findOffenceByCaseId(caseId);
        for (OffenceDTO offenceDTO : offenceEntities) {
            if (asaSeq.equals(offenceDTO.getAsnSeq())) {
                return false;
            }
        }
        return true;
    }
}
