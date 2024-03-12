package kafka.showbacks.demo;

import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import kafka.showbacks.demo.configuration.KafkaShowBacksDemoConfiguration;

class KafkaShowBackDemoApplication extends Application<KafkaShowBacksDemoConfiguration> {

	public static void main(String[] args) throws Exception {

		new KafkaShowBackDemoApplication().run(args);
	}

	@Override
	public void initialize(final Bootstrap<KafkaShowBacksDemoConfiguration> bootstrap) {
		bootstrap.setConfigurationSourceProvider(new SubstitutingSourceProvider(
				bootstrap.getConfigurationSourceProvider(), new EnvironmentVariableSubstitutor(false)
		));
	}

	@Override
	public void run(KafkaShowBacksDemoConfiguration configuration, Environment environment) {
		DaggerKafkaShowBacksDemoComponent.builder().
				configuration(configuration)
				.environment(environment)
				.build().buildConfluentKafkaShowBacksDemoService().startAsync();
	}


}
