package uk.gov.justice.laa.crime.crowncourt.builder;

import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtApplicationRequestDTO;
import uk.gov.justice.laa.crime.crowncourt.model.ApiUpdateCrownCourtApplicationRequest;

public class CrownCourtApplicationRequestDTOBuilder {
    public CrownCourtApplicationRequestDTO buildRequestDTO(final ApiUpdateCrownCourtApplicationRequest apiUpdateCrownCourtApplicationRequest) {
        return CrownCourtApplicationRequestDTO.builder()
                .repId(apiUpdateCrownCourtApplicationRequest.getRepId())
                .laaTransactionId(apiUpdateCrownCourtApplicationRequest.getLaaTransactionId())
                .userSession(apiUpdateCrownCourtApplicationRequest.getUserSession())
                .crownCourtSummary(apiUpdateCrownCourtApplicationRequest.getCrownCourtSummary())
                .build();
    }
}
