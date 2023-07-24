package uk.gov.justice.laa.crime.crowncourt.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.QueueDispatcher;
import okhttp3.mockwebserver.RecordedRequest;
import org.springframework.http.MediaType;

import java.net.HttpURLConnection;
import java.util.Map;
import java.util.UUID;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;

public class MockWebServerStubs {
    public static Dispatcher getDispatcher() {
        final Dispatcher dispatcher = new QueueDispatcher() {
            @Override
            public MockResponse dispatch (RecordedRequest request) throws InterruptedException {

                if ("/oauth2/token".equals(request.getPath())) {
                    return getOauthResponse();
                }

                var requestLine = request.getRequestLine();
                if ("GET /favicon.ico HTTP/1.1".equals(requestLine)) {
                    return new MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND);
                }

                return getResponseQueue().take();
            }
        };
        return dispatcher;
    }

    private static MockResponse getOauthResponse() {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> token = Map.of(
                "expires_in", 3600,
                "token_type", "Bearer",
                "access_token", UUID.randomUUID()
        );
        String responseBody;
        MockResponse response = new MockResponse();
        response.setResponseCode(OK.code());
        response.setHeader("Content-Type", MediaType.APPLICATION_JSON);

        try {
            responseBody = objectMapper.writeValueAsString(token);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return response.setBody(responseBody);
    }
}
