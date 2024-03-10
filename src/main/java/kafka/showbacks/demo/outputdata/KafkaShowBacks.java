package kafka.showbacks.demo.outputdata;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import kafka.showbacks.demo.common.model.CostType;

import java.math.BigDecimal;
import java.time.Instant;

@JsonSerialize
record KafkaShowBacks(String eventType, CostType costType, String kafkaProvider, String organization,
                      String application, BigDecimal teamCost, BigDecimal teamUsage,
                      Instant startPeriod, Instant endPeriod, long timestamp, String clusterId) {
}

