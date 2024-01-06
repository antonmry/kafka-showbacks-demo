package kafka.showbacks.demo.clouddata.serviceaccount.topic;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.ImmutableMap;
import kafka.showbacks.demo.common.exception.KafkaShowBackDemoException;

import javax.inject.Inject;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

//todo name
public final class NewRelicGraphServiceAccountTopicCache implements ServiceAccountTopic {

	private static final String QUERY_TO_GET_SERVICE_ACCOUNT_AND_TOPICS = "SELECT "
			+ "UNIQUES(TUPLE(data.authenticationInfo.principal.confluentServiceAccount.resourceId, data.request.data.topic)) "
			+ "FROM Log "
			+ "WHERE data.authenticationInfo.principal.confluentServiceAccount.resourceId IS NOT NULL "
			+ "AND data.request.data.topic IS NOT NULL "
			+ "AND aparse(data.resourceName,'%cloud-cluster=*/%') = '?' "
			+ "LIMIT MAX SINCE 1 DAY AGO";

	private final Cache<String, Map<String, Set<String>>> serviceAccountsGroupedByTopicCache;

	private final String newRelicAccountId;

	@Inject
	public NewRelicGraphServiceAccountTopicCache(final int cacheExpiredInHours,
	                                             //  final DiracClient diracClient,
	                                             final String newRelicAccountId) {

		//	this.diracClient = diracClient;
		this.newRelicAccountId = newRelicAccountId;
		this.serviceAccountsGroupedByTopicCache = Caffeine.newBuilder()
				.expireAfterWrite(cacheExpiredInHours, TimeUnit.HOURS)
				.build();
	}

	@Override
	public ImmutableMap<String, Set<String>> getServiceAccountGroupedByTopicInCluster(String clusterId) throws KafkaShowBackDemoException {
		return null;
	}
}
