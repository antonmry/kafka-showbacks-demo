package kafka.showbacks.demo.clouddata.serviceaccount.topic;

import com.google.common.collect.ImmutableMap;
import kafka.showbacks.demo.common.exception.KafkaShowBackDemoException;

import java.util.Set;

//todo name
public final class NewRelicGraphServiceAccountTopicCache implements ServiceAccountTopic{
	@Override
	public ImmutableMap<String, Set<String>> getServiceAccountGroupedByTopicInCluster(String clusterId) throws KafkaShowBackDemoException {
		return null;
	}
}
