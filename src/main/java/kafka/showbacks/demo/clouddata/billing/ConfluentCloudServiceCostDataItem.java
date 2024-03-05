package kafka.showbacks.demo.clouddata.billing;

import com.fasterxml.jackson.annotation.JsonProperty;
import kafka.showbacks.demo.clouddata.ConfluentCloudDataItem;

import java.math.BigDecimal;
import java.time.LocalDate;

record ConfluentCloudServiceCostDataItem(@JsonProperty("line_type") String costType,
                                         BigDecimal amount,
                                         @JsonProperty("quantity") BigDecimal clusterTotalUsage,
                                         @JsonProperty("resource") Resource resource,
                                         @JsonProperty("start_date") LocalDate startPeriod,
                                         @JsonProperty("end_date") LocalDate endPeriod) implements ConfluentCloudDataItem {

	record Resource(@JsonProperty("id") String clusterId) {
	}

}
