package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderDTO;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtCaseType;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtTrialOutcome;
import uk.gov.justice.laa.crime.crowncourt.exception.ValidationException;
import uk.gov.justice.laa.crime.crowncourt.model.UpdateCCOutcome;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.dto.ConcludedDTO;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.helper.CrownCourtCodeHelper;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.helper.ResultCodeHelper;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;
import uk.gov.justice.laa.crime.crowncourt.service.MaatCourtDataService;

import java.util.List;

import static org.mockito.Mockito.*;
import static uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtAppealOutcome.isAppeal;

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
    private MaatCourtDataService maatCourtDataService;

    @Test
    void testWhenCaseConcludedAndImpAndWarrantIsY_thenProcessDataAsExpected() {

        //given
        List<String> hearingResultCodes = List.of("1212", "3343");
        ConcludedDTO concludedDTO = getConcludedDTO();

        //when
        RepOrderDTO repOrderDTO = RepOrderDTO.builder()
                .catyCaseType(CrownCourtCaseType.INDICTABLE.name())
                .appealTypeCode("ACV")
                .id(123)
                .build();
        when(maatCourtDataService.getRepOrder(anyInt())).thenReturn(repOrderDTO);

        String courtCode = "1212";
        when(crownCourtCodeHelper.getCode(anyString())).thenReturn(courtCode);

        when(resultCodeHelper.isImprisoned("CONVICTED", hearingResultCodes)).thenReturn("Y");
        when(resultCodeHelper.isBenchWarrantIssued("CONVICTED", hearingResultCodes)).thenReturn("Y");

        prosecutionConcludedImpl.execute(concludedDTO);

        //then
        verify(maatCourtDataService, times(1))
                .updateCrownCourtOutcome(getUpdateCCOutcome(concludedDTO.getProsecutionConcluded().getMaatId(), "Y", "Y", courtCode));

        verify(processSentencingHelper)
                .processSentencingDate(concludedDTO.getCaseEndDate(),
                        concludedDTO.getProsecutionConcluded().getMaatId(),
                        repOrderDTO.getCatyCaseType());
    }

    private UpdateCCOutcome getUpdateCCOutcome(Integer repId, String benchWarrantIssued, String imprisoned, String courtCode) {
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

        when(maatCourtDataService.getRepOrder(anyInt())).thenReturn(null);

        prosecutionConcludedImpl.execute(concludedDTO);

        verify(maatCourtDataService, never())
                .updateCrownCourtOutcome(getUpdateCCOutcome(concludedDTO.getProsecutionConcluded().getMaatId(), "Y", "Y", ""));

        verify(processSentencingHelper, never())
                .processSentencingDate(concludedDTO.getCaseEndDate(),
                        concludedDTO.getProsecutionConcluded().getMaatId(),
                        "");
    }


    @Test
    void givenMessageIsReceived_whenProsecutionConcludedImplTestIsInvoked_thenCrownCourtProcessingRepositoryIsCalled() {
        //given
        List<String> hearingResultCodes = List.of("1212", "3343");
        ConcludedDTO concludedDTO = getConcludedDTO();

        //when
        RepOrderDTO repOrderDTO = RepOrderDTO.builder()
                .catyCaseType(CrownCourtCaseType.INDICTABLE.name())
                .appealTypeCode("ACV")
                .id(123)
                .build();
        when(maatCourtDataService.getRepOrder(anyInt())).thenReturn(repOrderDTO);

        String courtCode = "1212";
        when(crownCourtCodeHelper.getCode(anyString())).thenReturn(courtCode);

        when(resultCodeHelper.isImprisoned("CONVICTED", hearingResultCodes)).thenReturn("N");
        when(resultCodeHelper.isBenchWarrantIssued("CONVICTED", hearingResultCodes)).thenReturn("N");

        prosecutionConcludedImpl.execute(concludedDTO);

        //then
        verify(maatCourtDataService)
                .updateCrownCourtOutcome(getUpdateCCOutcome(concludedDTO.getProsecutionConcluded().getMaatId(),
                        "N",
                        "N",
                        courtCode));

        verify(processSentencingHelper)
                .processSentencingDate(concludedDTO.getCaseEndDate(),
                        concludedDTO.getProsecutionConcluded().getMaatId(),
                        repOrderDTO.getCatyCaseType());

    }

    @Test
    void testWhenResultCodesAreNotValid_thenProcessWithNullExpected() {
        //given
        ConcludedDTO concludedDTO = getConcludedDTO();

        //when
        RepOrderDTO repOrderEntity = RepOrderDTO.builder()
                .catyCaseType(CrownCourtCaseType.INDICTABLE.name())
                .appealTypeCode("ACV")
                .id(123).build();
        when(maatCourtDataService.getRepOrder(anyInt())).thenReturn(repOrderEntity);

        String courtCode = "1212";
        when(crownCourtCodeHelper.getCode(anyString())).thenReturn(courtCode);

        prosecutionConcludedImpl.execute(concludedDTO);

        //then
        verify(maatCourtDataService)
                .updateCrownCourtOutcome(
                        getUpdateCCOutcome(concludedDTO.getProsecutionConcluded().getMaatId(),
                                null,
                                null,
                                courtCode));

        verify(processSentencingHelper)
                .processSentencingDate(concludedDTO.getCaseEndDate(),
                        concludedDTO.getProsecutionConcluded().getMaatId(),
                        repOrderEntity.getCatyCaseType());
    }

    @Test
    void givenOutcomeIsEmpty_whenProsecutionConcludedImplCalled_ThenExceptionThrown() {

        ConcludedDTO concludedDTO = ConcludedDTO.builder()
                .prosecutionConcluded(ProsecutionConcluded.builder().maatId(121111).build())
                .build();

        prosecutionConcludedImpl.execute(concludedDTO);

        Assertions.assertThrows(ValidationException.class, () -> isAppeal(null));
    }

    private ConcludedDTO getConcludedDTO() {
        List<String> hearingResultCodes = List.of("1212", "3343");
        return ConcludedDTO.builder()
                .calculatedOutcome(CrownCourtTrialOutcome.CONVICTED.name())
                .hearingResultCodeList(hearingResultCodes)
                .caseUrn("caseURN12")
                .ouCourtLocation("121")
                .prosecutionConcluded(ProsecutionConcluded.builder()
                        .maatId(121111)
                        .isConcluded(true)
                        .build())
                .build();
    }
}