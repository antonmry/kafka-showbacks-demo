package kafka.showbacks.demo.clouddata.billing;

import kafka.showbacks.demo.common.exception.KafkaShowBackDemoException;
import kafka.showbacks.demo.common.model.ClusterCostData;

import java.time.LocalDate;
import java.util.Set;

/**
 * This interface can be uses to get the cost from different environments
 * (Currently only Confluent Cloud)
 */
public sealed interface CloudCostService permits ConfluentCloudCostService {

	Set<ClusterCostData> getCostDataByTimeRange(final LocalDate startDate, final LocalDate endDate) throws KafkaShowBackDemoException;
}
