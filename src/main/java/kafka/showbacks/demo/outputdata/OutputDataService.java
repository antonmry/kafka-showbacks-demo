package kafka.showbacks.demo.outputdata;

import kafka.showbacks.demo.common.exception.KafkaShowBackDemoException;
import kafka.showbacks.demo.common.model.TeamCostData;

import java.util.Set;

public sealed interface OutputDataService permits NR1OutputDataService {

	void sendOutputData(final Set<TeamCostData> teamCostDataSet, final String kafkaProvider) throws KafkaShowBackDemoException;
}
