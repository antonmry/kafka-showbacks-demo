package kafka.showbacks.demo.clustermetrics;

import kafka.showbacks.demo.common.exception.KafkaShowBackDemoException;

import java.util.Set;

/**
 * This interface provider data from different sources
 * Currently just is implemented from Cloud API
 * But the idea is that can implement another sources
 */
public interface ClusterMetricClient {
	Set<MetricDataItem> getMetricDataByClusterAndType(final MetricBody metricBody) throws KafkaShowBackDemoException;
}
