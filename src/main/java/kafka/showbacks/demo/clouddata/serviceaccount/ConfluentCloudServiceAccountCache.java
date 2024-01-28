package kafka.showbacks.demo.clouddata.serviceaccount;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import kafka.showbacks.demo.clouddata.ConfluentCloudServiceClient;
import kafka.showbacks.demo.common.exception.KafkaShowBackDemoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//todo logs exception
public class ConfluentCloudServiceAccountCache {
	private static final Logger log = LoggerFactory.getLogger(ConfluentCloudServiceAccountCache.class);

	private static final TypeReference<Set<ConfluentCloudServiceAccountDataItem>> TYPE_REFERENCE = new TypeReference<>() {
	};

	private static final Pattern pattern = Pattern.compile("Service account for the (.*?),(.*?),(.*?) application");

	private final ConfluentCloudServiceClient confluentCloudServiceAccountClient;

	private final Cache<Boolean, Map<String, ServiceAccountClusterInformation>> serviceAccountInformationCache;

	private final String serviceAccountCloudUrl;

	//todo ...
	private static final String QUERY_PARAMETER_MAX_PAGE_SIZE = "?page_size=100";


	@Inject
	public ConfluentCloudServiceAccountCache(final ConfluentCloudServiceClient confluentCloudServiceAccountClient,
	                                         final int cacheExpiredInHours,
	                                         final String serviceAccountCloudUrl) {
		this.confluentCloudServiceAccountClient = confluentCloudServiceAccountClient;
		this.serviceAccountInformationCache = Caffeine.newBuilder()
				.expireAfterWrite(cacheExpiredInHours, TimeUnit.HOURS)
				.build();
		//todo check here aboiut null
		this.serviceAccountCloudUrl = Joiner.on("").join(serviceAccountCloudUrl, QUERY_PARAMETER_MAX_PAGE_SIZE);
	}

	public ImmutableMap<String, ServiceAccountClusterInformation> getServiceAccountInformation() throws KafkaShowBackDemoException {
		try {
			return ImmutableMap.copyOf(serviceAccountInformationCache.get(Boolean.TRUE, this::fillServiceAccountInformationMap));
		} catch (RuntimeException runtimeException) {
			throw new KafkaShowBackDemoException("Error fill in cache with the services account.", runtimeException);
		}
	}

	private Map<String, ServiceAccountClusterInformation> fillServiceAccountInformationMap(final boolean defaultBooleanKey) {

		log.info("The cache with the service account and cluster information is empty.");

		try {
			final Set<ConfluentCloudServiceAccountDataItem> confluentCloudServiceAccountDataItems = confluentCloudServiceAccountClient.getCollectionFromConfluentCloudServiceClient(serviceAccountCloudUrl, TYPE_REFERENCE);

			if (confluentCloudServiceAccountDataItems.isEmpty()) {
				throw new KafkaShowBackDemoException("It has not been possible to recover the Services accounts. Review if the rest endpoint is working correctly.");
			}

			return mapConfluentCloudServiceAccountResponse(confluentCloudServiceAccountDataItems);
		} catch (KafkaShowBackDemoException c) {
			throw new RuntimeException(c);
		}
	}

	private Map<String, ServiceAccountClusterInformation> mapConfluentCloudServiceAccountResponse(final Set<ConfluentCloudServiceAccountDataItem> confluentCloudServiceAccountDataItems) {
		final Map<String, ServiceAccountClusterInformation> mapServiceAccountClusterInformation = new HashMap<>();
		confluentCloudServiceAccountDataItems.forEach(record -> {
			final Matcher matcher = pattern.matcher(record.getDescription());
			if (matcher.find() && matcher.groupCount() > 2) {
				mapServiceAccountClusterInformation.put(record.getId(), new ServiceAccountClusterInformation(matcher.group(1), matcher.group(2), matcher.group(3)));
			}
		});
		return mapServiceAccountClusterInformation;
	}
}
