package kafka.showbacks.demo;

import kafka.showbacks.demo.common.exception.KafkaShowBackDemoException;
import kafka.showbacks.demo.common.model.ClusterCostData;
import kafka.showbacks.demo.common.model.TeamCostData;

import java.util.Set;

/**
 * This interface is used currently to get the information in
 * Confluent environments.
 * The idea is that we can use this interface in the case of include
 * another environment as can be MSK
 */
//todo check comments
public interface TeamUsageCost {
	Set<TeamCostData> getCostDividedByTeams(final Set<ClusterCostData> clusterCostData) throws KafkaShowBackDemoException;
}
