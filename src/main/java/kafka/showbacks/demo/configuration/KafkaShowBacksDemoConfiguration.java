package kafka.showbacks.demo.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.core.Configuration;

//todo records
//todo Configuratoions class is not needed /neither json records...
//todo log configuration
public class KafkaShowBacksDemoConfiguration extends Configuration {

	@JsonProperty(value = "requestTimeOutInSeconds", defaultValue = "30")
	private int requestTimeOutInSeconds;

	@JsonProperty("telemetryUrl")
	private String telemetryUrl;

	@JsonProperty("cacheExpiredInHours")
	private int cacheExpiredInHours;

	@JsonProperty("confluent")
	private ConfluentConfiguration confluentConfiguration;

	//@JsonProperty("newRelicAccountId")
	//private String newRelicAccountId;

	//@JsonProperty("newRelicGraphUrl")
	//private String newRelicGraphUrl;

	//@JsonProperty("newRelicApiKey")
	//private String newRelicApiKey;

	@JsonProperty(value = "numRequestRetries", defaultValue = "3")
	private int numRequestRetries;

	@JsonProperty(value = "timeToRetryRequestCallMs", defaultValue = "2000")
	private int timeToRetryRequestCallMs;

	@JsonProperty(value = "initialDelaySeconds", defaultValue = "10")
	private long initialDelaySeconds;

	@JsonProperty(value = "periodInSeconds", defaultValue = "86400")
	private long periodInSeconds;

	public int getRequestTimeOutInSeconds() {
		return requestTimeOutInSeconds;
	}

	public String getTelemetryUrl() {
		return telemetryUrl;
	}

	public int getCacheExpiredInHours() {
		return cacheExpiredInHours;
	}


	/*public String getNewRelicAccountId() {
		return newRelicAccountId;
	}

	public String getNewRelicGraphUrl() {
		return newRelicGraphUrl;
	}

	public String getNewRelicApiKey() {
		return newRelicApiKey;
	}*/

	public int getNumRequestRetries() {
		return numRequestRetries;
	}

	public int getTimeToRetryRequestCallMs() {
		return timeToRetryRequestCallMs;
	}

	public long getInitialDelaySeconds() {
		return initialDelaySeconds;
	}

	public long getPeriodInSeconds() {
		return periodInSeconds;
	}

	public ConfluentConfiguration getConfluentConfiguration() {
		return confluentConfiguration;
	}
}
