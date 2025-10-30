package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.client;

import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.OffenceDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderCCOutcomeDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.WQHearingDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.WQLinkRegisterDTO;
import uk.gov.justice.laa.crime.crowncourt.model.UpdateCCOutcome;
import uk.gov.justice.laa.crime.crowncourt.model.UpdateSentenceOrder;

import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PutExchange;

@HttpExchange
public interface MaatCourtDataNonServletApiClient {

    @GetExchange("/rep-orders/{repId}")
    RepOrderDTO getRepOrderByRepId(@PathVariable Integer repId);

    @PutExchange("/crown-court/updateCCOutcome")
    void updateCrownCourtOutcome(@RequestBody UpdateCCOutcome request);

    @GetExchange("/wq-hearing/{hearingUUID}/maatId/{maatId}")
    List<WQHearingDTO> getWorkQueueHearing(@PathVariable String hearingUUID, @PathVariable Integer maatId);

    @GetExchange("/wq-link-register/{maatId}")
    List<WQLinkRegisterDTO> getWorkQueueLinkRegister(@PathVariable Integer maatId);

    @GetExchange("/offence/case/{caseId}")
    List<OffenceDTO> getOffenceByCaseId(@PathVariable Integer caseId);

    @GetExchange("/offence/{offenceId}/case/{caseId}")
    Integer getOffenceNewOffenceCount(@PathVariable String offenceId, @PathVariable Integer caseId);

    @GetExchange("/wq-offence/{offenceId}/case/{caseId}")
    Integer getWorkQueueOffenceCount(@PathVariable String offenceId, @PathVariable Integer caseId);

    @GetExchange("/xlat-result/wqType/{wqType}/subType/{subType}")
    List<Integer> getResultsByWorkQueueTypeSubType(@PathVariable Integer wqType, @PathVariable Integer subType);

    @GetExchange("/result/caseId/{caseId}/asnSeq/{asnSeq}")
    List<Integer> getResultCodeByCaseIdAndAsnSeq(@PathVariable Integer caseId, @PathVariable String asnSeq);

    @GetExchange("/wq-result/caseId/{caseId}/asnSeq/{asnSeq}")
    List<Integer> getWorkQueueResultCodeByCaseIdAndAsnSeq(@PathVariable Integer caseId, @PathVariable String asnSeq);

    @GetExchange("/xlat-result/cc-imprisonment")
    List<Integer> getResultCodesForCrownCourtImprisonment();

    @GetExchange("/xlat-result/cc-bench-warrant")
    List<Integer> getResultCodesForCrownCourtBenchWarrantUrl();

    @PutExchange("/crown-court/update-appeal-cc-sentence")
    void updateAppealSentenceOrderDate(@RequestBody UpdateSentenceOrder request);

    @PutExchange("/crown-court/update-cc-sentence")
    void updateSentenceOrderDate(@RequestBody UpdateSentenceOrder request);

    @GetExchange("/reservations/{maatId}")
    Boolean isMaatRecordLocked(@PathVariable Integer maatId);

    @GetExchange("/rep-orders/cc-outcome/reporder/{repId}")
    List<RepOrderCCOutcomeDTO> getRepOrderCCOutcomeByRepId(@PathVariable Integer repId);
}
