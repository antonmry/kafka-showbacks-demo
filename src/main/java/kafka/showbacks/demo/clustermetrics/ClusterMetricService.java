package kafka.showbacks.demo.clustermetrics;

import com.google.common.collect.ImmutableMap;
import kafka.showbacks.demo.common.exception.KafkaShowBackDemoException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * This interface provider services to get the data to calculate the cost by teams
 * Currently just is implemented from Confluent Cloud API
 * But the idea is that can implement another services as MSK
 */
public interface ClusterMetricService {

	ImmutableMap<Instant, List<MetricInformation>> getServicesAccountResponseBytesByClusterIdGroupedByHour(final String clusterId, final Instant startInstant, final Instant endInstant) throws KafkaShowBackDemoException;

	ImmutableMap<Instant, List<MetricInformation>> getServicesAccountRequestBytesByClusterIdGroupedByHour(final String clusterId, final Instant startInstant, final Instant endInstant) throws KafkaShowBackDemoException;

	ImmutableMap<Instant, BigDecimal> getReceiveBytesByClusterGroupByHour(final String clusterId, final Instant startInstant, final Instant endInstant) throws KafkaShowBackDemoException;

	ImmutableMap<Instant, BigDecimal> getSendBytesByClusterGroupByHour(final String clusterId, final Instant startInstant, final Instant endInstant) throws KafkaShowBackDemoException;

}
