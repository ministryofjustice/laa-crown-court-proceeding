package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.helper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.crime.crowncourt.service.MaatCourtDataService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtTrialOutcome.*;

@ExtendWith(MockitoExtension.class)
class ResultCodeHelperTest {

    public static final List<String> HEARING_RESULT_CODES = List.of("1002");
    @InjectMocks
    private ResultCodeHelper resultCodeHelper;

    @Mock
    private MaatCourtDataService maatCourtDataService;

    @Test
    void testWhenCCOutcomeIsConvictedAndResultCodeWithNonImp_thenReturnN() {
        when(maatCourtDataService.fetchResultCodesForCCImprisonment()).thenReturn(imprisonmentResultCodes());
        String isImp = resultCodeHelper.isImprisoned(CONVICTED.getValue(), List.of("0000"));
        assertThat(isImp).isEqualTo("N");
        verify(maatCourtDataService).fetchResultCodesForCCImprisonment();
    }

    @Test
    void testWhenCCOutcomeIsPartConvictedAndResultCodeWithNonImp_thenReturnN() {
        when(maatCourtDataService.fetchResultCodesForCCImprisonment()).thenReturn(imprisonmentResultCodes());
        String isImp = resultCodeHelper.isImprisoned(PART_CONVICTED.getValue(), List.of("0000"));
        assertThat(isImp).isEqualTo("N");
    }

    @Test
    void testWhenCCOutcomeIsAquittedAndResultCodeWithNonImp_thenReturnNull() {
        String isImp = resultCodeHelper.isImprisoned(AQUITTED.getValue(), List.of("0000"));
        assertThat(isImp).isNull();
    }

    @Test
    void whenCCOutcomeIsPartConvictedAndResultCodeWithImp_thenFlagIsY() {
        when(maatCourtDataService.fetchResultCodesForCCImprisonment()).thenReturn(imprisonmentResultCodes());
        String imprisoned = resultCodeHelper.isImprisoned(PART_CONVICTED.getValue(), HEARING_RESULT_CODES);
        assertThat(imprisoned).isEqualTo("Y");
    }

    @Test
    void whenCCOutcomeIsConvictedAndResultCodeWithImp_thenFlagIsY() {
        when(maatCourtDataService.fetchResultCodesForCCImprisonment()).thenReturn(imprisonmentResultCodes());
        String imprisoned = resultCodeHelper.isImprisoned(CONVICTED.getValue(), List.of("1016"));
        assertThat(imprisoned).isEqualTo("Y");
    }

    @Test
    void whenCCOutcomeIsAquittedAndResultCodeWithImp_thenFlagIsNull() {
        String imprisoned = resultCodeHelper.isImprisoned(AQUITTED.getValue(), HEARING_RESULT_CODES);
        assertThat(imprisoned).isNull();
    }

    @Test
    void whenCCOutcomeIsConvAndResultCodeWithImp_thenBWarFlagIsY() {
        when(maatCourtDataService.fetchResultCodesForCCImprisonment()).thenReturn(null);
        assertThatThrownBy(() -> resultCodeHelper.isImprisoned("CONVICTED", HEARING_RESULT_CODES))
                .isInstanceOf(NullPointerException.class);

    }

    @Test
    void whenCCOutcomeIsAquittedAndResultCodeWithBW_thenBWarFlagIsY() {
        //when
        when(maatCourtDataService.findByCjsResultCodeIn()).thenReturn(null);

        assertThatThrownBy(() -> resultCodeHelper.isBenchWarrantIssued("AQUITTED", HEARING_RESULT_CODES))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void whenCCOutcomeIsConvictedAndResultCodeWithBW_thenReturnY() {
        when(maatCourtDataService.findByCjsResultCodeIn()).thenReturn(imprisonmentResultCodes());
        String status = resultCodeHelper.isBenchWarrantIssued(CONVICTED.getValue(), HEARING_RESULT_CODES);

        //then
        verify(maatCourtDataService).findByCjsResultCodeIn();
        assertThat(status).isEqualTo("Y");
    }


    @Test
    void whenCCOutcomeIsConvictedAndResultCodeWithBW_thenReturnNull() {
        when(maatCourtDataService.findByCjsResultCodeIn()).thenReturn(imprisonmentResultCodes());
        String status = resultCodeHelper.isBenchWarrantIssued(CONVICTED.getValue(), List.of("1111"));
        assertThat(status).isNull();
    }

    @Test
    void whenCCOutcomeIsAquittedAndResultCodeWithBW_thenReturnY() {
        when(maatCourtDataService.findByCjsResultCodeIn()).thenReturn(imprisonmentResultCodes());
        String status = resultCodeHelper.isBenchWarrantIssued(AQUITTED.getValue(), HEARING_RESULT_CODES);
        assertThat(status).isEqualTo("Y");
    }

    @Test
    void whenCCOutcomeIsAquittedAndResultCodeWithBW_thenBWarFlagIsNull() {
        when(maatCourtDataService.findByCjsResultCodeIn()).thenReturn(imprisonmentResultCodes());
        String status = resultCodeHelper.isBenchWarrantIssued(AQUITTED.getValue(), List.of("2222"));
        assertThat(status).isNull();
    }

    @Test
    void whenCCOutcomeIsPartCAndResultCodeWithBW_thenReturnY() {
        when(maatCourtDataService.findByCjsResultCodeIn()).thenReturn(imprisonmentResultCodes());
        String status = resultCodeHelper.isBenchWarrantIssued(PART_CONVICTED.getValue(), List.of("1016"));
        assertThat(status).isEqualTo("Y");
    }

    @Test
    void whenCCOutcomeIsPartCAndResultCodeWithBW_thenBWarFlagIsNull() {
        when(maatCourtDataService.findByCjsResultCodeIn()).thenReturn(imprisonmentResultCodes());
        String status = resultCodeHelper.isBenchWarrantIssued(PART_CONVICTED.getValue(), List.of("4545"));
        assertThat(status).isNull();
    }

    private List<Integer> imprisonmentResultCodes() {
        return List.of(1002, 1024, 1003, 1007, 1016, 1002, 1081);
    }
}