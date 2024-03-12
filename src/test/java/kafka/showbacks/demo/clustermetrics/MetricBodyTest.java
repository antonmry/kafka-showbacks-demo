package kafka.showbacks.demo.clustermetrics;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MetricBodyTest {

	@Test
	public void metricBodyToJsonWorksOk() {
		final String jsonToReturn = "{\"aggregations\":[{\"metric\":\"io.confluent.kafka.server/request_bytes\"}],\"filter\":{\"field\":\"resource.kafka.id\",\"op\":\"EQ\",\"value\":\"lkc-xxxxx\"},\"granularity\":\"PT1M\",\"intervals\":[\"2023-07-23T16:00:00Z/2023-07-23T16:59:00Z\"],\"group_by\":[\"principal_id\"]}";
		MetricBody metricBody = createObject().build();
		final String jsonObject = metricBody.metricBodyToJson().get();
		assertEquals(jsonObject, jsonToReturn);
	}

	@Test
	public void metricBodyToJsonWorksOkWithMoreMetrics() {
		final String jsonToReturn = "{\"aggregations\":[{\"metric\":\"io.confluent.kafka.server/request_bytes\"}],\"filter\":{\"field\":\"resource.kafka.id\",\"op\":\"EQ\",\"value\":\"lkc-xxxxx\"},\"granularity\":\"PT1M\",\"intervals\":[\"2023-07-23T16:00:00Z/2023-07-23T16:59:00Z\"],\"group_by\":[\"topic\",\"principal_id\"]}";
		MetricBody metricBody = createObject()
				.withGroupBy("topic")
				.withMetric(MetricType.REQUEST_BYTES)
				.build();
		final String jsonObject = metricBody.metricBodyToJson().get();
		assertEquals(jsonObject, jsonToReturn);
	}

	private MetricBody.MetricBodyBuilder createObject() {
		MetricBody.MetricBodyBuilder metricBodyBuilder = new MetricBody.MetricBodyBuilder();

		final Instant start = Instant.parse("2023-07-23T11:00:00-05:00");
		final Instant end = Instant.parse("2023-07-23T11:59:00-05:00");

		return metricBodyBuilder
				.withMetric(MetricType.REQUEST_BYTES)
				.withCluster("lkc-xxxxx")
				.withGranularity("PT1M")
				.withGroupBy("principal_id")
				.withIntervals(start, end);
	}
}
