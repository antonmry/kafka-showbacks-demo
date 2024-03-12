package kafka.showbacks.demo.clustermetrics;

import kafka.showbacks.demo.common.exception.KafkaShowBackDemoException;
import kafka.showbacks.demo.common.rest.AbstractServiceClientTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockserver.model.Header;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class ConfluentCloudMetricClientTest extends AbstractServiceClientTest {

	private ClusterMetricClient metricData;

	@Mock
	private MetricBody metricBody;

	@BeforeEach
	public void setUp() {
		setUp(1080);
	}

	@Test
	public void returnEmptySetWhenMetricBodyIsIncorrect() {
		metricData = new ConfluentCloudMetricClient(API_KEY_TEST, API_SECRET_TEST, 10, PATH_TEST_OK, retryOnError);

		when(metricBody.metricBodyToJson()).thenReturn(Optional.empty());

		assertThrows(KafkaShowBackDemoException.class, () -> {
			metricData.getMetricDataByClusterAndType(metricBody);
		});
	}

	@Test
	public void throwConfluentTeamExceptionWhenConfigurationIsIncorrect() {
		metricData = new ConfluentCloudMetricClient(API_KEY_TEST, API_SECRET_TEST, 10, "http://127.0.0.1:1080" + PATH_TEST_KO, retryOnError);

		when(metricBody.metricBodyToJson()).thenReturn(createObject().metricBodyToJson());
		when(metricBody.getFilter()).thenReturn(new MetricBody.Filter("cluster", "EQ", "lkc-xxx"));
		when(metricBody.getAggregation()).thenReturn(Set.of(new MetricBody.Aggregation(MetricType.RECEIVE_BYTES.getMetricReference())));

		createBodyForInvalidCallMethodPOST(createObject().metricBodyToJson().get(), 400);

		Exception exception = assertThrows(KafkaShowBackDemoException.class, () -> {
			metricData.getMetricDataByClusterAndType(metricBody);
		});

		assertEquals(exception.getMessage(), "{\"errors\":[{\"status\":\"400\",\"detail\":\"Invalid intervals: [2023-08-20T16:00:00Z/2023-08-20T16:59:00Z] Data from 7 days before now may not be queried\"}]}");
	}

	@Test
	public void returnSetMetricDataItemWhenCallIsProcessedCorrectly() throws KafkaShowBackDemoException {
		metricData = new ConfluentCloudMetricClient(API_KEY_TEST, API_SECRET_TEST, 10, "http://127.0.0.1:1080" + PATH_TEST_OK, retryOnError);

		when(metricBody.metricBodyToJson()).thenReturn(createObject().metricBodyToJson());
		when(metricBody.getFilter()).thenReturn(new MetricBody.Filter("cluster", "EQ", "lkc-xxx"));
		when(metricBody.getAggregation()).thenReturn(Set.of(new MetricBody.Aggregation(MetricType.RECEIVE_BYTES.getMetricReference())));

		createBodyForCorrectAuth();

		Set<MetricDataItem> metricDataItemSet = metricData.getMetricDataByClusterAndType(metricBody);

		assertFalse(metricDataItemSet.isEmpty());
		assertEquals(metricDataItemSet.size(), 2);

		assertTrue(metricDataItemSet.stream().anyMatch(metricDataItem ->
				metricDataItem.value().equals(23.23) && metricDataItem.metric().equals("sa-xx000k")));
		assertTrue(metricDataItemSet.stream().anyMatch(metricDataItem ->
				metricDataItem.value().equals(24.55) && metricDataItem.metric().equals("sa-jsjsjs6")));

	}

	@Test
	public void throwConfluentCollectionExceptionWhenTheUriIsIncorrect() {
		metricData = new ConfluentCloudMetricClient(API_KEY_TEST, API_SECRET_TEST, 10, "127.0.0.1:1080" + PATH_TEST_KO, retryOnError);

		when(metricBody.metricBodyToJson()).thenReturn(createObject().metricBodyToJson());
		when(metricBody.getFilter()).thenReturn(new MetricBody.Filter("cluster", "EQ", "lkc-xxx"));

		Exception exception = assertThrows(KafkaShowBackDemoException.class, () -> {
			metricData.getMetricDataByClusterAndType(metricBody);
		});

		assertEquals(exception.getMessage(), "Error creating request builder to url 127.0.0.1:1080/test/call/ko");
	}

	@Test
	public void retryConnectionInGETWhenErrorCodeIsAllowed() throws InterruptedException, KafkaShowBackDemoException {
		metricData = new ConfluentCloudMetricClient(API_KEY_TEST, API_SECRET_TEST, 10, "http://127.0.0.1:1080" + PATH_TEST_KO, retryOnError);

		when(metricBody.metricBodyToJson()).thenReturn(createObject().metricBodyToJson());
		when(metricBody.getFilter()).thenReturn(new MetricBody.Filter("cluster", "EQ", "lkc-xxx"));
		when(metricBody.getAggregation()).thenReturn(Set.of(new MetricBody.Aggregation(MetricType.RECEIVE_BYTES.getMetricReference())));

		mockRetryOnErrorOnFailure(504);

		createBodyForInvalidCallMethodPOST(createObject().metricBodyToJson().get(), 504);

		assertThrows(KafkaShowBackDemoException.class, () -> {
			metricData.getMetricDataByClusterAndType(metricBody);
		});

		verifyCallRetryOnError();
	}

	private MetricBody createObject() {
		final Instant start = Instant.parse("2023-07-23T11:00:00-05:00");
		final Instant end = Instant.parse("2023-07-23T11:59:00-05:00");

		return MetricBody.metricBodyBuilder()
				.withMetric(MetricType.REQUEST_BYTES)
				.withCluster("lkc-xxxxx")
				.withGranularity("PT1M")
				.withGroupBy("principal_id")
				.withIntervals(start, end).build();
	}

	private void createBodyForCorrectAuth() {

		this.mockServerClient
				.when(
						request()
								.withMethod("POST")
								.withPath(PATH_TEST_OK)
								.withHeaders(Header.header("Content-type", "application/json"))
								.withBody(createObject().metricBodyToJson().get()),
						exactly(1))
				.respond(
						response()
								.withStatusCode(200)
								.withHeaders(
										new Header("Content-Type", "application/json; charset=utf-8"),
										new Header("Cache-Control", "public, max-age=86400"))
								.withBody("{\"data\":[{\"timestamp\":\"2023-08-10T16:00:00Z\",\"value\":23.23,\"metric.principal_id\":\"sa-xx000k\"},{\"timestamp\":\"2023-08-10T16:00:00Z\",\"value\":24.55,\"metric.principal_id\":\"sa-jsjsjs6\"}]}")
								.withDelay(TimeUnit.SECONDS, 1)
				);
	}

}
