package uk.gov.justice.laa.crime.crowncourt.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.crime.crowncourt.config.MockServicesConfiguration;
import uk.gov.justice.laa.crime.crowncourt.config.ServicesConfiguration;
import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;
import uk.gov.justice.laa.crime.crowncourt.exception.APIClientException;
import uk.gov.justice.laa.crime.crowncourt.service.CourtDataAdapterService;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.MessageType;
import uk.gov.justice.laa.crime.crowncourt.exception.CCPDataException;
import uk.gov.justice.laa.crime.crowncourt.model.laastatus.LaaStatusUpdate;
import uk.gov.justice.laa.crime.crowncourt.model.laastatus.RepOrderData;
import uk.gov.justice.laa.crime.crowncourt.service.QueueMessageLogService;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourtDataAdapterServiceTest {

    @Mock
    private CourtDataAdapterClient courtDataAdapterClient;

    @InjectMocks
    private CourtDataAdapterService courtDataAdapterService;

    @Spy
    private ServicesConfiguration configuration = MockServicesConfiguration.getConfiguration(1000);

    @Test
    void givenAValidHearingId_whenTriggerHearingProcessingIsInvoked_thenTheRequestIsSentCorrectly() {
        UUID testHearingId = UUID.randomUUID();
        String testTransactionId = UUID.randomUUID().toString();
        courtDataAdapterService.triggerHearingProcessing(testHearingId, testTransactionId);

        verify(courtDataAdapterClient).getApiResponseViaGET(
                eq(Void.class),
                anyString(),
                anyMap(),
                ArgumentMatchers.<MultiValueMap<String, String>>any(),
                any(UUID.class)
        );
    }

    @Test
    void givenAValidHearingId_whenTriggerHearingProcessingIsInvokedAndTheCallFails_thenFailureIsHandled() {

        UUID testHearingId = UUID.randomUUID();
        String testTransactionId = UUID.randomUUID().toString();

        when(courtDataAdapterClient.getApiResponseViaGET(
                eq(Void.class),
                anyString(),
                anyMap(),
                ArgumentMatchers.<MultiValueMap<String, String>>any(),
                any(UUID.class)
        )).thenThrow(new APIClientException());

        assertThatThrownBy(() -> {
            courtDataAdapterService.triggerHearingProcessing(testHearingId, testTransactionId);
        }).isInstanceOf(APIClientException.class);
    }
}
