package kafka.showbacks.demo;

import com.google.common.util.concurrent.AbstractScheduledService;
import kafka.showbacks.demo.common.exception.KafkaShowBackDemoException;
import kafka.showbacks.demo.common.model.ClusterCostData;
import kafka.showbacks.demo.common.model.TeamCostData;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

//todo protecte
//todo singlenton
//TODO CONFLUENT
class KafkaShowBacksDemoService extends AbstractScheduledService {

	private static final Logger log = LoggerFactory.getLogger(KafkaShowBacksDemoService.class);

	private final KafkaShowBacksDemo kafkaShowBacksDemo;

	private final long initialDelayInSeconds;

	private final long periodInSeconds;

	private final int daysToSubtract;

	private final Set<String> clustersId;

	//todo protect
	@Inject
	KafkaShowBacksDemoService(final KafkaShowBacksDemo kafkaShowBacksDemo,
	                          final Set<String> clustersIdSet,
	                          final long initialDelaySeconds,
	                          final long periodInSeconds,
	                          final boolean demoMode) {
		this.kafkaShowBacksDemo = kafkaShowBacksDemo;
		this.clustersId = clustersIdSet;
		this.initialDelayInSeconds = initialDelaySeconds;
		this.periodInSeconds = periodInSeconds;
		this.daysToSubtract = demoMode ? 1 : 7; //todo constants
	}

	@Override
	protected void runOneIteration() {
		log.info("Starting KafkaShowBacksDemoService.");
		//todo check if we should remove one hour
		final Set<ClusterCostData> costDataSet = getBillingDataForDate();

		if (!costDataSet.isEmpty()) {
			for (String clusterId : clustersId) {
				//todo if is empty recover all
				final Set<ClusterCostData> costDataByClusterId = costDataSet.stream()
						.filter(clusterCostData -> StringUtils.equalsIgnoreCase(clusterCostData.clusterID(), clusterId))
						.collect(Collectors.toSet());
				if (!costDataByClusterId.isEmpty()) {
					log.info("Calculating  costs data by team. ClusterId:{}", clusterId);
					final Set<TeamCostData> teamCostDataSet = getCostDividedByTeams(costDataByClusterId);
					teamCostDataSet.stream().forEach(x -> System.out.println(x));
					//todo store data
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


	private Set<ClusterCostData> getBillingDataForDate() {
		//local date vs da
		final LocalDate startDate = LocalDate.now().minusDays(this.daysToSubtract);
		final LocalDate endDate = LocalDate.now().minusDays(1);

		log.info("Searching data billing from {} until {}.", startDate, endDate);

		try {
			//todo exception and type
			return this.kafkaShowBacksDemo.getCostDataByDate(startDate, endDate);

		} catch (KafkaShowBackDemoException kafkaShowBackDemoException) {
			log.error("Error trying to get the billing by date.", kafkaShowBackDemoException);
		}
		return Collections.EMPTY_SET; //todo stop proccess
	}

	private Set<TeamCostData> getCostDividedByTeams(final Set<ClusterCostData> clusterCostDataSet) {
		try {
			return this.kafkaShowBacksDemo.getCostDividedByTeams(clusterCostDataSet);
		} catch (KafkaShowBackDemoException kafkaShowBackDemoException) {
			log.error("Error trying to calculate cost divided by teams.", kafkaShowBackDemoException);
		}
		return Collections.EMPTY_SET;
	}

}
