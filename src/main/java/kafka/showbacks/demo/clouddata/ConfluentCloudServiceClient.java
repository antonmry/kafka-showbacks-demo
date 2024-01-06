package kafka.showbacks.demo.clouddata;

import kafka.showbacks.demo.common.exception.KafkaShowBackDemoException;
import kafka.showbacks.demo.common.rest.AbstractServiceClient;
import kafka.showbacks.demo.common.rest.RetryOnError;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.net.http.HttpRequest;
import java.util.Set;

//todo add filter by cluster in both
public final class ConfluentCloudServiceClient extends AbstractServiceClient {

	private static final Logger log = LoggerFactory.getLogger(ConfluentCloudServiceClient.class);

	@Inject
	public ConfluentCloudServiceClient(final String confluentAPIKey,
	                                   final String confluentAPISecret,
	                                   final int durationInSeconds,
	                                   final RetryOnError retryOnError) {
		super(durationInSeconds, retryOnError);
		super.addHeader(BASIC_AUTHORIZATION_HEADER, getBasicAuthenticationHeader(confluentAPIKey, confluentAPISecret),
				BASIC_CONTENT_TYPE_KEY_HEADER, BASIC_CONTENT_TYPE_VALUE_HEADER);
	}

	//todo create interface
	public <T> void fillCollectionFromConfluentCloudServiceClient(final Set<T> collectionResult, String cloudUrl) throws KafkaShowBackDemoException {
		if (StringUtils.isEmpty(cloudUrl)) {
			throw new KafkaShowBackDemoException("The cloudUrl parameter can not be null");
		}

		log.info("Recovering all service accounts for Confluent Cloud environment");

		while (!StringUtils.isEmpty(cloudUrl)) {
			final HttpRequest httpRequest = createRequestGETBuilder(cloudUrl);
			cloudUrl = StringUtils.EMPTY;

			final String httpResponse = getHttpResponse(httpRequest).
					orElseThrow(() -> new KafkaShowBackDemoException("The call to fill the collection from cloud API return an empty answer."));

			final ConfluentCloudServiceResponse result = mapJsonStringToObjectResponse(ConfluentCloudServiceResponse.class, httpResponse);

			if (result.hasData()) {
				collectionResult.addAll(result.getData());
			}

			if (result.hasNextPages()) {
				cloudUrl = result.getMetadata().getNext();

				log.info("The query to fill the collection from cloud API return more than one page");
			}

		}

	}

}