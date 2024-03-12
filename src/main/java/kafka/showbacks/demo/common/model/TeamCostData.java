package kafka.showbacks.demo.common.model;

import java.math.BigDecimal;
import java.time.Instant;

public record TeamCostData(String clusterId, String organization, String application, BigDecimal teamCost,
                           BigDecimal teamUsage,
                           Instant startPeriod,
                           Instant endPeriod, CostType costType) {
}
