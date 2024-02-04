package kafka.showbacks.demo.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

public class ConfluentConfiguration extends KafkaShowBacksDemoConfiguration {
	@JsonProperty("apiKey")
	private String confluentApiKey;

	@JsonProperty("cloudBillingUrl")
	private String cloudBillingUrl;

	@JsonProperty("telemetryUrl")
	private String telemetryUrl;

	@JsonProperty("cloudServiceAccountUrl")
	private String cloudServiceAccountUrl;

	@JsonProperty("apiSecret")
	private String confluentApiSecret;

	@JsonProperty("clustersIdList")
	private Set<String> clustersIdList;

	public String getConfluentApiKey() {
		return confluentApiKey;
	}

	public String getCloudBillingUrl() {
		return cloudBillingUrl;
	}

	public String getCloudServiceAccountUrl() {
		return cloudServiceAccountUrl;
	}

	public String getConfluentApiSecret() {
		return confluentApiSecret;
	}

	public String getTelemetryUrl() {
		return telemetryUrl;
	}

	public Set<String> getClustersIdList() {
		return clustersIdList;
	}
}
