package kafka.showbacks.demo;

import com.google.common.util.concurrent.AbstractScheduledService;
import kafka.showbacks.demo.common.model.ClusterCostData;
import kafka.showbacks.demo.common.model.TeamCostData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.Set;
import java.util.concurrent.TimeUnit;

//todo protecte
//todo singlenton
//TODO CONFLUENT
class KafkaShowBacksDemoService extends AbstractScheduledService {

	private static final Logger log = LoggerFactory.getLogger(KafkaShowBacksDemoService.class);

	private final KafkaShowBacksDemo kafkaShowBacksDemo;

	private final long initialDelayInSeconds;

	private final long periodInSeconds;

	private final int daysToSubtract;

	//todo protect
	@Inject
	KafkaShowBacksDemoService(final KafkaShowBacksDemo kafkaShowBacksDemo,
	                          final long initialDelaySeconds,
	                          final long periodInSeconds,
	                          final boolean demoMode) {
		this.kafkaShowBacksDemo = kafkaShowBacksDemo;
		this.initialDelayInSeconds = initialDelaySeconds;
		this.periodInSeconds = periodInSeconds;
		this.daysToSubtract = demoMode ? 1 : 7; //todo constants
	}

	@Override
	protected void runOneIteration() throws Exception {
		log.info("Starting KafkaShowBacksDemoService.");

		//local date vs da
		final LocalDate startDate = LocalDate.now().minusDays(this.daysToSubtract);
		final LocalDate endDate = LocalDate.now().minusDays(1);

		log.info("Searching data billing from {} until {}.", startDate, endDate);

		//todo exception and type
		final Set<ClusterCostData> clusterCostDataSet = this.kafkaShowBacksDemo.getCostDataByDate(startDate, endDate);

		if (!clusterCostDataSet.isEmpty()) {
			log.info("Calculating costs data by team. Number of records {}", clusterCostDataSet.size());

			final Set<TeamCostData> teamCostDataSet = this.kafkaShowBacksDemo.getCostDividedByTeams(clusterCostDataSet);
			//todo store data

		} else {
			log.warn("We don't have billing information, so it's not possible to get the cost by teams.");
		}

		log.info("End KafkaShowBacksDemoService.");
	}

	@Override
	protected Scheduler scheduler() {
		return Scheduler.newFixedRateSchedule(initialDelayInSeconds, periodInSeconds, TimeUnit.SECONDS);
	}

}
