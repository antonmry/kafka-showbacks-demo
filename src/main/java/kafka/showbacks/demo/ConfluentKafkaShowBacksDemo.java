package kafka.showbacks.demo;

import com.google.common.collect.ImmutableMap;
import kafka.showbacks.demo.clouddata.billing.ConfluentCloudCostService;
import kafka.showbacks.demo.clouddata.serviceaccount.ConfluentCloudServiceAccountCache;
import kafka.showbacks.demo.clouddata.serviceaccount.ServiceAccountClusterInformation;
import kafka.showbacks.demo.clustermetrics.ClusterMetricService;
import kafka.showbacks.demo.clustermetrics.MetricInformation;
import kafka.showbacks.demo.common.BigDecimalOperations;
import kafka.showbacks.demo.common.exception.KafkaShowBackDemoException;
import kafka.showbacks.demo.common.model.ClusterCostData;
import kafka.showbacks.demo.common.model.TeamCostData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static kafka.showbacks.demo.CostType.KAFKA_NETWORK_READ;
import static kafka.showbacks.demo.CostType.KAFKA_NETWORK_WRITE;
import static kafka.showbacks.demo.common.BigDecimalOperations.IS_EQUAL_BIG_DECIMAL;
import static kafka.showbacks.demo.common.BigDecimalOperations.IS_GREATER_BIG_DECIMAL;
import static kafka.showbacks.demo.common.BigDecimalOperations.IS_LESS_BIG_DECIMAL;
import static kafka.showbacks.demo.common.BigDecimalOperations.add;
import static kafka.showbacks.demo.common.BigDecimalOperations.divide;
import static kafka.showbacks.demo.common.BigDecimalOperations.getPercentage;
import static kafka.showbacks.demo.common.BigDecimalOperations.getValueFromPercentage;
import static kafka.showbacks.demo.common.BigDecimalOperations.multiply;
import static kafka.showbacks.demo.common.BigDecimalOperations.subtract;

//todo big class refactor
//todo number classes study
public final class ConfluentKafkaShowBacksDemo implements KafkaShowBacksDemo {

	private static final Logger log = LoggerFactory.getLogger(ConfluentKafkaShowBacksDemo.class);

	private static final String DEFAULT_ORGANIZATION = "KPT";
	private static final String DEFAULT_APPLICATION = "kafka_platform";

	private static final BigDecimal DEFAULT_CKU_LIMIT_INGRESS_IN_BYTES = multiply(new BigDecimal("65"), new BigDecimal("1000000"));
	private static final BigDecimal DEFAULT_CKU_LIMIT_EGRESS_IN_BYTES = multiply(new BigDecimal("130"), new BigDecimal("1000000"));
	private static final BigDecimal DEFAULT_SECONDS_HOUR = new BigDecimal("3600");

	private ImmutableMap<String, ServiceAccountClusterInformation> serviceAccountInformationMap = ImmutableMap.of();

	private final Map<String, ImmutableMap<Instant, List<MetricInformation>>> responseByUsersAccountsGroupByStartTime;

	private final Map<String, ImmutableMap<Instant, List<MetricInformation>>> requestByUsersAccountsGroupByStartTime;

	private final Map<String, ImmutableMap<Instant, BigDecimal>> ingressGroupedByHourAndCluster;

	private final Map<String, ImmutableMap<Instant, BigDecimal>> egressGroupedByHourAndCluster;

	private final ClusterMetricService confluentCloudMetricService;
	private final ConfluentCloudServiceAccountCache confluentServiceAccountCache;
	private final ConfluentCloudCostService confluentCloudCostService;

	@Inject
	ConfluentKafkaShowBacksDemo(final ClusterMetricService confluentCloudMetricService,
	                            final ConfluentCloudServiceAccountCache confluentServiceAccountCache,
	                            final ConfluentCloudCostService confluentCloudCostService) {
		this.confluentCloudMetricService = confluentCloudMetricService;
		this.confluentServiceAccountCache = confluentServiceAccountCache;
		this.confluentCloudCostService = confluentCloudCostService;

		this.responseByUsersAccountsGroupByStartTime = new HashMap<>();
		this.requestByUsersAccountsGroupByStartTime = new HashMap<>();
		this.ingressGroupedByHourAndCluster = new HashMap<>();
		this.egressGroupedByHourAndCluster = new HashMap<>();
	}

	@Override
	public Set<ClusterCostData> getCostDataByDate(final LocalDate startDate, final LocalDate endDate) throws KafkaShowBackDemoException {
		return this.confluentCloudCostService.getCostDataByTimeRange(startDate, endDate);
	}

	@Override
	public Set<TeamCostData> getCostDividedByTeams(final Set<ClusterCostData> clusterCostDataSet) throws KafkaShowBackDemoException {
		log.info("Staring process to get cost by teams number records to process {}.", clusterCostDataSet.size());

		this.serviceAccountInformationMap = confluentServiceAccountCache.getServiceAccountInformation();

		if (serviceAccountInformationMap.isEmpty()) {
			throw new KafkaShowBackDemoException("We can not calculate the team cost due " +
					"we can not recover service account information");
		}

		final Set<TeamCostData> teamCostDataSet = new HashSet<>();

		fillMetricsNeededToCalculateTheCosts(clusterCostDataSet);

		for (ClusterCostData clusterCostData : clusterCostDataSet) {

			log.debug("Calculating cost: {},cluster: {},startPeriod: {}", clusterCostData.costType(),
					clusterCostData.clusterID(),
					clusterCostData.startPeriod());

			switch (clusterCostData.costType()) {
				case KAFKA_NUM_CKUS -> {
					if (!requestByUsersAccountsGroupByStartTime.isEmpty()
							&& !ingressGroupedByHourAndCluster.isEmpty()
							&& !egressGroupedByHourAndCluster.isEmpty()) {
						teamCostDataSet.addAll(getTeamCostByNumOfCKUS(clusterCostData));
					} else {
						teamCostDataSet.add(addTotalCostToDefaultOrganization(clusterCostData));
					}
				}
				case KAFKA_NETWORK_READ -> {
					if (!responseByUsersAccountsGroupByStartTime.isEmpty()) {
						teamCostDataSet.addAll(getTeamCostByMetricBytesType(clusterCostData));
					} else {
						teamCostDataSet.add(addTotalCostToDefaultOrganization(clusterCostData));
					}
				}
				case KAFKA_NETWORK_WRITE -> {
					if (!requestByUsersAccountsGroupByStartTime.isEmpty()) {
						teamCostDataSet.addAll(getTeamCostByMetricBytesType(clusterCostData));
					} else {
						teamCostDataSet.add(addTotalCostToDefaultOrganization(clusterCostData));
					}
				}
				default -> teamCostDataSet.add(addTotalCostToDefaultOrganization(clusterCostData));
			}
		}

		log.info("End process to get cost by teams.");

		return teamCostDataSet;
	}

	private Set<TeamCostData> getTeamCostByNumOfCKUS(final ClusterCostData clusterCostData) {
		final BigDecimal usageClusterPercentage = applyFormulaToGetUsageClusterPercentage(clusterCostData);

		if (usageClusterPercentage.compareTo(BigDecimal.ZERO) == IS_EQUAL_BIG_DECIMAL) {
			log.warn("No usage cluster percentage found for this cluster {} in this start period {}", clusterCostData.clusterID(), clusterCostData.startPeriod());

			return Collections.EMPTY_SET;
		}
		final BigDecimal totalCostRest = multiply(clusterCostData.clusterTotalCost(), usageClusterPercentage);
		final BigDecimal totalUsageRest = multiply(clusterCostData.clusterTotalUsage(), usageClusterPercentage);

		final Set<TeamCostData> teamCostDataSet = new HashSet<>();

		final BigDecimal totalCostUsed = subtract(clusterCostData.clusterTotalCost(), totalCostRest);
		final BigDecimal totalUsageUsed = subtract(clusterCostData.clusterTotalUsage(), totalUsageRest);

		final List<MetricInformation> requestsByUsersAccounts = getMetricValueFromClusterAndStartPeriod(requestByUsersAccountsGroupByStartTime, clusterCostData.clusterID(), clusterCostData.startPeriod(), Collections.EMPTY_LIST);

		final BigDecimal totalRequestBytesUsed = requestsByUsersAccounts
				.stream().map(MetricInformation::value).reduce(BigDecimal.ZERO, BigDecimalOperations::add);
		BigDecimal sumUsageByTeams = BigDecimal.ZERO;
		BigDecimal sumCostByTeams = BigDecimal.ZERO;
		for (MetricInformation entryRequestByUseAccount : requestsByUsersAccounts) {

			if (serviceAccountInformationMap.containsKey(entryRequestByUseAccount.metricIdentifier())) {
				final ServiceAccountClusterInformation serviceAccountInformation = serviceAccountInformationMap.get(entryRequestByUseAccount.metricIdentifier());

				final BigDecimal userPercentageRequestBytesUsed = getPercentage(entryRequestByUseAccount.value(), totalRequestBytesUsed);
				final BigDecimal teamUsageCost = getValueFromPercentage(totalCostUsed, userPercentageRequestBytesUsed);
				final BigDecimal teamUsageUsed = getValueFromPercentage(totalUsageUsed, userPercentageRequestBytesUsed);

				sumCostByTeams = add(sumCostByTeams, teamUsageCost);
				sumUsageByTeams = add(sumUsageByTeams, teamUsageUsed);

				teamCostDataSet.add(new TeamCostData(clusterCostData.clusterID(),
						serviceAccountInformation.organization(),
						serviceAccountInformation.application(),
						teamUsageCost,
						teamUsageUsed,
						clusterCostData.startPeriod(),
						clusterCostData.endPeriod(),
						clusterCostData.costType()));
			}
		}

		teamCostDataSet.add(alignFinalValuesObtainedWithInitialValues(clusterCostData, sumCostByTeams, sumUsageByTeams, totalCostRest, totalUsageRest));

		return teamCostDataSet;
	}

	/**
	 * This method returns the cost by team that can be the network write / read
	 * The method to calculate these costs are the same, but the network write  use the request bytes, the network read the response bytes and the storage the retention bytes
	 * To calculate it the total cost in a period of time should be assigned to all teams consuming in the same period proportionally to the amount of data consumed.
	 */
	private Set<TeamCostData> getTeamCostByMetricBytesType(final ClusterCostData clusterCostData) {
		final Set<TeamCostData> teamCostDataSet = new HashSet<>();

		final List<MetricInformation> serviceAccountMetricInformationList = getListServiceAccountMetricInformationByNetworkType(clusterCostData);

		final BigDecimal totalBytes = serviceAccountMetricInformationList.stream().map(MetricInformation::value).reduce(BigDecimal.ZERO, BigDecimalOperations::add);
		BigDecimal cumulusTotalUsageUsed = BigDecimal.ZERO;
		BigDecimal cumulusTotalCostUsed = BigDecimal.ZERO;

		for (MetricInformation serviceAccountResponseBytesInformation : serviceAccountMetricInformationList) {
			if (serviceAccountInformationMap.containsKey(serviceAccountResponseBytesInformation.metricIdentifier())) {
				final ServiceAccountClusterInformation serviceAccountClusterInformation = serviceAccountInformationMap.get(serviceAccountResponseBytesInformation.metricIdentifier());
				final BigDecimal userPercentageRequestBytesUsed = getPercentage(serviceAccountResponseBytesInformation.value(), totalBytes);
				final BigDecimal teamCost = getValueFromPercentage(clusterCostData.clusterTotalCost(), userPercentageRequestBytesUsed);
				final BigDecimal teamUsageUsed = getValueFromPercentage(clusterCostData.clusterTotalUsage(), userPercentageRequestBytesUsed);
				cumulusTotalCostUsed = add(cumulusTotalCostUsed, teamCost);
				cumulusTotalUsageUsed = add(cumulusTotalUsageUsed, teamUsageUsed);

				teamCostDataSet.add(new TeamCostData(clusterCostData.clusterID(), serviceAccountClusterInformation.organization(), serviceAccountClusterInformation.application(), teamCost, teamUsageUsed,
						clusterCostData.startPeriod(), clusterCostData.endPeriod(), clusterCostData.costType()));

			}
		}

		teamCostDataSet.add(alignFinalValuesObtainedWithInitialValues(clusterCostData, cumulusTotalCostUsed, cumulusTotalUsageUsed, BigDecimal.ZERO, BigDecimal.ZERO));

		return teamCostDataSet;
	}

	private List<MetricInformation> getListServiceAccountMetricInformationByNetworkType(final ClusterCostData clusterCostData) {
		if (clusterCostData.costType().equals(KAFKA_NETWORK_WRITE)) {
			return getMetricValueFromClusterAndStartPeriod(requestByUsersAccountsGroupByStartTime,
					clusterCostData.clusterID(),
					clusterCostData.startPeriod(), Collections.EMPTY_LIST);
		}

		if (clusterCostData.costType().equals(KAFKA_NETWORK_READ)) {
			return getMetricValueFromClusterAndStartPeriod(responseByUsersAccountsGroupByStartTime,
					clusterCostData.clusterID(),
					clusterCostData.startPeriod(), Collections.EMPTY_LIST);
		}

		return Collections.EMPTY_LIST;
	}

	private void fillMetricsNeededToCalculateTheCosts(final Set<ClusterCostData> clusterCostDataSet) throws KafkaShowBackDemoException {
		final Map<CostType, Map<String, List<ClusterCostData>>> mapGroupByCostAndCluster = getMapGroupByCostAndCluster(clusterCostDataSet);

		final Instant startPeriod = clusterCostDataSet.stream().map(ClusterCostData::startPeriod).min(Instant::compareTo).get();
		final Instant endPeriod = clusterCostDataSet.stream().map(ClusterCostData::endPeriod).max(Instant::compareTo).get();

		for (Map.Entry<CostType, Map<String, List<ClusterCostData>>> groupByCostAndClusterData : mapGroupByCostAndCluster.entrySet()) {
			final Map<String, List<ClusterCostData>> costDataByCluster = groupByCostAndClusterData.getValue();
			for (Map.Entry<String, List<ClusterCostData>> entryClusterCostData : costDataByCluster.entrySet()) {
				final String clusterId = entryClusterCostData.getKey();

				switch (groupByCostAndClusterData.getKey()) {
					case KAFKA_NUM_CKUS -> {
						fillRequestBytesMapByUserAccount(clusterId, startPeriod, endPeriod);
						fillClusterIngressAndEgress(clusterId, startPeriod, endPeriod);
					}

					case KAFKA_NETWORK_READ -> fillResponseBytesMapByUserAccount(clusterId, startPeriod, endPeriod);
					case KAFKA_NETWORK_WRITE -> fillRequestBytesMapByUserAccount(clusterId, startPeriod, endPeriod);
				}

			}
		}
	}

	private void fillClusterIngressAndEgress(final String clusterId, final Instant startPeriod, final Instant endPeriod) throws KafkaShowBackDemoException {
		final ImmutableMap<Instant, BigDecimal> totalIngress = confluentCloudMetricService.getReceiveBytesByClusterGroupByHour(clusterId, startPeriod, endPeriod);
		final ImmutableMap<Instant, BigDecimal> totalEgress = confluentCloudMetricService.getSendBytesByClusterGroupByHour(clusterId, startPeriod, endPeriod);

		if (!totalIngress.isEmpty() && !totalEgress.isEmpty()) {
			egressGroupedByHourAndCluster.put(clusterId, totalEgress);
			ingressGroupedByHourAndCluster.put(clusterId, totalIngress);
		}
	}

	private void fillRequestBytesMapByUserAccount(final String clusterId, final Instant startPeriod, final Instant endPeriod) throws KafkaShowBackDemoException {
		final ImmutableMap<Instant, List<MetricInformation>> responseByUsersAccounts =
				confluentCloudMetricService.getServicesAccountRequestBytesByClusterIdGroupedByHour(clusterId, startPeriod, endPeriod);

		if (!responseByUsersAccounts.isEmpty()) {
			requestByUsersAccountsGroupByStartTime.put(clusterId, responseByUsersAccounts);
		}
	}

	private void fillResponseBytesMapByUserAccount(final String clusterId, final Instant startPeriod, final Instant endPeriod) throws KafkaShowBackDemoException {
		final ImmutableMap<Instant, List<MetricInformation>> responseByUsersAccounts =
				confluentCloudMetricService.getServicesAccountResponseBytesByClusterIdGroupedByHour(clusterId, startPeriod, endPeriod);

		if (!responseByUsersAccounts.isEmpty()) {
			responseByUsersAccountsGroupByStartTime.put(clusterId, responseByUsersAccounts);
		}
	}

	private Map<CostType, Map<String, List<ClusterCostData>>> getMapGroupByCostAndCluster(final Set<ClusterCostData> clusterCostDataSet) throws KafkaShowBackDemoException {
		final Map<CostType, Map<String, List<ClusterCostData>>> mapGroupByCostAndCluster = new HashMap<>();
		for (CostType costType : CostType.values()) {
			if (clusterCostDataSet.stream().anyMatch(costData -> costData.costType().equals(costType))) {
				try {
					mapGroupByCostAndCluster.put(costType,
							clusterCostDataSet.stream()
									.filter(clusterCostData -> clusterCostData.costType().equals(costType))
									.collect(Collectors.groupingBy(ClusterCostData::clusterID)));
				} catch (RuntimeException runtimeException) { //TODO
					throw new KafkaShowBackDemoException("Error grouping by cluster & cost type.", runtimeException);
				}
			}
		}
		return mapGroupByCostAndCluster;
	}

	/**
	 * After to sum all values obtained we have to calculate the differences between the sum of all ours values assigned to the different teams
	 * with the initial totalCost and totalUsage that we receive, the difference should be added to the DEFAULT TEAM
	 */
	private static TeamCostData alignFinalValuesObtainedWithInitialValues(final ClusterCostData clusterCostData, final BigDecimal sumCostByTeams,
	                                                                      final BigDecimal sumUsageByTeams, BigDecimal totalCostRest, BigDecimal totalUsageRest) {

		//final cost < clusterTotalCost
		final BigDecimal sumCostTeamsAndRest = add(sumCostByTeams, totalCostRest);
		if (sumCostTeamsAndRest.compareTo(clusterCostData.clusterTotalCost()) == IS_LESS_BIG_DECIMAL) {
			totalCostRest = add(totalCostRest, subtract(clusterCostData.clusterTotalCost(), sumCostTeamsAndRest));
		} else if (sumCostTeamsAndRest.compareTo(clusterCostData.clusterTotalCost()) == IS_GREATER_BIG_DECIMAL) {
			log.warn("The calculation of total cost used: {} is bigger than total amount cost received {}", sumCostTeamsAndRest, clusterCostData.clusterTotalCost());
		}

		final BigDecimal sumUsageTeamsAndRest = add(sumUsageByTeams, totalUsageRest);

		if (sumUsageTeamsAndRest.compareTo(clusterCostData.clusterTotalUsage()) == IS_LESS_BIG_DECIMAL) {
			totalUsageRest = add(totalUsageRest, subtract(clusterCostData.clusterTotalUsage(), sumUsageTeamsAndRest));
		} else if (sumUsageTeamsAndRest.compareTo(clusterCostData.clusterTotalUsage()) == IS_GREATER_BIG_DECIMAL) {
			log.warn("The calculation of total usage used: {} is bigger than total amount usage received {}", sumUsageTeamsAndRest, clusterCostData.clusterTotalUsage());
		}

		return new TeamCostData(clusterCostData.clusterID(), DEFAULT_ORGANIZATION, DEFAULT_APPLICATION, totalCostRest, totalUsageRest,
				clusterCostData.startPeriod(), clusterCostData.endPeriod(), clusterCostData.costType());
	}

	/**
	 * This method is used to calculate the usage percentage until the Confluent metric (usage_cluster) will fix by Confluent team
	 * The cluster usage percentage is grabbing with the following form: max(totalIngres/(65*total CKU Cluster),totalEgress/(130*total CKU Cluster))
	 * the values 65/130 ingres/egress are the confluent cku limits
	 */
	private BigDecimal applyFormulaToGetUsageClusterPercentage(final ClusterCostData clusterCostData) {
		final BigDecimal sumIngressBytes = getMetricValueFromClusterAndStartPeriod(ingressGroupedByHourAndCluster, clusterCostData.clusterID(),
				clusterCostData.startPeriod(), BigDecimal.ZERO);
		final BigDecimal sumEgressBytes = getMetricValueFromClusterAndStartPeriod(egressGroupedByHourAndCluster, clusterCostData.clusterID(), clusterCostData.startPeriod(), BigDecimal.ZERO);

		final BigDecimal sumIngressSeconds = divide(sumIngressBytes, DEFAULT_SECONDS_HOUR);
		final BigDecimal sumEgressSeconds = divide(sumEgressBytes, DEFAULT_SECONDS_HOUR);

		final BigDecimal totalNumOfCKUs = clusterCostData.clusterTotalUsage();

		final BigDecimal ingressFormula = divide(sumIngressSeconds, multiply(totalNumOfCKUs, DEFAULT_CKU_LIMIT_INGRESS_IN_BYTES));
		final BigDecimal egressFormula = divide(sumEgressSeconds, multiply(totalNumOfCKUs, DEFAULT_CKU_LIMIT_EGRESS_IN_BYTES));

		return ingressFormula.max(egressFormula);
	}

	private <T> T getMetricValueFromClusterAndStartPeriod(final Map<String, ImmutableMap<Instant, T>> mapWithMetricInformation,
	                                                      final String clusterId,
	                                                      final Instant startInstant,
	                                                      final T defaultValue) {

		if (mapWithMetricInformation.containsKey(clusterId)) {
			final ImmutableMap<Instant, T> serviceAccountMetricInformationMap = mapWithMetricInformation.get(clusterId);
			if (serviceAccountMetricInformationMap.containsKey(startInstant)) {
				return serviceAccountMetricInformationMap.get(startInstant);
			}
		}
		return defaultValue;
	}

	private static TeamCostData addTotalCostToDefaultOrganization(final ClusterCostData clusterCostData) {
		log.warn("No action/data found for {} cost type and cluster {}. The total cost will include in the default organization ({}).",
				clusterCostData.costType(), clusterCostData.clusterID(), DEFAULT_ORGANIZATION);

		return new TeamCostData(clusterCostData.clusterID(), DEFAULT_ORGANIZATION, DEFAULT_APPLICATION, clusterCostData.clusterTotalCost(), clusterCostData.clusterTotalUsage(), clusterCostData.startPeriod(),
				clusterCostData.endPeriod(), clusterCostData.costType());
	}
}
