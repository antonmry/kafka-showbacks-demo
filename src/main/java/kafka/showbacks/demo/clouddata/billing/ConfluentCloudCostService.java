package kafka.showbacks.demo.clouddata.billing;

import kafka.showbacks.demo.clouddata.ConfluentCloudServiceClient;
import kafka.showbacks.demo.common.exception.KafkaShowBackDemoException;
import kafka.showbacks.demo.common.model.ClusterCostData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

//todo interface
//todo log level
//todo exceptions register
public final class ConfluentCloudCostService {

	private static final Logger log = LoggerFactory.getLogger(ConfluentCloudCostService.class);

	private final ConfluentCloudServiceClient confluentCloudCostServiceClient;

	@Inject
	public ConfluentCloudCostService(final ConfluentCloudServiceClient confluentCloudCostServiceClient) {
		this.confluentCloudCostServiceClient = confluentCloudCostServiceClient;
	}

	Set<ClusterCostData> getCostDataByTimeRangeAndCluster() throws KafkaShowBackDemoException {

		log.info("Getting cost by time range and cluster");

		final Set<ConfluentCloudServiceCostDataItem> confluentCloudServiceCostDataItemSet = new HashSet<>();
		this.confluentCloudCostServiceClient.fillCollectionFromConfluentCloudServiceClient(confluentCloudServiceCostDataItemSet);

		return mapDataItemCostToClusterCostData(confluentCloudServiceCostDataItemSet);
	}

	private Set<ClusterCostData> mapDataItemCostToClusterCostData(final Set<ConfluentCloudServiceCostDataItem> confluentCloudServiceCostDataItemSet) {

		log.info("Mapping cluster cost data results {}", confluentCloudServiceCostDataItemSet.size());
		return confluentCloudServiceCostDataItemSet.stream()
				.map(item -> new ClusterCostData(item.costType(), item.amount(),
						item.clusterTotalUsage(), item.clusterID(), item.startPeriod(), item.endPeriod())).collect(Collectors.toSet());
	}
}
