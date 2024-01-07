package kafka.showbacks.demo.clouddata.serviceaccount.topic;

import com.google.common.collect.ImmutableMap;
import kafka.showbacks.demo.common.exception.KafkaShowBackDemoException;

import java.util.Set;

/**
 * This interface is provide to get the cache with the relation between the service account and topics, take into account
 * that we can have in the future different providers to achieve this information
 */
//todo package??
public sealed interface ServiceAccountTopic permits NewRelicGraphServiceAccountTopicCache {
	ImmutableMap<String, Set<String>> getServiceAccountGroupedByTopicInCluster(final String clusterId) throws KafkaShowBackDemoException;

}
