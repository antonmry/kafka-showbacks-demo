package kafka.showbacks.demo;

import dagger.Provides;
import kafka.showbacks.demo.clouddata.ConfluentCloudServiceClient;
import kafka.showbacks.demo.clouddata.billing.ConfluentCloudCostService;
import kafka.showbacks.demo.clouddata.serviceaccount.ConfluentCloudServiceAccountCache;
import kafka.showbacks.demo.clouddata.serviceaccount.topic.NewRelicGraphClient;
import kafka.showbacks.demo.clouddata.serviceaccount.topic.NewRelicGraphServiceAccountTopicCache;
import kafka.showbacks.demo.clouddata.serviceaccount.topic.ServiceAccountTopic;
import kafka.showbacks.demo.clustermetrics.ClusterMetricClient;
import kafka.showbacks.demo.clustermetrics.ClusterMetricService;
import kafka.showbacks.demo.clustermetrics.ConfluentCloudMetricClient;
import kafka.showbacks.demo.clustermetrics.ConfluentCloudMetricService;
import kafka.showbacks.demo.common.rest.RetryOnError;
import kafka.showbacks.demo.configuration.KafkaShowBacksDemoConfiguration;

import javax.inject.Named;
import javax.inject.Singleton;

public interface KafkaShowBacksDemoModule {

	@Provides
	@Singleton
	@Named("confluentCloudCustomMetricClient")
	static ClusterMetricClient confluentCloudCustomMetricClient(final KafkaShowBacksDemoConfiguration kafkaShowBacksDemoConfiguration, final RetryOnError retryOnError) {
		return new ConfluentCloudMetricClient(kafkaShowBacksDemoConfiguration.getConfluentApiKey(), kafkaShowBacksDemoConfiguration.getConfluentApiSecret(),
				kafkaShowBacksDemoConfiguration.getRequestTimeOutInSeconds(), kafkaShowBacksDemoConfiguration.getTelemetryUrl(), retryOnError);
	}

	@Provides
	@Singleton
	@Named("confluentCloudMetricService")
	static ClusterMetricService confluentMetricService(@Named("confluentCloudCustomMetricClient") final ClusterMetricClient confluentCloudCustomMetricClient, final KafkaShowBacksDemoConfiguration kafkaShowBacksDemoConfiguration) {
		return new ConfluentCloudMetricService(confluentCloudCustomMetricClient, kafkaShowBacksDemoConfiguration.getCacheExpiredInHours());
	}

	@Provides
	@Singleton
	static ConfluentCloudServiceAccountCache confluentServiceAccountCache(final ConfluentCloudServiceClient confluentServiceAccountClient,
	                                                                      final KafkaShowBacksDemoConfiguration kafkaShowBacksDemoConfiguration) {
		return new ConfluentCloudServiceAccountCache(confluentServiceAccountClient, kafkaShowBacksDemoConfiguration.getCacheExpiredInHours(), kafkaShowBacksDemoConfiguration.getCloudServiceAccountUrl());
	}

	@Provides
	@Singleton
	static ConfluentCloudServiceClient confluentServiceAccountClient(final KafkaShowBacksDemoConfiguration kafkaShowBacksDemoConfiguration, final RetryOnError retryOnError) {
		return new ConfluentCloudServiceClient(kafkaShowBacksDemoConfiguration.getConfluentApiKey(), kafkaShowBacksDemoConfiguration.getConfluentApiSecret(),
				kafkaShowBacksDemoConfiguration.getRequestTimeOutInSeconds(), retryOnError);
	}

	@Provides
	@Singleton
	static ConfluentCloudCostService confluentCloudCostService(final ConfluentCloudServiceClient confluentCloudServiceClient,
	                                                           final KafkaShowBacksDemoConfiguration kafkaShowBacksDemoConfiguration) {
		return new ConfluentCloudCostService(confluentCloudServiceClient, kafkaShowBacksDemoConfiguration.getCloudBillingUrl());
	}

	@Provides
	@Singleton
	static NewRelicGraphClient newRelicGraphClient(final KafkaShowBacksDemoConfiguration kafkaShowBacksDemoConfiguration,
	                                               final RetryOnError retryOnError) {
		return new NewRelicGraphClient(retryOnError, kafkaShowBacksDemoConfiguration.getRequestTimeOutInSeconds(),
				kafkaShowBacksDemoConfiguration.getNewRelicGraphUrl(), kafkaShowBacksDemoConfiguration.getNewRelicApiKey());
	}

	@Provides
	@Singleton
	@Named("newRelicGraphServiceAccountTopicCache")
	static ServiceAccountTopic newRelicGraphServiceAccountTopicCache(final NewRelicGraphClient newRelicGraphClient,
	                                                                 final KafkaShowBacksDemoConfiguration kafkaShowBacksDemoConfiguration) {
		return new NewRelicGraphServiceAccountTopicCache(newRelicGraphClient,
				kafkaShowBacksDemoConfiguration.getCacheExpiredInHours(),
				kafkaShowBacksDemoConfiguration.getNewRelicAccountId());
	}

	@Provides
	@Singleton
	@Named("confluentCloudCustomMetricClient") //todo add exemple to other kafka environment
	static KafkaShowBacksDemo confluentKafkaShowBackDemo(@Named("confluentCloudMetricService") final ClusterMetricService confluentClusterMetricService,
	                                                     final ConfluentCloudServiceAccountCache confluentCloudServiceAccountCache,
	                                                     final ConfluentCloudCostService confluentCloudCostService,
	                                                     final ServiceAccountTopic serviceAccountTopic) {
		return new ConfluentKafkaShowBacksDemo(confluentClusterMetricService, confluentCloudServiceAccountCache, confluentCloudCostService, serviceAccountTopic);
	}
}
