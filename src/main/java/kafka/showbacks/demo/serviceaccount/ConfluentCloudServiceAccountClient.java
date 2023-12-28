package kafka.showbacks.demo.serviceaccount;

import com.google.common.base.Joiner;
import kafka.showbacks.demo.common.exception.KafkaShowBackDemoException;
import kafka.showbacks.demo.common.rest.AbstractServiceClient;
import kafka.showbacks.demo.common.rest.RetryOnError;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.net.http.HttpRequest;
import java.util.HashSet;
import java.util.Set;

public class ConfluentCloudServiceAccountClient extends AbstractServiceClient {

	private static final Logger log = LoggerFactory.getLogger(ConfluentCloudServiceAccountClient.class);

	private static final String QUERY_PARAMETER_MAX_PAGE_SIZE = "?page_size=100";

	private final String cloudUrl;

	@Inject
	public ConfluentCloudServiceAccountClient(final String confluentAPIKey,
	                                          final String confluentAPISecret,
	                                          final int durationInSeconds,
	                                          final String cloudUrl,
	                                          final RetryOnError retryOnError) {
		super(durationInSeconds, retryOnError);
		super.addHeader(BASIC_AUTHORIZATION_HEADER, getBasicAuthenticationHeader(confluentAPIKey, confluentAPISecret),
				BASIC_CONTENT_TYPE_KEY_HEADER, BASIC_CONTENT_TYPE_VALUE_HEADER);
		this.cloudUrl = cloudUrl;
	}

	Set<ConfluentCloudServiceAccountDataItem> getServiceAccountClients() throws KafkaShowBackDemoException {
		if (StringUtils.isEmpty(this.cloudUrl)) {
			throw new KafkaShowBackDemoException("The cloudUrl parameter can not be null");
		}

		String temporalCloudUrl = Joiner.on("").join(this.cloudUrl, QUERY_PARAMETER_MAX_PAGE_SIZE);

		log.info("Recovering all service accounts for Confluent Cloud environment");

		final Set<ConfluentCloudServiceAccountDataItem> confluentCloudServiceAccountDataItems = new HashSet<>();

		while (!StringUtils.isEmpty(temporalCloudUrl)) {
			final HttpRequest httpRequest = createRequestGETBuilder(temporalCloudUrl);
			temporalCloudUrl = StringUtils.EMPTY;

			final String httpResponse = getHttpResponse(httpRequest).
					orElseThrow(() -> new KafkaShowBackDemoException("The call to get the services account return an empty answer."));

			final ConfluentCloudServiceAccountResponse result = mapJsonStringToObjectResponse(ConfluentCloudServiceAccountResponse.class, httpResponse);

			if (result.hasData()) {
				confluentCloudServiceAccountDataItems.addAll(result.getData());
			}

			if (result.hasNextPages()) {
				temporalCloudUrl = result.getMetadata().getNext();

				log.info("The query to get the service account return more than one page");
			}

		}

		return confluentCloudServiceAccountDataItems;
	}


}
