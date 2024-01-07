package kafka.showbacks.demo.clustermetrics;

import java.math.BigDecimal;
import java.time.Instant;

public record MetricInformation(String metricIdentifier, Instant startTime, BigDecimal value) {
}
