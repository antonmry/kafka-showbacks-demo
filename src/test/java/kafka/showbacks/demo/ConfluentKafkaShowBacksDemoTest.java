package kafka.showbacks.demo;

import com.google.common.collect.ImmutableMap;
import kafka.showbacks.demo.clouddata.billing.ConfluentCloudCostService;
import kafka.showbacks.demo.clouddata.serviceaccount.ConfluentCloudServiceAccountCache;
import kafka.showbacks.demo.clouddata.serviceaccount.ServiceAccountClusterInformation;
import kafka.showbacks.demo.clustermetrics.ClusterMetricService;
import kafka.showbacks.demo.clustermetrics.MetricInformation;
import kafka.showbacks.demo.common.exception.KafkaShowBackDemoException;
import kafka.showbacks.demo.common.model.ClusterCostData;
import kafka.showbacks.demo.common.model.CostType;
import kafka.showbacks.demo.common.model.TeamCostData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import static kafka.showbacks.demo.common.model.CostType.KAFKA_CONNECT_NUM_TASKS;
import static kafka.showbacks.demo.common.model.CostType.KAFKA_NETWORK_READ;
import static kafka.showbacks.demo.common.model.CostType.KAFKA_NETWORK_WRITE;
import static kafka.showbacks.demo.common.model.CostType.KAFKA_NUM_CKUS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConfluentKafkaShowBacksDemoTest {
	private static final String DEFAULT_ORGANIZATION = "KPT";
	private static final String DEFAULT_APPLICATION = "kafka_platform";

	private static final String CLUSTER_TEST_ID = "lkc-xxxx";
	private static final String SERVICE_ACCOUNT_TEST_ID_1 = "sa-xxxx-1";
	private static final String SERVICE_ACCOUNT_TEST_ID_2 = "sa-xxxx-2";
	private static final String SERVICE_ACCOUNT_TEST_ID_3 = "sa-xxxx-3";

	private static final String TOPIC_TEST_ID_1 = "topics-1";
	private static final String TOPIC_TEST_ID_2 = "topics-2";
	private static final String TOPIC_TEST_ID_3 = "topics-3";

	private static final String ORGANIZATION_TEST_1 = "test_organization_1";
	private static final String ORGANIZATION_TEST_2 = "test_organization_2";
	private static final String ORGANIZATION_TEST_3 = "test_organization_3";

	private static final Instant START_TIME_TEST = Instant.parse("2023-08-28T11:00:00-05:00");

	private static final int DEFAULT_SCALE_TO_TEST = 2;

	private KafkaShowBacksDemo kafkaShowBacksDemo;
	@Mock
	private ClusterMetricService confluentCloudMetricService;
	@Mock
	private ConfluentCloudServiceAccountCache confluentServiceAccountCache;
	@Mock
	private ConfluentCloudCostService cloudCostService;


	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		kafkaShowBacksDemo = new ConfluentKafkaShowBacksDemo(confluentCloudMetricService, confluentServiceAccountCache, cloudCostService);
	}

	@Test
	public void returnEmptyCollectionWhenNotFoundServiceAccounts() throws KafkaShowBackDemoException {
		final ClusterCostData clusterCostData = createDataTestClusterCostData(KAFKA_NUM_CKUS, new BigDecimal("4000"));
		when(confluentServiceAccountCache.getServiceAccountInformation()).thenReturn(ImmutableMap.of());
		assertThrows(KafkaShowBackDemoException.class, () -> {
			kafkaShowBacksDemo.getCostDividedByTeams(Set.of(clusterCostData));
		});
		verify(confluentCloudMetricService, never()).getServicesAccountRequestBytesByClusterIdGroupedByHour(clusterCostData.clusterID(), clusterCostData.startPeriod(), clusterCostData.endPeriod());
	}

	@Test
	public void returnAllCostToDefaultPlatformWhenNotFoundRequestBytesByServiceAccounts() throws KafkaShowBackDemoException {
		final ClusterCostData clusterCostData = createDataTestClusterCostData(KAFKA_NUM_CKUS, new BigDecimal("76"));
		when(confluentServiceAccountCache.getServiceAccountInformation()).thenReturn(createDataTestServiceAccountInfo());
		when(confluentCloudMetricService
				.getServicesAccountRequestBytesByClusterIdGroupedByHour(clusterCostData.clusterID(), clusterCostData.startPeriod(), clusterCostData.endPeriod()))
				.thenReturn(ImmutableMap.of());
		when(confluentCloudMetricService
				.getReceiveBytesByClusterGroupByHour(clusterCostData.clusterID(), clusterCostData.startPeriod(), clusterCostData.endPeriod()))
				.thenReturn(ImmutableMap.of(START_TIME_TEST, new BigDecimal("5.123064149E9")));
		when(confluentCloudMetricService
				.getSendBytesByClusterGroupByHour(clusterCostData.clusterID(), clusterCostData.startPeriod(), clusterCostData.endPeriod()))
				.thenReturn(ImmutableMap.of(START_TIME_TEST, new BigDecimal("5.8985787245E10")));

		final Set<TeamCostData> teamCostData = kafkaShowBacksDemo.getCostDividedByTeams(Set.of(clusterCostData));

		assertTrue(teamCostData.size() == 1);


		verify(confluentCloudMetricService, times(1)).getReceiveBytesByClusterGroupByHour(clusterCostData.clusterID(), clusterCostData.startPeriod(), clusterCostData.endPeriod());
		verify(confluentCloudMetricService, times(1)).getSendBytesByClusterGroupByHour(clusterCostData.clusterID(), clusterCostData.startPeriod(), clusterCostData.endPeriod());
		verify(confluentCloudMetricService, times(1)).getServicesAccountRequestBytesByClusterIdGroupedByHour(clusterCostData.clusterID(), clusterCostData.startPeriod(), clusterCostData.endPeriod());
	}

	@Test
	public void returnEmptyCollectionWhenNotFoundIngressAndEgress() throws KafkaShowBackDemoException {
		final ClusterCostData clusterCostData = createDataTestClusterCostData(KAFKA_NUM_CKUS, new BigDecimal("4000"));
		when(confluentServiceAccountCache.getServiceAccountInformation()).thenReturn(createDataTestServiceAccountInfo());
		when(confluentCloudMetricService
				.getServicesAccountRequestBytesByClusterIdGroupedByHour(clusterCostData.clusterID(), clusterCostData.startPeriod(), clusterCostData.endPeriod()))
				.thenReturn(createDataTestBytesByUserAccount());
		when(confluentCloudMetricService
				.getSendBytesByClusterGroupByHour(clusterCostData.clusterID(), clusterCostData.startPeriod(), clusterCostData.endPeriod()))
				.thenReturn(ImmutableMap.of());
		when(confluentCloudMetricService
				.getReceiveBytesByClusterGroupByHour(clusterCostData.clusterID(), clusterCostData.startPeriod(), clusterCostData.endPeriod()))
				.thenReturn(ImmutableMap.of());

		final Set<TeamCostData> teamCostData = kafkaShowBacksDemo.getCostDividedByTeams(Set.of(clusterCostData));

		assertAllCostToDefaultApplication(teamCostData, clusterCostData);

		verify(confluentCloudMetricService, times(1)).getSendBytesByClusterGroupByHour(clusterCostData.clusterID(), clusterCostData.startPeriod(), clusterCostData.endPeriod());
		verify(confluentCloudMetricService, times(1)).getReceiveBytesByClusterGroupByHour(clusterCostData.clusterID(), clusterCostData.startPeriod(), clusterCostData.endPeriod());
		verify(confluentCloudMetricService, times(1)).getServicesAccountRequestBytesByClusterIdGroupedByHour(clusterCostData.clusterID(), clusterCostData.startPeriod(), clusterCostData.endPeriod());
	}

	@Test
	public void calculateOKNumCKUCostDependingOfUsage() throws KafkaShowBackDemoException {
		final ClusterCostData clusterCostData = createDataTestClusterCostData(KAFKA_NUM_CKUS, new BigDecimal("64"));
		when(confluentServiceAccountCache.getServiceAccountInformation()).thenReturn(createDataTestServiceAccountInfo());
		when(confluentCloudMetricService
				.getServicesAccountRequestBytesByClusterIdGroupedByHour(clusterCostData.clusterID(), clusterCostData.startPeriod(), clusterCostData.endPeriod()))
				.thenReturn(createDataTestBytesByUserAccount());
		when(confluentCloudMetricService
				.getReceiveBytesByClusterGroupByHour(clusterCostData.clusterID(), clusterCostData.startPeriod(), clusterCostData.endPeriod()))
				.thenReturn(ImmutableMap.of(START_TIME_TEST, new BigDecimal("5.123064149E9")));
		when(confluentCloudMetricService
				.getSendBytesByClusterGroupByHour(clusterCostData.clusterID(), clusterCostData.startPeriod(), clusterCostData.endPeriod()))
				.thenReturn(ImmutableMap.of(START_TIME_TEST, new BigDecimal("5.8985787245E10")));

		final Set<TeamCostData> teamCostDataSet = kafkaShowBacksDemo.getCostDividedByTeams(Set.of(clusterCostData));

		verify(confluentCloudMetricService, times(1)).getReceiveBytesByClusterGroupByHour(clusterCostData.clusterID(), clusterCostData.startPeriod(), clusterCostData.endPeriod());
		verify(confluentCloudMetricService, times(1)).getSendBytesByClusterGroupByHour(clusterCostData.clusterID(), clusterCostData.startPeriod(), clusterCostData.endPeriod());
		verify(confluentCloudMetricService, times(1)).getServicesAccountRequestBytesByClusterIdGroupedByHour(clusterCostData.clusterID(), clusterCostData.startPeriod(), clusterCostData.endPeriod());

		assertEquals(teamCostDataSet.size(), 4);

		assertCostByNumCKUs(teamCostDataSet, clusterCostData);
	}

	@Test
	public void returnEmptyCollectionWhenNoResponseBytesFound() throws KafkaShowBackDemoException {
		final ClusterCostData clusterCostData = createDataTestClusterCostData(KAFKA_NETWORK_READ, new BigDecimal("4000"));
		when(confluentServiceAccountCache.getServiceAccountInformation()).thenReturn(createDataTestServiceAccountInfo());
		when(confluentCloudMetricService
				.getServicesAccountResponseBytesByClusterIdGroupedByHour(clusterCostData.clusterID(), clusterCostData.startPeriod(), clusterCostData.endPeriod()))
				.thenReturn(ImmutableMap.of());

		final Set<TeamCostData> teamCostDataSet = kafkaShowBacksDemo.getCostDividedByTeams(Set.of(clusterCostData));

		assertAllCostToDefaultApplication(teamCostDataSet, clusterCostData);
	}

	@Test
	public void calculateOKNumNetWorkReadCostDependingOfUsage() throws KafkaShowBackDemoException {
		final ClusterCostData clusterCostData = createDataTestClusterCostData(KAFKA_NETWORK_READ, new BigDecimal("4000"));
		when(confluentServiceAccountCache.getServiceAccountInformation()).thenReturn(createDataTestServiceAccountInfo());
		when(confluentCloudMetricService
				.getServicesAccountResponseBytesByClusterIdGroupedByHour(clusterCostData.clusterID(), clusterCostData.startPeriod(), clusterCostData.endPeriod()))
				.thenReturn(createDataTestBytesByUserAccount());

		final Set<TeamCostData> teamCostDataSet = kafkaShowBacksDemo.getCostDividedByTeams(Set.of(clusterCostData));

		assertEquals(teamCostDataSet.size(), 4);

		assertCostByNetworkType(teamCostDataSet, clusterCostData);
	}


	@Test
	public void calculateOKNumNetWorkWriteCostDependingOfUsage() throws KafkaShowBackDemoException {
		final ClusterCostData clusterCostData = createDataTestClusterCostData(KAFKA_NETWORK_WRITE, new BigDecimal("4000"));
		when(confluentServiceAccountCache.getServiceAccountInformation()).thenReturn(createDataTestServiceAccountInfo());
		when(confluentCloudMetricService
				.getServicesAccountRequestBytesByClusterIdGroupedByHour(clusterCostData.clusterID(), clusterCostData.startPeriod(), clusterCostData.endPeriod()))
				.thenReturn(createDataTestBytesByUserAccount());

		final Set<TeamCostData> teamCostDataSet = kafkaShowBacksDemo.getCostDividedByTeams(Set.of(clusterCostData));

		assertEquals(teamCostDataSet.size(), 4);

		assertCostByNetworkType(teamCostDataSet, clusterCostData);
	}

	@Test
	public void returnCorrectlyMoreThanOneCostTypeAndCluster() throws KafkaShowBackDemoException {
		final ClusterCostData clusterCostDataNumCKUs = createDataTestClusterCostData(KAFKA_NUM_CKUS, new BigDecimal("64"));
		final ClusterCostData clusterCostDataNetWorkRead = createDataTestClusterCostData(KAFKA_NETWORK_READ, new BigDecimal("4000"));
		final ClusterCostData clusterCostDataNetWorkWrite = createDataTestClusterCostData(KAFKA_NETWORK_WRITE, new BigDecimal("4000"));


		final Set<ClusterCostData> clusterCostDataSet = Set.of(clusterCostDataNumCKUs, clusterCostDataNetWorkRead, clusterCostDataNetWorkWrite);
		when(confluentServiceAccountCache.getServiceAccountInformation()).thenReturn(createDataTestServiceAccountInfo());
		when(confluentCloudMetricService
				.getServicesAccountResponseBytesByClusterIdGroupedByHour(clusterCostDataNetWorkRead.clusterID(), clusterCostDataNetWorkRead.startPeriod(), clusterCostDataNetWorkRead.endPeriod()))
				.thenReturn(createDataTestBytesByUserAccount());
		when(confluentCloudMetricService
				.getServicesAccountRequestBytesByClusterIdGroupedByHour(clusterCostDataNumCKUs.clusterID(), clusterCostDataNumCKUs.startPeriod(), clusterCostDataNumCKUs.endPeriod()))
				.thenReturn(createDataTestBytesByUserAccount());
		when(confluentCloudMetricService
				.getReceiveBytesByClusterGroupByHour(clusterCostDataNumCKUs.clusterID(), clusterCostDataNumCKUs.startPeriod(), clusterCostDataNumCKUs.endPeriod()))
				.thenReturn(ImmutableMap.of(START_TIME_TEST, new BigDecimal("5.123064149E9")));
		when(confluentCloudMetricService
				.getSendBytesByClusterGroupByHour(clusterCostDataNumCKUs.clusterID(), clusterCostDataNumCKUs.startPeriod(), clusterCostDataNumCKUs.endPeriod()))
				.thenReturn(ImmutableMap.of(START_TIME_TEST, new BigDecimal("5.8985787245E10")));

		final Set<TeamCostData> teamCostDataSet = kafkaShowBacksDemo.getCostDividedByTeams(clusterCostDataSet);

		assertEquals(teamCostDataSet.size(), 12);

		assertCostByNumCKUs(teamCostDataSet, clusterCostDataNumCKUs);

		assertCostByNetworkType(teamCostDataSet, clusterCostDataNetWorkRead);

		assertCostByNetworkType(teamCostDataSet, clusterCostDataNetWorkWrite);
	}

	@Test
	public void returnTotalCostAndDefaultOrganizationWhenTheCalculationOfCostIsNotDefined() throws KafkaShowBackDemoException {
		final ClusterCostData clusterCostDataConnectNumTasks = createDataTestClusterCostData(KAFKA_CONNECT_NUM_TASKS, new BigDecimal("4000"));
		final Set<ClusterCostData> clusterCostDataSet = Set.of(clusterCostDataConnectNumTasks);

		when(confluentServiceAccountCache.getServiceAccountInformation()).thenReturn(createDataTestServiceAccountInfo());

		final Set<TeamCostData> teamCostDataSet = kafkaShowBacksDemo.getCostDividedByTeams(clusterCostDataSet);

		assertEquals(teamCostDataSet.size(), 1);

		assertAllCostToDefaultApplication(teamCostDataSet, clusterCostDataConnectNumTasks);
	}


	private void assertCostByNetworkType(final Set<TeamCostData> teamCostDataSet, final ClusterCostData clusterCostData) {
		assertTrue(teamCostDataSet.stream().anyMatch(tcd -> tcd.organization().equals(ORGANIZATION_TEST_1)
				&& tcd.teamCost().setScale(DEFAULT_SCALE_TO_TEST, RoundingMode.HALF_EVEN).compareTo(new BigDecimal("3378.38")) == 0
				&& tcd.teamUsage().setScale(DEFAULT_SCALE_TO_TEST, RoundingMode.HALF_EVEN).compareTo(new BigDecimal("1081.08")) == 0
				&& tcd.startPeriod().equals(clusterCostData.startPeriod())
				&& tcd.endPeriod().equals(clusterCostData.endPeriod())
				&& tcd.costType().equals(clusterCostData.costType())));
		assertTrue(teamCostDataSet.stream().anyMatch(tcd -> tcd.organization().equals(ORGANIZATION_TEST_2)
				&& tcd.teamCost().setScale(DEFAULT_SCALE_TO_TEST, RoundingMode.HALF_EVEN).compareTo(new BigDecimal("1689.19")) == 0
				&& tcd.teamUsage().setScale(DEFAULT_SCALE_TO_TEST, RoundingMode.HALF_EVEN).compareTo(new BigDecimal("540.54")) == 0
				&& tcd.startPeriod().equals(clusterCostData.startPeriod())
				&& tcd.endPeriod().equals(clusterCostData.endPeriod())
				&& tcd.costType().equals(clusterCostData.costType())));
		assertTrue(teamCostDataSet.stream().anyMatch(tcd -> tcd.organization().equals(ORGANIZATION_TEST_3)
				&& tcd.teamCost().setScale(DEFAULT_SCALE_TO_TEST, RoundingMode.HALF_EVEN).compareTo(new BigDecimal("7432.43")) == 0
				&& tcd.teamUsage().setScale(DEFAULT_SCALE_TO_TEST, RoundingMode.HALF_EVEN).compareTo(new BigDecimal("2378.38")) == 0
				&& tcd.startPeriod().equals(clusterCostData.startPeriod())
				&& tcd.endPeriod().equals(clusterCostData.endPeriod())
				&& tcd.costType().equals(clusterCostData.costType())));
		assertTrue(teamCostDataSet.stream().anyMatch(tcd -> tcd.organization().equals(DEFAULT_ORGANIZATION)
				&& tcd.teamCost().setScale(DEFAULT_SCALE_TO_TEST, RoundingMode.HALF_EVEN).compareTo(new BigDecimal("0.00")) == 0
				&& tcd.teamUsage().setScale(DEFAULT_SCALE_TO_TEST, RoundingMode.HALF_EVEN).compareTo(new BigDecimal("0.00")) == 0
				&& tcd.startPeriod().equals(clusterCostData.startPeriod())
				&& tcd.endPeriod().equals(clusterCostData.endPeriod())
				&& tcd.costType().equals(clusterCostData.costType())));
	}

	private void assertCostByNumCKUs(final Set<TeamCostData> teamCostDataSet, final ClusterCostData clusterCostData) {
		assertTrue(teamCostDataSet.stream().anyMatch(tcd -> tcd.organization().equals(ORGANIZATION_TEST_1)
				&& tcd.teamCost().setScale(DEFAULT_SCALE_TO_TEST, RoundingMode.HALF_EVEN).compareTo(new BigDecimal("3371.73")) == 0
				&& tcd.teamUsage().setScale(DEFAULT_SCALE_TO_TEST, RoundingMode.HALF_EVEN).compareTo(new BigDecimal("17.26")) == 0
				&& tcd.startPeriod().equals(clusterCostData.startPeriod())
				&& tcd.endPeriod().equals(clusterCostData.endPeriod())
				&& tcd.costType().equals(KAFKA_NUM_CKUS)));
		assertTrue(teamCostDataSet.stream().anyMatch(tcd -> tcd.organization().equals(ORGANIZATION_TEST_2)
				&& tcd.teamCost().setScale(DEFAULT_SCALE_TO_TEST, RoundingMode.HALF_EVEN).compareTo(new BigDecimal("1685.86")) == 0
				&& tcd.teamUsage().setScale(DEFAULT_SCALE_TO_TEST, RoundingMode.HALF_EVEN).compareTo(new BigDecimal("8.63")) == 0
				&& tcd.startPeriod().equals(clusterCostData.startPeriod())
				&& tcd.endPeriod().equals(clusterCostData.endPeriod())
				&& tcd.costType().equals(KAFKA_NUM_CKUS)));
		assertTrue(teamCostDataSet.stream().anyMatch(tcd -> tcd.organization().equals(ORGANIZATION_TEST_3)
				&& tcd.teamCost().setScale(DEFAULT_SCALE_TO_TEST, RoundingMode.HALF_EVEN).compareTo(new BigDecimal("7417.80")) == 0
				&& tcd.teamUsage().setScale(DEFAULT_SCALE_TO_TEST, RoundingMode.HALF_EVEN).compareTo(new BigDecimal("37.98")) == 0
				&& tcd.startPeriod().equals(clusterCostData.startPeriod())
				&& tcd.endPeriod().equals(clusterCostData.endPeriod())
				&& tcd.costType().equals(KAFKA_NUM_CKUS)));
		assertTrue(teamCostDataSet.stream().anyMatch(tcd -> tcd.organization().equals(DEFAULT_ORGANIZATION)
				&& tcd.teamCost().setScale(DEFAULT_SCALE_TO_TEST, RoundingMode.HALF_EVEN).compareTo(new BigDecimal("24.62")) == 0
				&& tcd.teamUsage().setScale(DEFAULT_SCALE_TO_TEST, RoundingMode.HALF_EVEN).compareTo(new BigDecimal("0.13")) == 0
				&& tcd.startPeriod().equals(clusterCostData.startPeriod())
				&& tcd.endPeriod().equals(clusterCostData.endPeriod())
				&& tcd.costType().equals(KAFKA_NUM_CKUS)));
	}

	private void assertAllCostToDefaultApplication(final Set<TeamCostData> teamCostDataSet, final ClusterCostData clusterCostData) {
		assertTrue(teamCostDataSet.stream().anyMatch(costData -> costData.costType().equals(clusterCostData.costType())));
		assertTrue(teamCostDataSet.stream().anyMatch(costData -> costData.teamCost().equals(clusterCostData.clusterTotalCost())));
		assertTrue(teamCostDataSet.stream().anyMatch(costData -> costData.teamUsage().equals(clusterCostData.clusterTotalUsage())));
		assertTrue(teamCostDataSet.stream().anyMatch(costData -> costData.organization().equals(DEFAULT_ORGANIZATION)));
		assertTrue(teamCostDataSet.stream().anyMatch(costData -> costData.application().equals(DEFAULT_APPLICATION)));
		assertTrue(teamCostDataSet.stream().anyMatch(costData -> costData.startPeriod().equals(clusterCostData.startPeriod())));
		assertTrue(teamCostDataSet.stream().anyMatch(costData -> costData.endPeriod().equals(clusterCostData.endPeriod())));
	}

	private ClusterCostData createDataTestClusterCostData(final CostType costType, final BigDecimal totalUsage) {
		Instant end = Instant.parse("2023-07-29T11:59:00-05:00");
		return new ClusterCostData(costType, new BigDecimal("12500"), totalUsage, CLUSTER_TEST_ID, START_TIME_TEST, end);
	}

	private ImmutableMap<String, ServiceAccountClusterInformation> createDataTestServiceAccountInfo() {
		return ImmutableMap.of(SERVICE_ACCOUNT_TEST_ID_1,
				new ServiceAccountClusterInformation(CLUSTER_TEST_ID,
						ORGANIZATION_TEST_1,
						"test_application_1"
				),
				SERVICE_ACCOUNT_TEST_ID_2,
				new ServiceAccountClusterInformation(CLUSTER_TEST_ID,
						ORGANIZATION_TEST_2,
						"test_application_2"),
				SERVICE_ACCOUNT_TEST_ID_3,
				new ServiceAccountClusterInformation(CLUSTER_TEST_ID,
						ORGANIZATION_TEST_3,
						"test_application_3"));
	}

	private ImmutableMap<Instant, List<MetricInformation>> createDataTestBytesByUserAccount() {
		return ImmutableMap.of(START_TIME_TEST,
				List.of(new MetricInformation(SERVICE_ACCOUNT_TEST_ID_1, START_TIME_TEST, new BigDecimal("1000")),
						new MetricInformation(SERVICE_ACCOUNT_TEST_ID_2, START_TIME_TEST, new BigDecimal("500")),
						new MetricInformation(SERVICE_ACCOUNT_TEST_ID_3, START_TIME_TEST, new BigDecimal("2200"))));
	}

	private ImmutableMap<Instant, List<MetricInformation>> createDataTestBytesByTopic() {
		return ImmutableMap.of(START_TIME_TEST,
				List.of(new MetricInformation(TOPIC_TEST_ID_1, START_TIME_TEST, new BigDecimal("1000")),
						new MetricInformation(TOPIC_TEST_ID_2, START_TIME_TEST, new BigDecimal("500")),
						new MetricInformation(TOPIC_TEST_ID_3, START_TIME_TEST, new BigDecimal("2200"))));
	}

}
