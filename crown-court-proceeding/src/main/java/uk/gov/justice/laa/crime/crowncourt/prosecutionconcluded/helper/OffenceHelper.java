package uk.gov.justice.laa.crime.crowncourt.prosecutionconcluded.helper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.crime.crowncourt.entity.OffenceEntity;
import uk.gov.justice.laa.crime.crowncourt.enums.WQType;
import uk.gov.justice.laa.crime.crowncourt.prosecutionconcluded.model.OffenceSummary;
import uk.gov.justice.laa.crime.crowncourt.repository.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.justice.laa.crime.crowncourt.constants.CourtDataConstants.*;


@RequiredArgsConstructor
@Component
public class OffenceHelper {

    private final OffenceRepository offenceRepository;
    private final WQResultRepository wqResultRepository;
    private final ResultRepository resultRepository;
    private final XLATResultRepository xlatResultRepository;
    private final WQOffenceRepository wqOffenceRepository;
    private final WqLinkRegisterRepository wqLinkRegisterRepository;


    public List<OffenceSummary> getTrialOffences(List<OffenceSummary> offenceList, int maatId) {

        //int caseId = wqLinkRegisterRepository.findBymaatId(maatId).get(0).getCaseId();
        //TODO

        //List<OffenceEntity> offenceEntities = offenceRepository.findByCaseId(caseId);
        //TODO
        List<Integer> committalForTrialRefResults = xlatResultRepository.findResultsByWQType(WQType.COMMITTAL_QUEUE.value(),
                COMMITTAL_FOR_TRIAL_SUB_TYPE);
        List<Integer> committalForSentenceRefResults = xlatResultRepository.findResultsByWQType(WQType.COMMITTAL_QUEUE.value(),
                COMMITTAL_FOR_SENTENCE_SUB_TYPE);

        return offenceList
                .stream()
                .filter(offence -> (hasCommittalResults(offence, offenceEntities, committalForTrialRefResults))
                        || isNewCCOffence(offence, offenceEntities, committalForSentenceRefResults, caseId))
                .collect(Collectors.toList());

    }


    private boolean isNewCCOffence(OffenceSummary offence, List<OffenceEntity> offenceEntities, List<Integer> committalForSentenceRefResults, int caseId) {
        boolean isNewCCOffence = false;

        /*int newOffenceCount = offenceRepository.getNewOffenceCount(caseId, offence.getOffenceId().toString())
                + wqOffenceRepository.getNewOffenceCount(caseId, offence.getOffenceId().toString());*/
        //TODO

        if (newOffenceCount > 0 &&
                !hasCommittalResults(offence, offenceEntities, committalForSentenceRefResults)) {
            isNewCCOffence = true;
        }
        return isNewCCOffence;
    }


    private boolean hasCommittalResults(OffenceSummary offence, List<OffenceEntity> offenceEntities, List<Integer> committalRefResults) {
        OffenceEntity offenceEntity = offenceEntities
                .stream()
                .filter(o -> o.getOffenceId() != null
                        && o.getOffenceId().equalsIgnoreCase(offence.getOffenceId().toString()))
                .findFirst().orElse(null);

        return hasResults(offenceEntity, committalRefResults);
    }


    private boolean hasResults(OffenceEntity offenceEntity, List<Integer> committalRefResults) {
        boolean isCommittal = false;
        if (offenceEntity != null) {
            String asnSeq = getAsnSeq(offenceEntity);
            /*List<Integer> resultList = resultRepository
                    .findResultCodeByCaseIdAndAsnSeq(offenceEntity.getCaseId(), asnSeq);*/
            //TODO
            /*List<Integer> wqResultList = wqResultRepository
                    .findResultCodeByCaseIdAndAsnSeq(offenceEntity.getCaseId(), asnSeq);*/
            //TODO

            isCommittal = Stream.concat(resultList.stream(), wqResultList.stream())
                    .anyMatch(committalRefResults::contains);
        }
        return isCommittal;
    }

    private String getAsnSeq(OffenceEntity offenceEntity) {
        int asnSeq = Integer.parseInt(offenceEntity.getAsnSeq());
        return asnSeq < 9 ? offenceEntity.getAsnSeq().substring(2) : offenceEntity.getAsnSeq().substring(1);
    }


    public boolean isNewOffence(Integer caseId, String asaSeq) {

        /*Integer offenceCount =
                offenceRepository.getOffenceCountForAsnSeq(
                        caseId,
                        String.format(LEADING_ZERO_3, Integer.parseInt(asaSeq)));*/
        //TODO

        return offenceCount == 0;
    }
}
