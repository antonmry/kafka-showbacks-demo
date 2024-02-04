package kafka.showbacks.demo;

import dagger.Module;
import dagger.Provides;
import kafka.showbacks.demo.clouddata.ConfluentCloudServiceClient;
import kafka.showbacks.demo.clouddata.billing.ConfluentCloudCostService;
import kafka.showbacks.demo.clouddata.serviceaccount.ConfluentCloudServiceAccountCache;
import kafka.showbacks.demo.clustermetrics.ClusterMetricClient;
import kafka.showbacks.demo.clustermetrics.ClusterMetricService;
import kafka.showbacks.demo.clustermetrics.ConfluentCloudMetricClient;
import kafka.showbacks.demo.clustermetrics.ConfluentCloudMetricService;
import kafka.showbacks.demo.common.rest.RetryOnError;
import kafka.showbacks.demo.configuration.ConfluentConfiguration;
import kafka.showbacks.demo.configuration.KafkaShowBackDemoConfigurationModule;
import kafka.showbacks.demo.configuration.KafkaShowBacksDemoConfiguration;

import javax.inject.Named;
import javax.inject.Singleton;

@Module(includes = KafkaShowBackDemoConfigurationModule.class)
public interface KafkaShowBacksDemoModule {

	@Provides
	@Singleton
	@Named("confluentCloudCustomMetricClient") //todo configurations
	static ClusterMetricClient confluentCloudCustomMetricClient(final KafkaShowBacksDemoConfiguration kafkaShowBacksDemoCongifuration, final RetryOnError retryOnError) {
		return new ConfluentCloudMetricClient(kafkaShowBacksDemoCongifuration.getConfluentConfiguration().getConfluentApiKey(), kafkaShowBacksDemoCongifuration.getConfluentConfiguration().getConfluentApiSecret(),
				kafkaShowBacksDemoCongifuration.getRequestTimeOutInSeconds(), kafkaShowBacksDemoCongifuration.getConfluentConfiguration().getTelemetryUrl(), retryOnError);
	}

	@Provides
	@Singleton
	@Named("confluentCloudMetricService")
	static ClusterMetricService confluentCloudMetricService(@Named("confluentCloudCustomMetricClient") final ClusterMetricClient confluentCloudCustomMetricClient, final KafkaShowBacksDemoConfiguration kafkaShowBacksDemoConfiguration) {
		return new ConfluentCloudMetricService(confluentCloudCustomMetricClient, kafkaShowBacksDemoConfiguration.getCacheExpiredInHours());
	}

	@Provides
	@Singleton
	static ConfluentCloudServiceAccountCache confluentServiceAccountCache(final ConfluentCloudServiceClient confluentServiceAccountClient,
	                                                                      final KafkaShowBacksDemoConfiguration kafkaShowBacksDemoConfiguration) {
		return new ConfluentCloudServiceAccountCache(confluentServiceAccountClient, kafkaShowBacksDemoConfiguration.getCacheExpiredInHours(), kafkaShowBacksDemoConfiguration.getConfluentConfiguration().getCloudServiceAccountUrl());
	}

	@Provides
	@Singleton //todo singlenton ?? //todo configurations
	static ConfluentCloudServiceClient confluentServiceAccountClient(final ConfluentConfiguration confluentConfiguration, final KafkaShowBacksDemoConfiguration kafkaShowBacksDemoConfiguration, final RetryOnError retryOnError) {
		return new ConfluentCloudServiceClient(confluentConfiguration.getConfluentApiKey(), confluentConfiguration.getConfluentApiSecret(),
				kafkaShowBacksDemoConfiguration.getRequestTimeOutInSeconds(), retryOnError);
	}

	@Provides
	@Singleton
	static ConfluentCloudCostService confluentCloudCostService(final ConfluentCloudServiceClient confluentCloudServiceClient,
	                                                           final ConfluentConfiguration confluentConfiguration) {
		return new ConfluentCloudCostService(confluentCloudServiceClient, confluentConfiguration.getCloudBillingUrl());
	}

	/*@Provides
	@Singleton
	static NewRelicGraphClient newRelicGraphClient(final KafkaShowBacksDemoConfiguration kafkaShowBacksDemoConfiguration,
	                                               final RetryOnError retryOnError) {
		return new NewRelicGraphClient(retryOnError, kafkaShowBacksDemoConfiguration.getRequestTimeOutInSeconds(),
				kafkaShowBacksDemoConfiguration.getNewRelicGraphUrl(), kafkaShowBacksDemoConfiguration.getNewRelicApiKey());
	}*/

	/*@Provides
	@Singleton
	@Named("newRelicGraphServiceAccountTopicCache")
	static ServiceAccountTopic newRelicGraphServiceAccountTopicCache(final NewRelicGraphClient newRelicGraphClient,
	                                                                 final KafkaShowBacksDemoConfiguration kafkaShowBacksDemoConfiguration) {
		return new NewRelicGraphServiceAccountTopicCache(newRelicGraphClient,
				kafkaShowBacksDemoConfiguration.getCacheExpiredInHours(),
				kafkaShowBacksDemoConfiguration.getNewRelicAccountId());
	}*/

	@Provides
	@Singleton
	@Named("confluentCloudShowBacks") //todo add exemple to other kafka environment
	static KafkaShowBacksDemo confluentCloudShowBacks(@Named("confluentCloudMetricService") final ClusterMetricService confluentCloudMetricService,
	                                                  final ConfluentCloudServiceAccountCache confluentCloudServiceAccountCache,
	                                                  final ConfluentCloudCostService confluentCloudCostService) {
		return new ConfluentKafkaShowBacksDemo(confluentCloudMetricService, confluentCloudServiceAccountCache, confluentCloudCostService);

	}


	@Provides
	@Singleton
	@Named("KafkaShowBacksDemoService") //todo to remove msk
	static KafkaShowBacksDemoService KafkaShowBacksDemoService(@Named("confluentCloudShowBacks") final KafkaShowBacksDemo kafkaShowBacksDemo,
	                                                           final KafkaShowBacksDemoConfiguration kafkaShowBacksDemoConfiguration,
	                                                           final boolean isDemoMode) {
		return new KafkaShowBacksDemoService(kafkaShowBacksDemo, kafkaShowBacksDemoConfiguration.getConfluentConfiguration().getClustersIdList(), kafkaShowBacksDemoConfiguration.getInitialDelaySeconds(), kafkaShowBacksDemoConfiguration.getPeriodInSeconds(), isDemoMode);
	}

	/**
	 * Example other kafka environment
	 */
	@Provides
	@Singleton
	@Named("otherKafkaEnvironmentCloudShowBacks")
	static KafkaShowBacksDemo otherKafkaEnvironmentCloudShowBacks() {
		return null;
	}

	@Provides
	static RetryOnError retryOnError(final KafkaShowBacksDemoConfiguration kafkaShowBacksDemoConfiguration) {
		return new RetryOnError(kafkaShowBacksDemoConfiguration.getNumRequestRetries(),
				kafkaShowBacksDemoConfiguration.getTimeToRetryRequestCallMs());
	}

	@Provides
	@Singleton
	static ConfluentConfiguration confluentConfiguration(final KafkaShowBacksDemoConfiguration kafkaShowBacksDemoConfiguration) {
		return kafkaShowBacksDemoConfiguration.getConfluentConfiguration();
	}
	//TODO IN ANOTHER MODULE WITH HEALTH CHECK
	//todo ??
	/*@Provides
	@Singleton
	static Set<Service> autoServices(final KafkaShowBacksDemoService kafkaShowBacksDemoService) {
		return ImmutableSet.of(kafkaShowBacksDemoService);
	}*/
}
