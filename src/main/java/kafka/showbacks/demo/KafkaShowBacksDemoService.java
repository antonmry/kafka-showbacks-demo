package kafka.showbacks.demo;

import com.google.common.util.concurrent.AbstractScheduledService;
import kafka.showbacks.demo.common.exception.KafkaShowBackDemoException;
import kafka.showbacks.demo.common.model.ClusterCostData;
import kafka.showbacks.demo.common.model.TeamCostData;
import kafka.showbacks.demo.outputdata.OutputDataService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

class KafkaShowBacksDemoService extends AbstractScheduledService {

	private static final Logger log = LogManager.getLogger();

	private final KafkaShowBacksDemo kafkaShowBacksDemo;

	private final OutputDataService nr1OutputDataService;

	private final long initialDelayInSeconds;

	private final long periodInSeconds;

	private final int daysToSubtract;

	private final Set<String> clustersId;

	@Inject
	KafkaShowBacksDemoService(final KafkaShowBacksDemo kafkaShowBacksDemo,
	                          final OutputDataService outputDataService,
	                          final Set<String> clustersIdSet,
	                          final long initialDelaySeconds,
	                          final long periodInSeconds,
	                          final int daysToExecute) {
		this.kafkaShowBacksDemo = kafkaShowBacksDemo;
		this.nr1OutputDataService = outputDataService;
		this.clustersId = clustersIdSet;
		this.initialDelayInSeconds = initialDelaySeconds;
		this.periodInSeconds = periodInSeconds;
		this.daysToSubtract = daysToExecute;
	}

	@Override
	protected void runOneIteration() throws KafkaShowBackDemoException {
		log.info("Starting KafkaShowBacksDemoService.");

		final Set<ClusterCostData> costDataSet = getBillingDataForDate();

		if (!costDataSet.isEmpty()) {
			for (String clusterId : clustersId) {

				final Set<ClusterCostData> costDataByClusterId = costDataSet.stream()
						.filter(clusterCostData -> StringUtils.equalsIgnoreCase(clusterCostData.clusterID(), clusterId))
						.collect(Collectors.toSet());
				if (!costDataByClusterId.isEmpty()) {

					final Set<TeamCostData> teamCostDataSet = getCostDividedByTeams(costDataByClusterId);

					storeOutputData(teamCostDataSet);

				} else {
					log.warn("No cost founds for clusterId:{}", clusterId);
				}
			}

		} else {
			log.warn("We don't have billing information, so it's not possible to get the cost by teams.");
		}

		log.info("End KafkaShowBacksDemoService.");
	}

	@Override
	protected Scheduler scheduler() {
		return Scheduler.newFixedRateSchedule(initialDelayInSeconds, periodInSeconds, TimeUnit.SECONDS);
	}


	private Set<ClusterCostData> getBillingDataForDate() throws KafkaShowBackDemoException {

		final LocalDate startDate = LocalDate.now().minusDays(this.daysToSubtract);
		final LocalDate endDate = LocalDate.now();

		log.info("Searching data billing from {} until {}.", startDate, endDate);

		try {
			return this.kafkaShowBacksDemo.getCostDataByDate(startDate, endDate);

		} catch (KafkaShowBackDemoException kafkaShowBackDemoException) {
			log.error("Error trying to get the billing by date from startDate: {} until: {} ", startDate, endDate, kafkaShowBackDemoException);
			throw kafkaShowBackDemoException;
		}
	}

	private Set<TeamCostData> getCostDividedByTeams(final Set<ClusterCostData> clusterCostDataSet) {
		log.info("Calculating  costs data by team. ClusterCostData size: {}", clusterCostDataSet.size());
		try {
			return this.kafkaShowBacksDemo.getCostDividedByTeams(clusterCostDataSet);
		} catch (KafkaShowBackDemoException kafkaShowBackDemoException) {
			log.error("Error trying to calculate cost divided by teams.", kafkaShowBackDemoException);
		}
		return Collections.EMPTY_SET;
	}

	private void storeOutputData(final Set<TeamCostData> teamCostDataSet) {
		log.info("Storing output data. Number of records: {}", teamCostDataSet.size());
		try {
			this.nr1OutputDataService.sendOutputData(teamCostDataSet, "Confluent");
		} catch (KafkaShowBackDemoException e) {
			log.error("Error storing output data.", e);
		}
	}
}
