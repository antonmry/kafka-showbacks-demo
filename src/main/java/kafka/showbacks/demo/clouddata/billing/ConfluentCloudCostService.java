package kafka.showbacks.demo.clouddata.billing;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Joiner;
import kafka.showbacks.demo.CostType;
import kafka.showbacks.demo.clouddata.ConfluentCloudServiceClient;
import kafka.showbacks.demo.common.exception.KafkaShowBackDemoException;
import kafka.showbacks.demo.common.model.ClusterCostData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class ConfluentCloudCostService implements CloudCostService {
	private static final TypeReference<Set<ConfluentCloudServiceCostDataItem>> TYPE_REFERENCE = new TypeReference<>() {
	};
	private static final Logger log = LogManager.getLogger();

	private static final String QUERY_PARAMETER_MAX_PAGE_SIZE = "?start_date=%s&end_date=%s&page_size=10000";

	private static final Map<String, String> VALID_COST_TYPE = Arrays.stream(CostType.values())
			.collect(Collectors.toMap(CostType::toString, CostType::getName));

	private final ConfluentCloudServiceClient confluentCloudCostServiceClient;

	private final String billingCloudUrl;

	@Inject
	public ConfluentCloudCostService(final ConfluentCloudServiceClient confluentCloudCostServiceClient,
	                                 final String billingCloudUrl) {
		this.confluentCloudCostServiceClient = confluentCloudCostServiceClient;
		this.billingCloudUrl = billingCloudUrl;
	}

	@Override
	public Set<ClusterCostData> getCostDataByTimeRange(final LocalDate startDate, final LocalDate endDate) throws KafkaShowBackDemoException {
		log.info("Getting cost by time range and cluster from {} until {}", startDate, endDate);

		final String urlWithRangeTime = Joiner.on("").join(billingCloudUrl, String.format(QUERY_PARAMETER_MAX_PAGE_SIZE, startDate.toString(), endDate.toString()));

		final Set<ConfluentCloudServiceCostDataItem> confluentCloudServiceCostDataItemSet = this.confluentCloudCostServiceClient.getCollectionFromConfluentCloudServiceClient(urlWithRangeTime, TYPE_REFERENCE);

		return mapDataItemCostToClusterCostData(confluentCloudServiceCostDataItemSet);
	}

	private Set<ClusterCostData> mapDataItemCostToClusterCostData(final Set<ConfluentCloudServiceCostDataItem> confluentCloudServiceCostDataItemSet) throws KafkaShowBackDemoException {
		log.info("Mapping cluster cost data results {}", confluentCloudServiceCostDataItemSet.size());
		try {
			return confluentCloudServiceCostDataItemSet.stream()
					.filter(item -> VALID_COST_TYPE.containsKey(item.costType()))
					.map(item -> new ClusterCostData(CostType.valueOf(item.costType()), item.amount(),
							item.clusterTotalUsage(), item.resource().clusterId(),
							item.startPeriod().atStartOfDay().toInstant(ZoneOffset.UTC),
							item.endPeriod().atStartOfDay().toInstant(ZoneOffset.UTC)))
					.collect(Collectors.toSet());
		} catch (NullPointerException | IllegalArgumentException exception) {
			throw new KafkaShowBackDemoException("Error mapping cluster cost data result", exception);
		}
	}
}
