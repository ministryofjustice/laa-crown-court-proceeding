package uk.gov.justice.laa.crime.crowncourt.builder;

import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtsActionsRequestDTO;
import uk.gov.justice.laa.crime.crowncourt.model.ApiCheckCrownCourtActionsRequest;

public class CrownCourtsActionsRequestDTOBuilder {
    public CrownCourtsActionsRequestDTO buildRequestDTO(final ApiCheckCrownCourtActionsRequest apiCheckCrownCourtActionsRequest) {
        return new CrownCourtsActionsRequestDTO();
    }
}
