package kafka.showbacks.demo.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.core.Configuration;

//todo different configurations class
public final class KafkaShowBacksDemoConfiguration extends Configuration {

	@JsonProperty("confluentApiKey")
	private String confluentApiKey;

	@JsonProperty("confluentApiSecret")
	private String confluentApiSecret;

	@JsonProperty(value = "requestTimeOutInSeconds", defaultValue = "60")
	private int requestTimeOutInSeconds;

	@JsonProperty("telemetryUrl")
	private String telemetryUrl;

	@JsonProperty("cloudUrl")
	private String cloudUrl;

	@JsonProperty("cacheExpiredInHours")
	private int cacheExpiredInHours;

	public String getConfluentApiKey() {
		return confluentApiKey;
	}

	public String getConfluentApiSecret() {
		return confluentApiSecret;
	}

	public int getRequestTimeOutInSeconds() {
		return requestTimeOutInSeconds;
	}

	public String getTelemetryUrl() {
		return telemetryUrl;
	}

	public int getCacheExpiredInHours() {
		return cacheExpiredInHours;
	}

	public String getCloudUrl() {
		return cloudUrl;
	}
}
