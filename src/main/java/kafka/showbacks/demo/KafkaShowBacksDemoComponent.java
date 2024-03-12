package kafka.showbacks.demo;

import dagger.BindsInstance;
import dagger.Component;
import io.dropwizard.core.setup.Environment;
import kafka.showbacks.demo.configuration.KafkaShowBacksDemoConfiguration;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Component(modules = KafkaShowBacksDemoModule.class)
interface KafkaShowBacksDemoComponent {

	@Named("confluentKafkaShowBacksDemoService")
	KafkaShowBacksDemoService buildConfluentKafkaShowBacksDemoService();

	@Component.Builder
	interface Builder {
		@BindsInstance
		Builder configuration(KafkaShowBacksDemoConfiguration configuration);

		@BindsInstance
		Builder environment(Environment environment);

		KafkaShowBacksDemoComponent build();
	}
}
