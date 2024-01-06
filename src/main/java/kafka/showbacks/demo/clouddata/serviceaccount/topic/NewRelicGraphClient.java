package kafka.showbacks.demo.clouddata.serviceaccount.topic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Joiner;
import kafka.showbacks.demo.common.exception.KafkaShowBackDemoException;
import kafka.showbacks.demo.common.rest.AbstractServiceClient;
import kafka.showbacks.demo.common.rest.ResponseObject;
import kafka.showbacks.demo.common.rest.RetryOnError;
import org.apache.commons.lang3.StringUtils;

import java.net.http.HttpRequest;
import java.util.Set;

//todo package
//todo interface
//todo check pagination
//todo warning
public final class NewRelicGraphClient extends AbstractServiceClient {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	private static final String NR_GRAPH_BODY_FORMAT = "{\"query\":\"{\n  actor {\n    account(id: {ACCOUNT_ID}) "
			+ "{\n      nrql(query: {QUERY}) {\n        rawResponse\n      }\n    }\n  }\n}\n\"}";

	private static final String HEADER_API_KEY = "API-Key";

	private final String nrGraphUrl;

	public NewRelicGraphClient(final int requestTimeOutInSeconds,
	                           final String nrGraphUrl,
	                           final String apiKey,
	                           final RetryOnError retryOnError) {
		super(requestTimeOutInSeconds, retryOnError);
		super.addHeader(HEADER_API_KEY, apiKey, BASIC_CONTENT_TYPE_KEY_HEADER, BASIC_CONTENT_TYPE_VALUE_HEADER);
		this.nrGraphUrl = nrGraphUrl;
	}

	public Set<NewRelicGraphData> executeQuery(final String query, final String accountId) throws KafkaShowBackDemoException {

		if (StringUtils.isEmpty(query) || StringUtils.isEmpty(accountId)) {
			throw new KafkaShowBackDemoException(Joiner.on(" ")
					.join("The accountId:", accountId, "and query:", query, "are mandatory, review the configuration."));
		}

		final String requestJsonPayload = generateRequestJsonPayload(query, accountId);

		final HttpRequest httpRequest = createRequestPOSTBuilder(requestJsonPayload,
				nrGraphUrl);

		final String httpResponse = getHttpResponse(httpRequest).orElseThrow(
				() -> new KafkaShowBackDemoException(Joiner.on(" ").join("Error querying nrGraph with the following query:", query)));

		final NewRellicGraphResponse newRellicGraphResponse = mapJsonStringToObjectResponse(NewRellicGraphResponse.class, httpResponse);

		if (!newRellicGraphResponse.hasData()) {
			throw new KafkaShowBackDemoException(Joiner.on(" ")
					.join("The following Dirac query has not returned data:", query));
		}

		return newRellicGraphResponse.results();
	}

	//todo refactor is the best way to do it
	private String generateRequestJsonPayload(final String query, final String accountId) {
		return NR_GRAPH_BODY_FORMAT.replace("{ACCOUNT_ID}", accountId).replace("{QUERY}", query);
	}

	@JsonSerialize
	private record NewRellicGraphResponse(Set<NewRelicGraphData> results) implements ResponseObject {

		@Override
		public boolean hasData() {
			return results != null && !results().isEmpty();
		}

		@Override
		public boolean hasNextPages() {
			return ResponseObject.super.hasNextPages();
		}
	}


}
