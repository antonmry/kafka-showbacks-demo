package kafka.showbacks.demo.outputdata;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import kafka.showbacks.demo.CostType;

import java.math.BigDecimal;
import java.time.Instant;

@JsonSerialize //todo and check all
record KafkaShowBacks(String eventType, CostType costType, String kafkaProvider, String organization,
                      String application,
                      BigDecimal teamCost, BigDecimal teamUsage,
                      Instant startPeriod, Instant endPeriod, long timestamp, String clusterId) {
}
