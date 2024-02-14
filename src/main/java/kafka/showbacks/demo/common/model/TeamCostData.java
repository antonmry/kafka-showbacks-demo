package kafka.showbacks.demo.common.model;

import kafka.showbacks.demo.CostType;

import java.math.BigDecimal;
import java.time.Instant;

//todo check correct place
public record TeamCostData(String clusterId, String organization, String application, BigDecimal teamCost,
                           BigDecimal teamUsage,
                           Instant startPeriod,
                           Instant endPeriod, CostType costType) {
}
