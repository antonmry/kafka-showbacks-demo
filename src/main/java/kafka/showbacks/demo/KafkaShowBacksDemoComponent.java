package kafka.showbacks.demo;

import dagger.Component;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Component(modules = KafkaShowBacksDemoModule.class)
public interface KafkaShowBacksDemoComponent {
	@Named("confluentTeamUsageCost")
	TeamUsageCost buildConfluentTeamUsageCost();

	/**
	 * TODO just is an example currently this return null
	 *
	 * @return
	 */
	@Named("mskTeamUsageCost")
	TeamUsageCost buildMSKTeamUsageCost();
}
