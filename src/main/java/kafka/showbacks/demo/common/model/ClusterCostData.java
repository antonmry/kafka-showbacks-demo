package kafka.showbacks.demo.common.model;

import java.math.BigDecimal;
import java.time.Instant;

public record ClusterCostData(CostType costType, BigDecimal clusterTotalCost, BigDecimal clusterTotalUsage,
                              String clusterID, Instant startPeriod,
                              Instant endPeriod) {
}
