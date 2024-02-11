package kafka.showbacks.demo.common.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.base.Joiner;
import kafka.showbacks.demo.common.exception.KafkaShowBackDemoException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;

import static java.time.temporal.ChronoUnit.SECONDS;

//todo check log
//todo check exceptions error message log
//todo warnings
public class AbstractServiceClient {
	private static final Logger log = LoggerFactory.getLogger(AbstractServiceClient.class);

	private static final HttpClient httpClient = HttpClient.newBuilder().build();

	private static final ObjectMapper objectMapper = new ObjectMapper();

	//todo enum
	protected static final String BASIC_CONTENT_TYPE_KEY_HEADER = "Content-Type";

	protected static final String BASIC_CONTENT_TYPE_VALUE_HEADER = "application/json";

	protected static final String BASIC_AUTHORIZATION_HEADER = "Authorization";

	protected static final String API_KEY_HEADER = "Api-Key";

	protected static final String CONTENT_ENCODING_GZIP_HEADER = "gzip";

	private final RetryOnError retryOnError;

	private final int requestTimeOutInSeconds;

	private String[] headers;

	protected AbstractServiceClient(final int requestTimeOutInSeconds,
	                                final RetryOnError retryOnError
	) {
		this.retryOnError = retryOnError;
		this.requestTimeOutInSeconds = requestTimeOutInSeconds;

		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.registerModule(new JavaTimeModule());
	}

	protected HttpRequest createRequestGETBuilder(final String url) throws KafkaShowBackDemoException {
		return getBasicRequestBuilder(url)
				.GET().build();
	}

	protected HttpRequest createRequestPOSTBuilder(final String body,
	                                               final String url) throws KafkaShowBackDemoException {

		return getBasicRequestBuilder(url)
				.POST(HttpRequest.BodyPublishers.ofString(body)).build();

	}

	protected Optional<String> getHttpResponse(final HttpRequest httpRequest) throws KafkaShowBackDemoException {
		retryOnError.restart();

		final HttpResponse<String> metricRequestHttpResponse = send(httpRequest);

		if (metricRequestHttpResponse.statusCode() != HttpURLConnection.HTTP_OK) {
			throw new KafkaShowBackDemoException(StringUtils.isNoneBlank(metricRequestHttpResponse.body()) ? metricRequestHttpResponse.body() :
					Joiner.on(" ").join("Unrecognized error sending request, status code", metricRequestHttpResponse.statusCode()));
		}
		return Optional.ofNullable(metricRequestHttpResponse.body());
	}

	protected String getBasicAuthenticationHeader(String username, String password) {
		String valueToEncode = username + ":" + password;
		return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes(StandardCharsets.UTF_8));
	}


	private HttpRequest.Builder getBasicRequestBuilder(final String url) throws KafkaShowBackDemoException {
		try {
			return HttpRequest.newBuilder()
					.uri(new URI(url))
					.headers(headers)
					.timeout(Duration.of(requestTimeOutInSeconds, SECONDS));
		} catch (URISyntaxException | IllegalArgumentException exception) {
			throw new KafkaShowBackDemoException(Joiner.on(" ").join("Error creating request builder to url", url)
					, exception);
		}
	}
	//todo check willackar
	protected <T> T mapJsonStringToObjectResponse(final Class<T> classType, final String response) throws KafkaShowBackDemoException {
		try {
			return objectMapper.readValue(
					response,
					classType
			);
		} catch (JsonProcessingException jsonProcessingException) {
			throw new KafkaShowBackDemoException(Joiner.on(" ").join("Error mapping the response object", response), jsonProcessingException);
		}
	}

	protected void addHeader(String... headers) {
		this.headers = headers;
	}

	private HttpResponse<String> send(final HttpRequest httpRequest) throws KafkaShowBackDemoException {
		try {

			HttpResponse<String> metricRequestHttpResponse = httpClient
					.send(httpRequest, HttpResponse.BodyHandlers.ofString());

			while (retryOnError.shouldRetry(metricRequestHttpResponse.statusCode())) {
				retryOnError.errorOccurred();
				log.error("Retrying call with number status code {}", metricRequestHttpResponse.statusCode());
				metricRequestHttpResponse = httpClient
						.send(httpRequest, HttpResponse.BodyHandlers.ofString());
			}
			return metricRequestHttpResponse;

		} catch (IOException | InterruptedException | IllegalArgumentException | SecurityException e) {
			throw new KafkaShowBackDemoException("Error getting the response ", e);
		}
	}

}
