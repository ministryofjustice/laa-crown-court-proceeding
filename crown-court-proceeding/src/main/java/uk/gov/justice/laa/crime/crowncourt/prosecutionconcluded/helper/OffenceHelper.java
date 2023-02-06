package uk.gov.justice.laa.crime.crowncourt.prosecutionconcluded.helper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.OffenceDTO;
import uk.gov.justice.laa.crime.crowncourt.enums.WQType;
import uk.gov.justice.laa.crime.crowncourt.prosecutionconcluded.model.OffenceSummary;
import uk.gov.justice.laa.crime.crowncourt.service.MaatCourtDataService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static uk.gov.justice.laa.crime.crowncourt.constants.CourtDataConstants.COMMITTAL_FOR_SENTENCE_SUB_TYPE;
import static uk.gov.justice.laa.crime.crowncourt.constants.CourtDataConstants.COMMITTAL_FOR_TRIAL_SUB_TYPE;

@RequiredArgsConstructor
@Component
public class OffenceHelper {
    private final MaatCourtDataService maatCourtDataService;

    public List<OffenceSummary> getTrialOffences(List<OffenceSummary> offenceList, int maatId) {

        int caseId = maatCourtDataService.findWQLinkRegisterByMaatId(maatId);
        List<OffenceDTO> offenceDTO = maatCourtDataService.findOffenceByCaseId(caseId);
        List<Integer> committalForTrialRefResults = maatCourtDataService.findResultsByWQTypeSubType(WQType.COMMITTAL_QUEUE.value(),
                COMMITTAL_FOR_TRIAL_SUB_TYPE);
        List<Integer> committalForSentenceRefResults = maatCourtDataService.findResultsByWQTypeSubType(WQType.COMMITTAL_QUEUE.value(),
                COMMITTAL_FOR_SENTENCE_SUB_TYPE);

        List<OffenceSummary> list = new ArrayList<>();
        for (OffenceSummary offence : offenceList) {
            if ((hasCommittalResults(offence, offenceDTO, committalForTrialRefResults))
                    || isNewCCOffence(offence, offenceDTO, committalForSentenceRefResults, caseId)) {
                list.add(offence);
            }
        }
        return list;

    }

    private boolean isNewCCOffence(OffenceSummary offence, List<OffenceDTO> offenceDTO, List<Integer> committalForSentenceRefResults, int caseId) {
        boolean isNewCCOffence = false;

        int newOffenceCount = maatCourtDataService.getOffenceNewOffenceCount(caseId, offence.getOffenceId().toString())
                + maatCourtDataService.getWQOffenceNewOffenceCount(caseId, offence.getOffenceId().toString());

        if (newOffenceCount > 0 &&
                !hasCommittalResults(offence, offenceDTO, committalForSentenceRefResults)) {
            isNewCCOffence = true;
        }
        return isNewCCOffence;
    }

    private boolean hasCommittalResults(OffenceSummary offence, List<OffenceDTO> offenceEntities, List<Integer> committalRefResults) {
        OffenceDTO offenceDTO = offenceEntities
                .stream()
                .filter(o -> o.getOffenceId() != null
                        && o.getOffenceId().equalsIgnoreCase(offence.getOffenceId().toString()))
                .findFirst().orElse(null);

        return hasResults(offenceDTO, committalRefResults);
    }

    private boolean hasResults(OffenceDTO offenceDTO, List<Integer> committalRefResults) {
        boolean isCommittal = false;
        if (offenceDTO != null) {
            String asnSeq = getAsnSeq(offenceDTO);
            List<Integer> resultList = maatCourtDataService
                    .getResultCodeByCaseIdAndAsnSeq(offenceDTO.getCaseId(), asnSeq);
            List<Integer> wqResultList = maatCourtDataService
                    .getWqResultCodeByCaseIdAndAsnSeq(offenceDTO.getCaseId(), asnSeq);

            isCommittal = Stream.concat(resultList.stream(), wqResultList.stream())
                    .anyMatch(committalRefResults::contains);
        }
        return isCommittal;
    }

    private String getAsnSeq(OffenceDTO offenceDTO) {
        int asnSeq = Integer.parseInt(offenceDTO.getAsnSeq());
        return asnSeq < 9 ? offenceDTO.getAsnSeq().substring(2) : offenceDTO.getAsnSeq().substring(1);
    }

}
