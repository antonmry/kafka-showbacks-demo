package kafka.showbacks.demo.clouddata.billing;

import com.google.common.base.Joiner;
import kafka.showbacks.demo.clouddata.ConfluentCloudServiceClient;
import kafka.showbacks.demo.common.exception.KafkaShowBackDemoException;
import kafka.showbacks.demo.common.model.ClusterCostData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

//todo interface
//todo log level
//todo exceptions register
//todo warnings
//todo cache... take into accounts dates
public final class ConfluentCloudCostService {

	private static final Logger log = LoggerFactory.getLogger(ConfluentCloudCostService.class);

	private static final String QUERY_PARAMETER_MAX_PAGE_SIZE = "?start_date=&t&end_date=%t&page_size=10000";

	private final ConfluentCloudServiceClient confluentCloudCostServiceClient;

	private final String billingCloudUrl;

	@Inject
	public ConfluentCloudCostService(final ConfluentCloudServiceClient confluentCloudCostServiceClient,
	                                 final String billingCloudUrl) {
		this.confluentCloudCostServiceClient = confluentCloudCostServiceClient;
		this.billingCloudUrl = billingCloudUrl;
	}

	//todo cluster??
	public Set<ClusterCostData> getCostDataByTimeRange(final LocalDate startDate, final LocalDate endDate) throws KafkaShowBackDemoException {
		//todo how to send parameters
		log.info("Getting cost by time range and cluster");
		final String urlWithRangeTime = Joiner.on("").join(billingCloudUrl, String.format(QUERY_PARAMETER_MAX_PAGE_SIZE, startDate, endDate));

		final Set<ConfluentCloudServiceCostDataItem> confluentCloudServiceCostDataItemSet = new HashSet<>();
		this.confluentCloudCostServiceClient.fillCollectionFromConfluentCloudServiceClient(confluentCloudServiceCostDataItemSet, urlWithRangeTime);

		return mapDataItemCostToClusterCostData(confluentCloudServiceCostDataItemSet);
	}

	private Set<ClusterCostData> mapDataItemCostToClusterCostData(final Set<ConfluentCloudServiceCostDataItem> confluentCloudServiceCostDataItemSet) {

		log.info("Mapping cluster cost data results {}", confluentCloudServiceCostDataItemSet.size());
		return confluentCloudServiceCostDataItemSet.stream()
				.map(item -> new ClusterCostData(item.costType(), item.amount(),
						item.clusterTotalUsage(), item.clusterID(), item.startPeriod(), item.endPeriod())).collect(Collectors.toSet());
	}
}
