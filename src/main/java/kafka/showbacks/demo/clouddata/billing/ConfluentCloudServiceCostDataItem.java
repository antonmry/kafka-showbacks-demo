package kafka.showbacks.demo.clouddata.billing;

import com.fasterxml.jackson.annotation.JsonProperty;
import kafka.showbacks.demo.CostType;

import java.math.BigDecimal;
import java.time.Instant;

record ConfluentCloudServiceCostDataItem(@JsonProperty("line_type") CostType costType,
                                         BigDecimal amount,
                                         @JsonProperty("quantity") BigDecimal clusterTotalUsage, //todo usages?
                                         @JsonProperty("id") String clusterID,
                                         @JsonProperty("start_date") Instant startPeriod,
                                         @JsonProperty("end_date") Instant endPeriod) {
}
