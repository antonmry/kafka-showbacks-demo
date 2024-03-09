package kafka.showbacks.demo.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.core.Configuration;

public class KafkaShowBacksDemoConfiguration extends Configuration {

	@JsonProperty(value = "requestTimeOutInSeconds", defaultValue = "30")
	private int requestTimeOutInSeconds;

	@JsonProperty("cacheExpiredInHours")
	private int cacheExpiredInHours;

	@JsonProperty("confluent")
	private ConfluentConfiguration confluentConfiguration;

	@JsonProperty(value = "numRequestRetries", defaultValue = "3")
	private int numRequestRetries;

	@JsonProperty(value = "timeToRetryRequestCallMs", defaultValue = "2000")
	private int timeToRetryRequestCallMs;

	@JsonProperty(value = "initialDelaySeconds", defaultValue = "10")
	private long initialDelaySeconds;

	@JsonProperty(value = "periodInSeconds", defaultValue = "86400")
	private long periodInSeconds;

	@JsonProperty("newrelic")
	private NR1Configuration nr1Configuration;

	@JsonProperty("daysToExecute")
	private int daysToExecute;

	public int getRequestTimeOutInSeconds() {
		return requestTimeOutInSeconds;
	}

	public int getCacheExpiredInHours() {
		return cacheExpiredInHours;
	}

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

	public NR1Configuration getNr1Configuration() {
		return nr1Configuration;
	}

	public int getDaysToExecute() {
		return daysToExecute;
	}
}
