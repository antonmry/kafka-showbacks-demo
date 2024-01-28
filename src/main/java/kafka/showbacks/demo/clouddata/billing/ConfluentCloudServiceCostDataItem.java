package kafka.showbacks.demo.clouddata.billing;

import com.fasterxml.jackson.annotation.JsonProperty;
import kafka.showbacks.demo.clouddata.ConfluentCloudDataItem;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ConfluentCloudServiceCostDataItem(@JsonProperty("line_type") String costType,
                                                BigDecimal amount,
                                                @JsonProperty("quantity") BigDecimal clusterTotalUsage, //todo usages?
                                                @JsonProperty("id") String clusterID,
                                                @JsonProperty("start_date") LocalDate startPeriod, //todo check
                                                @JsonProperty("end_date") LocalDate endPeriod) implements ConfluentCloudDataItem {
}
