package kafka.showbacks.demo.clustermetrics;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

record MetricDataItem(Double value, //the rest metric API can return NaN value
                      @JsonProperty("metric.principal_id") @JsonAlias("metric.topic") String metric,
                      Instant timestamp) { //always is returning a UTC time

}
