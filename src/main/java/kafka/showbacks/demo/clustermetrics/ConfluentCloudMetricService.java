package kafka.showbacks.demo.clustermetrics;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.ImmutableMap;
import kafka.showbacks.demo.common.exception.KafkaShowBackDemoException;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ConfluentCloudMetricService implements ClusterMetricService {
	private final ClusterMetricClient clusterMetricClient;
	private final Cache<String, Set<MetricDataItem>> metricDataItemCache;

	@Inject
	public ConfluentCloudMetricService(final @Named("confluentCloudCustomMetricClient") ClusterMetricClient clusterMetricClient,
	                                   final int cacheExpiredInHours) {
		this.clusterMetricClient = clusterMetricClient;
		this.metricDataItemCache = Caffeine.newBuilder()
				.expireAfterWrite(cacheExpiredInHours, TimeUnit.HOURS)
				.build();
	}

	@Override
	public ImmutableMap<Instant, List<MetricInformation>> getServicesAccountResponseBytesByClusterIdGroupedByHour(String clusterId, Instant startInstant, Instant endInstant) throws KafkaShowBackDemoException {
		return getMetricInformationByClusterIdGroupedByHour(MetricType.RESPONSE_BYTES, startInstant, endInstant, clusterId);
	}

	@Override
	public ImmutableMap<Instant, List<MetricInformation>> getServicesAccountRequestBytesByClusterIdGroupedByHour(final String clusterId, final Instant startInstant, final Instant endInstant) throws KafkaShowBackDemoException {
		return getMetricInformationByClusterIdGroupedByHour(MetricType.REQUEST_BYTES, startInstant, endInstant, clusterId);
	}

	@Override
	public ImmutableMap<Instant, BigDecimal> getPercentageOfUsageByClusterGroupByHour(final String clusterId, final Instant startInstant, final Instant endInstant) throws KafkaShowBackDemoException {
		return getValueMetricByClusterIdGroupedByHour(MetricType.CLUSTER_LOAD_PERCENT, startInstant, endInstant, clusterId);
	}

	@Override
	public ImmutableMap<Instant, BigDecimal> getReceiveBytesByClusterGroupByHour(String clusterId, Instant startInstant, Instant endInstant) throws KafkaShowBackDemoException {
		return getValueMetricByClusterIdGroupedByHour(MetricType.RECEIVE_BYTES, startInstant, endInstant, clusterId);
	}

	@Override
	public ImmutableMap<Instant, BigDecimal> getSendBytesByClusterGroupByHour(String clusterId, Instant startInstant, Instant endInstant) throws KafkaShowBackDemoException {
		return getValueMetricByClusterIdGroupedByHour(MetricType.SEND_BYTES, startInstant, endInstant, clusterId);
	}

	@Override
	public ImmutableMap<Instant, List<MetricInformation>> getRetainedBytesTopicGroupedByHour(String clusterId, Instant startInstant, Instant endInstant) throws KafkaShowBackDemoException {
		return getMetricInformationByClusterIdGroupedByHour(MetricType.KAFKA_STORAGE, startInstant, endInstant, clusterId);
	}

	private static MetricBody.MetricBodyBuilder buildMetricBody(final MetricType metricType, final Instant startInstant, final Instant endInstant, final String clusterId) {

		return MetricBody.metricBodyBuilder()
				.withMetric(metricType)
				.withCluster(clusterId)
				.withGroupBy(metricType.getGroupBy())
				.withIntervals(startInstant, endInstant);
	}

	private ImmutableMap<Instant, BigDecimal> getValueMetricByClusterIdGroupedByHour(final MetricType metricType, final Instant startInstant, final Instant endInstant, final String clusterId) throws KafkaShowBackDemoException {
		final MetricBody metricBody = buildMetricBody(metricType, startInstant, endInstant, clusterId)
				.build();

		final Set<MetricDataItem> metricsDataItem = clusterMetricClient
				.getMetricDataByClusterAndType(metricBody);

		return ImmutableMap.copyOf(metricsDataItem.stream().collect(Collectors.toMap(MetricDataItem::timestamp, val -> convertDoubleValueToBigDecimal(val.value()))));
	}

	private ImmutableMap<Instant, List<MetricInformation>> getMetricInformationByClusterIdGroupedByHour(final MetricType metricType, final Instant startInstant, final Instant endInstant, final String clusterId)
			throws KafkaShowBackDemoException {
		final MetricBody metricBody = buildMetricBody(metricType, startInstant, endInstant, clusterId)
				.build();

		Set<MetricDataItem> metricsDataItem = metricDataItemCache.getIfPresent(metricBody.toString());

		if (metricsDataItem == null || metricsDataItem.isEmpty()) {
			metricsDataItem = clusterMetricClient
					.getMetricDataByClusterAndType(metricBody);

			metricDataItemCache.put(metricBody.toString(), metricsDataItem);
		}

		return ImmutableMap.copyOf(metricsDataItem.stream()
				.map(data ->
						new MetricInformation(data.metric(),
								data.timestamp(), convertDoubleValueToBigDecimal(data.value())))
				.collect(Collectors.groupingBy(MetricInformation::startTime)));
	}

	private static BigDecimal convertDoubleValueToBigDecimal(final Double value) {
		return value == null || value.isNaN() ? BigDecimal.ZERO : new BigDecimal(Double.toString(value));
	}
}
