package kafka.showbacks.demo.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NR1Configuration(@JsonProperty("licenseApiKey") String licenseApiKey,
                               @JsonProperty("eventType") String eventType,
                               @JsonProperty("accountId") String accountId,
                               @JsonProperty("eventAPIUrl") String eventApiUrl) {
}
