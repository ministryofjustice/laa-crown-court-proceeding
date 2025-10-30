package uk.gov.justice.laa.crime.crowncourt.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import uk.gov.justice.laa.crime.crowncourt.staticdata.repository.CrownCourtsRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CrownCourtsServiceTest {

    @InjectMocks
    private CrownCourtsService crownCourtsService;

    @Mock
    private CrownCourtsRepository crownCourtsRepository;

    @Test
    void testCrownCourtsService_whenGetCrownCourtByIdInvoked_shouldSuccess() {
        crownCourtsService.getById("401");
        verify(crownCourtsRepository, times(1)).findById(any());
    }
}
