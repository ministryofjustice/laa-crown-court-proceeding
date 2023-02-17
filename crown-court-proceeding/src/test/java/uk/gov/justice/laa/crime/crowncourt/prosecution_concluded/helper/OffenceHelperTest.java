package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.helper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.OffenceDTO;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.OffenceSummary;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.Plea;
import uk.gov.justice.laa.crime.crowncourt.service.MaatCourtDataService;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OffenceHelperTest {

    @InjectMocks
    private OffenceHelper offenceHelper;
    @Mock
    private MaatCourtDataService maatCourtDataService;

    @Test
    void testWhenOffenceResultIsCommittal_thenReturnTrue() {

        when(maatCourtDataService.findOffenceByCaseId(anyInt())).thenReturn(getOffenceEntity());
        when(maatCourtDataService.findWQLinkRegisterByMaatId(anyInt())).thenReturn(456);
        when(maatCourtDataService.findResultsByWQTypeSubType(anyInt(), anyInt()))
                .thenReturn(List.of(4057, 4558, 4559, 4560, 4561, 4562, 4564, 4567, 4593, 1290));
        when(maatCourtDataService.getResultCodeByCaseIdAndAsnSeq(anyInt(), anyString()))
                .thenReturn(List.of(4057, 4558));
        when(maatCourtDataService.getWqResultCodeByCaseIdAndAsnSeq(anyInt(), anyString()))
                .thenReturn(List.of(4057, 4558));

        List<OffenceSummary> offenceSummaryList = offenceHelper.getTrialOffences(getOffenceSummary(), 12121);

        verify(maatCourtDataService).findOffenceByCaseId(anyInt());
        verify(maatCourtDataService, atLeast(2)).findResultsByWQTypeSubType(anyInt(), anyInt());
        assertEquals(1, offenceSummaryList.size());
        assertEquals(UUID.fromString("e2540d98-995f-43f2-97e4-f712b8a5d6a6"), offenceSummaryList.get(0).getOffenceId());
    }

    @Test
    void testWhenOffenceResultIsNotCommittal_thenReturnFalse() {
        when(maatCourtDataService.findOffenceByCaseId(anyInt())).thenReturn(getOffenceEntity());
        when(maatCourtDataService.findWQLinkRegisterByMaatId(anyInt())).thenReturn(456);
        when(maatCourtDataService.findResultsByWQTypeSubType(anyInt(), anyInt()))
                .thenReturn(List.of(4057, 4558, 4559, 4560, 4561, 4562, 4564, 4567, 4593, 1290));
        when(maatCourtDataService.getResultCodeByCaseIdAndAsnSeq(anyInt(), anyString()))
                .thenReturn(List.of(453, 454));
        when(maatCourtDataService.getWqResultCodeByCaseIdAndAsnSeq(anyInt(), anyString()))
                .thenReturn(List.of(6665, 6666));

        List<OffenceSummary> offenceSummaryList = offenceHelper.getTrialOffences(getOffenceSummary(), 12121);

        verify(maatCourtDataService).findOffenceByCaseId(anyInt());
        verify(maatCourtDataService, atLeast(2)).findResultsByWQTypeSubType(anyInt(), anyInt());
        assertEquals(0, offenceSummaryList.size());
    }

    private List<OffenceDTO> getOffenceEntity() {
        return
                List.of(
                        OffenceDTO.builder()
                                .offenceId("e2540d98-995f-43f2-97e4-f712b8a5d6a6")
                                .asnSeq("001")
                                .caseId(12121)
                                .applicationFlag(1)
                                .build(),
                        OffenceDTO.builder()
                                .offenceId("908ad01e-5a38-4158-957a-0c1d1a783862")
                                .asnSeq("002")
                                .caseId(12121)
                                .build());
    }

    private List<OffenceSummary> getOffenceSummary() {

        return List.of(
                OffenceSummary.builder()
                        .offenceCode("1212")
                        .offenceId(UUID.fromString("e2540d98-995f-43f2-97e4-f712b8a5d6a6"))
                        .plea(Plea.builder().value("NOT_GUILTY").pleaDate("2021-11-12").build())
                        .build()
        );
    }
}