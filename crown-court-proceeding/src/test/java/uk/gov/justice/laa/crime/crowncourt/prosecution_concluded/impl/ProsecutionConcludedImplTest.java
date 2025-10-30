package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.impl;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.laa.crime.enums.CrownCourtAppealOutcome.isAppeal;

import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderDTO;
import uk.gov.justice.laa.crime.crowncourt.model.UpdateCCOutcome;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.dto.ConcludedDTO;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.helper.CrownCourtCodeHelper;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.helper.ResultCodeHelper;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service.CourtDataAPIService;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtCaseType;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtTrialOutcome;
import uk.gov.justice.laa.crime.exception.ValidationException;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProsecutionConcludedImplTest {

    @InjectMocks
    private ProsecutionConcludedImpl prosecutionConcludedImpl;

    @Mock
    private CrownCourtCodeHelper crownCourtCodeHelper;

    @Mock
    private ProcessSentencingImpl processSentencingHelper;

    @Mock
    private ResultCodeHelper resultCodeHelper;

    @Mock
    private CourtDataAPIService courtDataAPIService;

    @Test
    void testWhenCaseConcludedAndImpAndWarrantIsY_thenProcessDataAsExpected() {

        List<String> hearingResultCodes = List.of("1212", "3343");
        ConcludedDTO concludedDTO = getConcludedDTO();

        RepOrderDTO repOrderDTO = RepOrderDTO.builder()
                .catyCaseType(CrownCourtCaseType.INDICTABLE.name())
                .appealTypeCode("ACV")
                .id(121111)
                .build();

        String courtCode = "1212";
        when(resultCodeHelper.isImprisoned("CONVICTED", hearingResultCodes)).thenReturn("Y");
        when(resultCodeHelper.isBenchWarrantIssued("CONVICTED", hearingResultCodes))
                .thenReturn("Y");

        prosecutionConcludedImpl.execute(concludedDTO, repOrderDTO);

        // then
        verify(courtDataAPIService, times(1))
                .updateCrownCourtOutcome(getUpdateCCOutcome(
                        concludedDTO.getProsecutionConcluded().getMaatId(), "Y", "Y", courtCode));

        verify(processSentencingHelper)
                .processSentencingDate(
                        concludedDTO.getCaseEndDate(),
                        concludedDTO.getProsecutionConcluded().getMaatId(),
                        repOrderDTO.getCatyCaseType());
    }

    private UpdateCCOutcome getUpdateCCOutcome(
            Integer repId, String benchWarrantIssued, String imprisoned, String courtCode) {

        return UpdateCCOutcome.builder()
                .repId(repId)
                .ccOutcome("CONVICTED")
                .benchWarrantIssued(benchWarrantIssued)
                .appealType("ACV")
                .imprisoned(imprisoned)
                .caseNumber("caseURN12")
                .crownCourtCode(courtCode)
                .build();
    }

    @Test
    void testWhenRepOrderIsMissing_thenDoNothing() {

        ConcludedDTO concludedDTO = getConcludedDTO();

        prosecutionConcludedImpl.execute(concludedDTO, null);

        verify(courtDataAPIService, never())
                .updateCrownCourtOutcome(getUpdateCCOutcome(
                        concludedDTO.getProsecutionConcluded().getMaatId(), "Y", "Y", ""));

        verify(processSentencingHelper, never())
                .processSentencingDate(
                        concludedDTO.getCaseEndDate(),
                        concludedDTO.getProsecutionConcluded().getMaatId(),
                        "");
    }

    @Test
    void givenMessageIsReceived_whenProsecutionConcludedImplTestIsInvoked_thenCrownCourtProcessingRepositoryIsCalled() {
        List<String> hearingResultCodes = List.of("1212", "3343");
        ConcludedDTO concludedDTO = getConcludedDTO();

        RepOrderDTO repOrderDTO = RepOrderDTO.builder()
                .catyCaseType(CrownCourtCaseType.INDICTABLE.name())
                .appealTypeCode("ACV")
                .id(121111)
                .build();

        String courtCode = "1212";
        when(resultCodeHelper.isImprisoned("CONVICTED", hearingResultCodes)).thenReturn("N");
        when(resultCodeHelper.isBenchWarrantIssued("CONVICTED", hearingResultCodes))
                .thenReturn("N");

        prosecutionConcludedImpl.execute(concludedDTO, repOrderDTO);

        // then
        verify(courtDataAPIService)
                .updateCrownCourtOutcome(getUpdateCCOutcome(
                        concludedDTO.getProsecutionConcluded().getMaatId(), "N", "N", courtCode));

        verify(processSentencingHelper)
                .processSentencingDate(
                        concludedDTO.getCaseEndDate(),
                        concludedDTO.getProsecutionConcluded().getMaatId(),
                        repOrderDTO.getCatyCaseType());
    }

    @Test
    void testWhenResultCodesAreNotValid_thenProcessWithNullExpected() {
        ConcludedDTO concludedDTO = getConcludedDTO();

        RepOrderDTO repOrderDTO = RepOrderDTO.builder()
                .catyCaseType(CrownCourtCaseType.INDICTABLE.name())
                .appealTypeCode("ACV")
                .id(121111)
                .build();

        String courtCode = "1212";

        prosecutionConcludedImpl.execute(concludedDTO, repOrderDTO);

        verify(courtDataAPIService)
                .updateCrownCourtOutcome(getUpdateCCOutcome(
                        concludedDTO.getProsecutionConcluded().getMaatId(), null, null, courtCode));

        verify(processSentencingHelper)
                .processSentencingDate(
                        concludedDTO.getCaseEndDate(),
                        concludedDTO.getProsecutionConcluded().getMaatId(),
                        repOrderDTO.getCatyCaseType());
    }

    @Test
    void givenOutcomeIsEmpty_whenProsecutionConcludedImplCalled_ThenExceptionThrown() {

        ConcludedDTO concludedDTO = ConcludedDTO.builder()
                .prosecutionConcluded(
                        ProsecutionConcluded.builder().maatId(121111).build())
                .build();

        prosecutionConcludedImpl.execute(concludedDTO, null);

        Assertions.assertThrows(ValidationException.class, () -> isAppeal(null));
    }

    @Test
    void givenACaseTypeAndOutcomeIsConvicted_whenExecuteIsInvoked_ThenExceptionThrown() {

        ConcludedDTO concludedDTO = ConcludedDTO.builder()
                .prosecutionConcluded(ProsecutionConcluded.builder()
                        .maatId(TestModelDataBuilder.TEST_REP_ID)
                        .build())
                .calculatedOutcome("CONVICTED")
                .build();

        RepOrderDTO repOrderDTO = RepOrderDTO.builder()
                .catyCaseType("CONVICTED")
                .appealTypeCode("ACV")
                .id(123)
                .build();

        assertThatThrownBy(() -> prosecutionConcludedImpl.execute(concludedDTO, repOrderDTO))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Crown Court - Case type not valid for Trial");
    }

    @Test
    void givenAAppealIsCrownCourt_whenExecuteIsInvoked_ThenExceptionThrown() {

        ConcludedDTO concludedDTO = ConcludedDTO.builder()
                .prosecutionConcluded(
                        ProsecutionConcluded.builder().maatId(121111).build())
                .calculatedOutcome("UNSUCCESSFUL")
                .build();

        RepOrderDTO repOrderDTO = RepOrderDTO.builder()
                .catyCaseType("CONVICTED")
                .appealTypeCode("ACV")
                .id(123)
                .build();

        assertThatThrownBy(() -> prosecutionConcludedImpl.execute(concludedDTO, repOrderDTO))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Case type not valid for Appeal");
    }

    private ConcludedDTO getConcludedDTO() {
        List<String> hearingResultCodes = List.of("1212", "3343");
        return ConcludedDTO.builder()
                .calculatedOutcome(CrownCourtTrialOutcome.CONVICTED.name())
                .hearingResultCodeList(hearingResultCodes)
                .caseUrn("caseURN12")
                .crownCourtCode("1212")
                .prosecutionConcluded(ProsecutionConcluded.builder()
                        .maatId(121111)
                        .isConcluded(true)
                        .build())
                .build();
    }
}
