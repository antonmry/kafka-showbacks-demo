package kafka.showbacks.demo.clouddata;

import com.fasterxml.jackson.core.type.TypeReference;
import kafka.showbacks.demo.common.exception.KafkaShowBackDemoException;
import kafka.showbacks.demo.common.rest.AbstractServiceClient;
import kafka.showbacks.demo.common.rest.RetryOnError;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import java.net.http.HttpRequest;
import java.util.HashSet;
import java.util.Set;

public final class ConfluentCloudServiceClient extends AbstractServiceClient {

	private static final Logger log = LogManager.getLogger();

	@Inject
	public ConfluentCloudServiceClient(final String confluentAPIKey,
	                                   final String confluentAPISecret,
	                                   final int durationInSeconds,
	                                   final RetryOnError retryOnError) {
		super(durationInSeconds, retryOnError);
		super.addHeader(BASIC_AUTHORIZATION_HEADER, getBasicAuthenticationHeader(confluentAPIKey, confluentAPISecret),
				BASIC_CONTENT_TYPE_KEY_HEADER, BASIC_CONTENT_TYPE_VALUE_HEADER);
	}

	public <T extends ConfluentCloudDataItem> Set<T> getCollectionFromConfluentCloudServiceClient(String cloudUrl, final TypeReference<Set<T>> typeReference) throws KafkaShowBackDemoException {
		if (StringUtils.isEmpty(cloudUrl)) {
			throw new KafkaShowBackDemoException("The cloudUrl parameter can not be null");
		}

		log.info("Recovering all service accounts for Confluent Cloud environment");
		final Set<T> results = new HashSet<>();

		while (!StringUtils.isEmpty(cloudUrl)) {
			final HttpRequest httpRequest = createRequestGETBuilder(cloudUrl);
			cloudUrl = StringUtils.EMPTY;

			final String httpResponse = getHttpResponse(httpRequest).
					orElseThrow(() -> new KafkaShowBackDemoException("The call to fill the collection from cloud API return an empty answer."));

			final ConfluentCloudServiceResponse result = mapJsonStringToObjectResponse(ConfluentCloudServiceResponse.class, httpResponse);

			if (result.hasData()) {
				results.addAll(result.getConfluentCloudDataItem(typeReference));
			}

			if (result.hasNextPages()) {
				cloudUrl = result.getMetadata().next();

				log.info("The query to fill the collection from cloud API return more than one page");
			}

		}

		return results;
	}

}
