package kafka.showbacks.demo.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.core.Configuration;

//todo different configurations class
//todo records
//todo Configuratoions class is not needed /neither json records...
public final class KafkaShowBacksDemoConfiguration extends Configuration {

	@JsonProperty("confluentApiKey")
	private String confluentApiKey;

	@JsonProperty("confluentApiSecret")
	private String confluentApiSecret;

	@JsonProperty(value = "requestTimeOutInSeconds", defaultValue = "60")
	private int requestTimeOutInSeconds;

	@JsonProperty("telemetryUrl")
	private String telemetryUrl;

	@JsonProperty("cloudBillingUrl")
	private String cloudBillingUrl;

	@JsonProperty("cloudServiceAccountUrl")
	private String cloudServiceAccountUrl;

	@JsonProperty("cacheExpiredInHours")
	private int cacheExpiredInHours;

	@JsonProperty("newRelicAccountId")
	private String newRelicAccountId;

	@JsonProperty("newRelicGraphUrl")
	private String newRelicGraphUrl;

	@JsonProperty("newRelicApiKey")
	private String newRelicApiKey;

	@JsonProperty(value = "numRequestRetries", defaultValue = "3")
	private int numRequestRetries;

	@JsonProperty(value = "timeToRetryRequestCallMs", defaultValue = "2000")
	private int timeToRetryRequestCallMs;

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

	public String getCloudBillingUrl() {
		return cloudBillingUrl;
	}

	public String getCloudServiceAccountUrl() {
		return cloudServiceAccountUrl;
	}

	public String getNewRelicAccountId() {
		return newRelicAccountId;
	}

	public String getNewRelicGraphUrl() {
		return newRelicGraphUrl;
	}

	public String getNewRelicApiKey() {
		return newRelicApiKey;
	}

	public int getNumRequestRetries() {
		return numRequestRetries;
	}

	public int getTimeToRetryRequestCallMs() {
		return timeToRetryRequestCallMs;
	}
}
