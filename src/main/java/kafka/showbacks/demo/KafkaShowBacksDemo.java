package kafka.showbacks.demo;

import kafka.showbacks.demo.common.exception.KafkaShowBackDemoException;
import kafka.showbacks.demo.common.model.ClusterCostData;
import kafka.showbacks.demo.common.model.TeamCostData;

import java.time.LocalDate;
import java.util.Set;

/**
 * This interface is used currently to get the information in
 * Confluent environments.
 * The idea is that we can use this interface in the case of include
 * another Kafka ecosystems
 */
public interface KafkaShowBacksDemo {
	/**
	 * Return the billing data of Confluent extracted from Confluent API.
	 * Currently, this information just can be obtained by complete days, and it's not possible
	 * filter by cluster.
	 */
	Set<ClusterCostData> getCostDataByDate(final LocalDate startDate, final LocalDate endDate) throws KafkaShowBackDemoException;

	/**
	 * Return the calculated cost by team taking during the time range sent
	 * in the clusterCostData item (startPeriod and endPeriod).
	 */
	Set<TeamCostData> getCostDividedByTeams(final Set<ClusterCostData> clusterCostData) throws KafkaShowBackDemoException;

}
