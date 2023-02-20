package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.crime.crowncourt.model.UpdateSentenceOrder;
import uk.gov.justice.laa.crime.crowncourt.service.MaatCourtDataService;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtCaseType.APPEAL_CC;

@ExtendWith(MockitoExtension.class)
class ProcessSentencingImplTest {

    @InjectMocks
    private ProcessSentencingImpl processSentencingImpl;

    @Mock
    private MaatCourtDataService maatCourtDataService;

    @Test
    void testWhenAppealTypeCC_thenProcessInvoke() {
        doNothing().when(maatCourtDataService).invokeUpdateSentenceOrderDate(any());
        processSentencingImpl.processSentencingDate("2012-12-12", 121121, "CC");
        verify(maatCourtDataService).invokeUpdateSentenceOrderDate(any());
    }

    private UpdateSentenceOrder getUpdateSentenceOrder() {
        return UpdateSentenceOrder.builder()
                .repId(121121)
                .dbUser(null)
                .sentenceOrderDate(LocalDate.parse("2012-12-12"))
                .dateChanged(LocalDate.now())
                .build();
    }

    @Test
    void testWhenAppealTypeCC_thenProcessUpdate() {
        doNothing().when(maatCourtDataService).invokeUpdateAppealSentenceOrderDate(any());
        processSentencingImpl.processSentencingDate("2012-12-12", 121121, APPEAL_CC.getValue());
        verify(maatCourtDataService).invokeUpdateAppealSentenceOrderDate(getUpdateSentenceOrder());
    }
}