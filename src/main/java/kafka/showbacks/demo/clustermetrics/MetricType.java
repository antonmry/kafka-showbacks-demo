package kafka.showbacks.demo.clustermetrics;

import org.apache.commons.lang3.StringUtils;

enum MetricType {
	REQUEST_BYTES("io.confluent.kafka.server/request_bytes", "metric.principal_id"),
	RESPONSE_BYTES("io.confluent.kafka.server/response_bytes", "metric.principal_id"),
	RECEIVE_BYTES("io.confluent.kafka.server/received_bytes", StringUtils.EMPTY),
	SEND_BYTES("io.confluent.kafka.server/sent_bytes", StringUtils.EMPTY);

	private final String metricReference;
	private final String groupBy;

	MetricType(final String metricReference, final String groupBy) {
		this.metricReference = metricReference;
		this.groupBy = groupBy;
	}

	String getMetricReference() {
		return metricReference;
	}

	String getGroupBy() {
		return groupBy;
	}
}
