package kafka.showbacks.demo.clouddata;

import com.google.common.base.Joiner;
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

	private static final String QUERY_PARAMETER_MAX_PAGE_SIZE = "?page_size=100";

	private final String cloudUrl;

	@Inject
	public ConfluentCloudServiceClient(final String confluentAPIKey,
	                                   final String confluentAPISecret,
	                                   final int durationInSeconds,
	                                   final String cloudUrl,
	                                   final RetryOnError retryOnError) {
		super(durationInSeconds, retryOnError);
		super.addHeader(BASIC_AUTHORIZATION_HEADER, getBasicAuthenticationHeader(confluentAPIKey, confluentAPISecret),
				BASIC_CONTENT_TYPE_KEY_HEADER, BASIC_CONTENT_TYPE_VALUE_HEADER);
		this.cloudUrl = cloudUrl;
	}

	//todo create interface
	public <T> void fillCollectionFromConfluentCloudServiceClient(Set<T> collectionResult) throws KafkaShowBackDemoException {
		if (StringUtils.isEmpty(this.cloudUrl)) {
			throw new KafkaShowBackDemoException("The cloudUrl parameter can not be null");
		}

		String temporalCloudUrl = Joiner.on("").join(this.cloudUrl, QUERY_PARAMETER_MAX_PAGE_SIZE);

		log.info("Recovering all service accounts for Confluent Cloud environment");

		//todo ini in another place
		//collectionResult = new ArrayList<>() {};

		while (!StringUtils.isEmpty(temporalCloudUrl)) {
			final HttpRequest httpRequest = createRequestGETBuilder(temporalCloudUrl);
			temporalCloudUrl = StringUtils.EMPTY;

			final String httpResponse = getHttpResponse(httpRequest).
					orElseThrow(() -> new KafkaShowBackDemoException("The call to get the services account return an empty answer."));

			final ConfluentCloudServiceResponse result = mapJsonStringToObjectResponse(ConfluentCloudServiceResponse.class, httpResponse);

			if (result.hasData()) {
				collectionResult.addAll(result.getData());
			}

			if (result.hasNextPages()) {
				temporalCloudUrl = result.getMetadata().getNext();

				log.info("The query to get the service account return more than one page");
			}

		}

	}

}
