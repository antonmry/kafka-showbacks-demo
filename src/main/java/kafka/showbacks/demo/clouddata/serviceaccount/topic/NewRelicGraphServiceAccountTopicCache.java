package kafka.showbacks.demo.clouddata.serviceaccount.topic;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.ImmutableMap;
import kafka.showbacks.demo.common.exception.KafkaShowBackDemoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

//todo name
public final class NewRelicGraphServiceAccountTopicCache implements ServiceAccountTopic {

	private static final Logger log = LoggerFactory.getLogger(NewRelicGraphServiceAccountTopicCache.class);

	private static final String QUERY_TO_GET_SERVICE_ACCOUNT_AND_TOPICS = "SELECT "
			+ "UNIQUES(TUPLE(data.authenticationInfo.principal.confluentServiceAccount.resourceId, data.request.data.topic)) "
			+ "FROM Log "
			+ "WHERE data.authenticationInfo.principal.confluentServiceAccount.resourceId IS NOT NULL "
			+ "AND data.request.data.topic IS NOT NULL "
			+ "AND aparse(data.resourceName,'%cloud-cluster=*/%') = '?' "
			+ "LIMIT MAX SINCE 1 DAY AGO";

	private final Cache<String, Map<String, Set<String>>> serviceAccountsGroupedByTopicCache;

	private final String newRelicAccountId;

	private final NewRelicGraphClient newRelicGraphClient;

	//todo accountid here
	@Inject
	public NewRelicGraphServiceAccountTopicCache(final NewRelicGraphClient newRelicGraphClient,
	                                             final int cacheExpiredInHours,
	                                             final String newRelicAccountId) {

		this.newRelicGraphClient = newRelicGraphClient;
		this.newRelicAccountId = newRelicAccountId;
		this.serviceAccountsGroupedByTopicCache = Caffeine.newBuilder()
				.expireAfterWrite(cacheExpiredInHours, TimeUnit.HOURS)
				.build();
	}

	@Override
	public ImmutableMap<String, Set<String>> getServiceAccountGroupedByTopicInCluster(String clusterId) throws KafkaShowBackDemoException {
		try {
			return ImmutableMap.copyOf(serviceAccountsGroupedByTopicCache.get(clusterId, this::fillCacheServiceAccountGroupedByTopicInCluster));
		} catch (RuntimeException runtimeException) {
			log.error("Error fill in the relation between services account and topics", runtimeException);
			throw new KafkaShowBackDemoException(runtimeException.getMessage(), runtimeException);
		}
	}

	private Map<String, Set<String>> fillCacheServiceAccountGroupedByTopicInCluster(final String clusterId) {

		log.info("The cache with service accounts and topics is empty and it have to fill out.");
		try {

			final Set<NewRelicGraphData> newRelicGraphDataSet = newRelicGraphClient.executeQuery(QUERY_TO_GET_SERVICE_ACCOUNT_AND_TOPICS.replace("?", clusterId), newRelicAccountId);

			if (newRelicGraphDataSet.isEmpty()) {
				throw new KafkaShowBackDemoException("It has not been possible to recover the needed data to get the relation between services accounts and topics.");
			}

			final Map<String, Set<String>> values = new HashMap<>();

			for (NewRelicGraphData newRelicGraphData : newRelicGraphDataSet) {
				values.putAll(Arrays.stream(newRelicGraphData.members())
						.collect(Collectors.groupingBy(topic -> topic[1],
								Collectors.mapping(sa -> sa[0], Collectors.toSet()))));

			}
			return values;
		} catch (KafkaShowBackDemoException kafkaShowBackDemoException) {
			throw new RuntimeException(kafkaShowBackDemoException);
		}
	}
}
