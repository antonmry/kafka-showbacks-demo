package kafka.showbacks.demo.configuration;

import dagger.Binds;
import dagger.Module;
import io.dropwizard.core.Configuration;

import javax.inject.Singleton;

@Module
public interface KafkaShowBackDemoConfigurationModule {

	@Binds
	@Singleton
	Configuration KafkaShowBacksDemoConfiguration(KafkaShowBacksDemoConfiguration kafkaShowBacksDemoConfiguration);
}
