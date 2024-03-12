package kafka.showbacks.demo.clustermetrics;

import com.google.common.collect.ImmutableMap;
import kafka.showbacks.demo.common.exception.KafkaShowBackDemoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConfluentCloudMetricServiceTest {

	private static final String CLUSTER_ID_TEST = "lkc-xxx";
	private static final Instant INSTANT_START_TEST = Instant.parse("2023-07-29T11:00:00-05:00");
	private static final Instant INSTANT_END_TEST = Instant.parse("2023-07-29T11:59:00-05:00");


	private ConfluentCloudMetricService confluentCloudMetricService;

	@Mock
	private ClusterMetricClient clusterMetricClient;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		confluentCloudMetricService = new ConfluentCloudMetricService(clusterMetricClient, 12);
	}

	@Test
	public void returnEmptyImmutableMapWhenNoDataFoundRequestBytes() throws KafkaShowBackDemoException {

		when(clusterMetricClient.getMetricDataByClusterAndType(any())).thenReturn(Collections.EMPTY_SET);

		final ImmutableMap<Instant, List<MetricInformation>> requestByUserAccount = confluentCloudMetricService
				.getServicesAccountRequestBytesByClusterIdGroupedByHour(CLUSTER_ID_TEST, INSTANT_START_TEST, INSTANT_END_TEST);

		assertTrue(requestByUserAccount.isEmpty());
	}


	@Test
	public void returnEmptyImmutableMapWhenNoDataFoundResponseBytes() throws KafkaShowBackDemoException {

		when(clusterMetricClient.getMetricDataByClusterAndType(any())).thenReturn(Collections.EMPTY_SET);

		final ImmutableMap<Instant, List<MetricInformation>> requestByUserAccount = confluentCloudMetricService
				.getServicesAccountResponseBytesByClusterIdGroupedByHour(CLUSTER_ID_TEST, INSTANT_START_TEST, INSTANT_END_TEST);

		assertTrue(requestByUserAccount.isEmpty());
	}


	@Test
	public void returnImmutableMapWhenDataFoundRequestBytes() throws KafkaShowBackDemoException {

		final Instant instant1 = Instant.parse("2023-07-29T11:00:00-05:00");
		final Instant instant2 = Instant.parse("2023-07-29T12:00:00-05:00");

		final Set<MetricDataItem> metricDataItemSet = Set.of(
				new MetricDataItem(111111d, "sa-xxx1", instant1),
				new MetricDataItem(222222d, "sa-xxx2", instant1),
				new MetricDataItem(333333d, "sa-xxx3", instant2)
		);

		when(clusterMetricClient.getMetricDataByClusterAndType(any())).thenReturn(metricDataItemSet);

		final ImmutableMap<Instant, List<MetricInformation>> requestByUserAccount = confluentCloudMetricService
				.getServicesAccountRequestBytesByClusterIdGroupedByHour(CLUSTER_ID_TEST, INSTANT_START_TEST, INSTANT_END_TEST);

		assertResponseInBytesRequest(requestByUserAccount, instant1, instant2);

	}

	@Test
	public void returnImmutableMapWhenDataFoundResponseBytes() throws KafkaShowBackDemoException {
		final Instant instant1 = Instant.parse("2023-07-29T11:00:00-05:00");
		final Instant instant2 = Instant.parse("2023-07-29T12:00:00-05:00");

		final Set<MetricDataItem> metricDataItemSet = Set.of(
				new MetricDataItem(111111d, "sa-xxx1", instant1),
				new MetricDataItem(222222d, "sa-xxx2", instant1),
				new MetricDataItem(333333d, "sa-xxx3", instant2)
		);

		when(clusterMetricClient.getMetricDataByClusterAndType(any())).thenReturn(metricDataItemSet);

		final ImmutableMap<Instant, List<MetricInformation>> responseBytesByUserAccount = confluentCloudMetricService
				.getServicesAccountRequestBytesByClusterIdGroupedByHour(CLUSTER_ID_TEST, INSTANT_START_TEST, INSTANT_END_TEST);

		assertResponseInBytesRequest(responseBytesByUserAccount, instant1, instant2);
	}

	@Test
	public void returnImmutableMapWhenDataFoundInSentBytes() throws KafkaShowBackDemoException {
		final Instant instant1 = Instant.parse("2023-07-29T11:00:00-05:00");
		final Instant instant2 = Instant.parse("2023-07-29T12:00:00-05:00");

		final Set<MetricDataItem> metricDataItemSet = Set.of(
				new MetricDataItem(7.96825630388d, null, instant1),
				new MetricDataItem(7.87897902480d, null, instant2)
		);

		when(clusterMetricClient.getMetricDataByClusterAndType(any())).thenReturn(metricDataItemSet);

		final ImmutableMap<Instant, BigDecimal> sendBytesByCluster = confluentCloudMetricService
				.getSendBytesByClusterGroupByHour(CLUSTER_ID_TEST, INSTANT_START_TEST, INSTANT_END_TEST);

		assertEquals(sendBytesByCluster.size(), 2);

		assertTrue(sendBytesByCluster.containsKey(instant1));
		assertTrue(sendBytesByCluster.containsKey(instant2));

		assertTrue(sendBytesByCluster.get(instant1).compareTo(new BigDecimal("7.96825630388")) == 0);
		assertTrue(sendBytesByCluster.get(instant2).compareTo(new BigDecimal("7.87897902480")) == 0);
	}

	@Test
	public void returnImmutableMapWhenDataFoundInReceivedBytes() throws KafkaShowBackDemoException {
		final Instant instant1 = Instant.parse("2023-07-29T11:00:00-05:00");
		final Instant instant2 = Instant.parse("2023-07-29T12:00:00-05:00");

		final Set<MetricDataItem> metricDataItemSet = Set.of(
				new MetricDataItem(9.96825630388d, null, instant1),
				new MetricDataItem(7.87897902480d, null, instant2)
		);

		when(clusterMetricClient.getMetricDataByClusterAndType(any())).thenReturn(metricDataItemSet);

		final ImmutableMap<Instant, BigDecimal> receivedBytesByCluster = confluentCloudMetricService
				.getReceiveBytesByClusterGroupByHour(CLUSTER_ID_TEST, INSTANT_START_TEST, INSTANT_END_TEST);

		assertEquals(receivedBytesByCluster.size(), 2);

		assertTrue(receivedBytesByCluster.containsKey(instant1));
		assertTrue(receivedBytesByCluster.containsKey(instant2));

		assertTrue(receivedBytesByCluster.get(instant1).compareTo(new BigDecimal("9.96825630388")) == 0);
		assertTrue(receivedBytesByCluster.get(instant2).compareTo(new BigDecimal("7.87897902480")) == 0);
	}

	@Test
	public void notCallClientMetricsWhenTheResultHadBeenCache() throws KafkaShowBackDemoException {
		final Instant instant1 = Instant.parse("2023-07-29T11:00:00-05:00");
		final Instant instant2 = Instant.parse("2023-07-29T12:00:00-05:00");

		final Set<MetricDataItem> metricDataItemSet = Set.of(
				new MetricDataItem(111111d, "sa-xxx1", instant1),
				new MetricDataItem(222222d, "sa-xxx2", instant1),
				new MetricDataItem(333333d, "sa-xxx3", instant2)
		);

		when(clusterMetricClient.getMetricDataByClusterAndType(any())).thenReturn(metricDataItemSet);

		confluentCloudMetricService
				.getServicesAccountRequestBytesByClusterIdGroupedByHour(CLUSTER_ID_TEST, INSTANT_START_TEST, INSTANT_END_TEST);

		confluentCloudMetricService
				.getServicesAccountRequestBytesByClusterIdGroupedByHour(CLUSTER_ID_TEST, INSTANT_START_TEST, INSTANT_END_TEST);

		//we call two times the method but the client metric API just it has been call 1, the result it has been stored in the cache
		verify(clusterMetricClient, times(1)).getMetricDataByClusterAndType(any(MetricBody.class));
	}

	private void assertResponseInBytesRequest(final ImmutableMap<Instant, List<MetricInformation>> requestByUserAccount, final Instant instant1, final Instant instant2) {

		assertEquals(requestByUserAccount.size(), 2);

		assertTrue(requestByUserAccount.containsKey(instant1));
		assertTrue(requestByUserAccount.containsKey(instant2));

		assertEquals(requestByUserAccount.get(instant1).size(), 2);
		assertEquals(requestByUserAccount.get(instant2).size(), 1);

		assertTrue(requestByUserAccount.get(instant1).stream().anyMatch(sai -> sai.metricIdentifier().equals("sa-xxx1")
				&& sai.value().compareTo(new BigDecimal("111111")) == 0));
		assertTrue(requestByUserAccount.get(instant1).stream().anyMatch(sai -> sai.metricIdentifier().equals("sa-xxx2")
				&& sai.value().compareTo(new BigDecimal("222222")) == 0));
		assertTrue(requestByUserAccount.get(instant2).stream().anyMatch(sai -> sai.metricIdentifier().equals("sa-xxx3")
				&& sai.value().compareTo(new BigDecimal("333333")) == 0));
	}
}
