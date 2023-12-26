package kafka.showbacks.demo;

import dagger.Provides;
import kafka.showbacks.demo.clustermetrics.ClusterMetricClient;
import kafka.showbacks.demo.clustermetrics.ConfluentCloudMetricClient;
import kafka.showbacks.demo.common.rest.RetryOnError;
import kafka.showbacks.demo.configuration.KafkaShowBacksDemoConfiguration;

import javax.inject.Named;
import javax.inject.Singleton;

public class KafkaShowBacksDemoModule {

	@Provides
	@Singleton
	@Named("confluentCloudCustomMetricClient")
	static ClusterMetricClient confluentCloudCustomMetricClient(final KafkaShowBacksDemoConfiguration kafkaShowBacksDemoConfiguration, final RetryOnError retryOnError) {
		return new ConfluentCloudMetricClient(kafkaShowBacksDemoConfiguration.getConfluentApiKey(), kafkaShowBacksDemoConfiguration.getConfluentApiSecret(),
				kafkaShowBacksDemoConfiguration.getRequestTimeOutInSeconds(), kafkaShowBacksDemoConfiguration.getTelemetryUrl(), retryOnError);
	}
}
