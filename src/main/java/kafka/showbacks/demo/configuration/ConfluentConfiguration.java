package kafka.showbacks.demo.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;
//provider not works
public record ConfluentConfiguration(@JsonProperty("apiKey") String confluentApiKey,
                                     @JsonProperty("cloudBillingUrl") String cloudBillingUrl,
                                     @JsonProperty("telemetryUrl") String telemetryUrl,
                                     @JsonProperty("cloudServiceAccountUrl") String cloudServiceAccountUrl,
                                     @JsonProperty("apiSecret") String confluentApiSecret,
                                     @JsonProperty("clustersIdList") Set<String> clustersIdList) {

}
