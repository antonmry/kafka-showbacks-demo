package kafka.showbacks.demo.common.rest;

import kafka.showbacks.demo.common.exception.KafkaShowBackDemoException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.junit.jupiter.MockServerSettings;
import org.mockserver.matchers.Times;
import org.mockserver.model.Header;

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@ExtendWith({MockServerExtension.class})
@MockServerSettings(ports = {1080, 1081, 1082})
public class AbstractServiceClientTest {

	protected static final String PATH_TEST_KO = "/test/call/ko";
	protected static final String PATH_TEST_OK = "/test/call/ok";

	protected static final String API_KEY_TEST = "api_key_test";
	protected static final String API_SECRET_TEST = "api_secret_test";

	protected MockServerClient mockServerClient;

	@Mock
	protected RetryOnError retryOnError;


	public void setUp(final int port) {
		MockitoAnnotations.openMocks(this);
		this.mockServerClient = new MockServerClient("127.0.0.1", port);
	}

	protected void mockRetryOnErrorOnFailure(final int statusCode) throws KafkaShowBackDemoException {
		when(retryOnError.shouldRetry(eq(statusCode))).thenReturn(true, true, true, false);
	}

	protected void verifyCallRetryOnError() throws InterruptedException {
		verify(retryOnError, atLeastOnce()).errorOccurred();
	}

	protected void verifyNoCallRetryOnError() throws InterruptedException {
		verify(retryOnError, times(0)).errorOccurred();
	}

	protected void createBodyForInvalidCallMethodGET(final int statusCode) {
		this.mockServerClient
				.when(
						request()
								.withMethod("GET")
								.withPath(PATH_TEST_KO)
								.withHeaders(Header.header("Content-type", "application/json"))
						,
						Times.exactly(1))
				.respond(
						response()
								.withStatusCode(statusCode)
								.withHeaders(
										new Header("Content-Type", "application/json; charset=utf-8"),
										new Header("Cache-Control", "public, max-age=86400"))
								.withBody("{\"errors\":[{\"status\":\"400\",\"detail\":\"Invalid intervals: [2023-08-20T16:00:00Z/2023-08-20T16:59:00Z] Data from 7 days before now may not be queried\"}]}")
								.withDelay(TimeUnit.SECONDS, 1)
				);
	}

	protected void createBodyForInvalidCallMethodPOST(final String jsonBody, final int statusCode) {
		this.mockServerClient
				.when(
						request()
								.withMethod("POST")
								.withPath(PATH_TEST_KO)
								.withHeaders(Header.header("Content-type", "application/json"))
								.withBody(jsonBody),
						exactly(1))
				.respond(
						response()
								.withStatusCode(statusCode)
								.withHeaders(
										new Header("Content-Type", "application/json; charset=utf-8"),
										new Header("Cache-Control", "public, max-age=86400"))
								.withBody("{\"errors\":[{\"status\":\"400\",\"detail\":\"Invalid intervals: [2023-08-20T16:00:00Z/2023-08-20T16:59:00Z] Data from 7 days before now may not be queried\"}]}")
								.withDelay(TimeUnit.SECONDS, 1)
				);
	}
}
