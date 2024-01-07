package kafka.showbacks.demo.common.model;

import kafka.showbacks.demo.CostType;

import java.math.BigDecimal;
import java.time.Instant;

//todo check correct place
public record ClusterCostData(CostType costType, BigDecimal clusterTotalCost, BigDecimal clusterTotalUsage,
                              String clusterID, Instant startPeriod,
                              Instant endPeriod) {
}
