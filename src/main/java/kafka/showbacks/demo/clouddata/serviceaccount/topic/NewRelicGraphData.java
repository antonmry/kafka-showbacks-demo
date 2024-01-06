package kafka.showbacks.demo.clouddata.serviceaccount.topic;

import java.util.Arrays;

//todo package
record NewRelicGraphData(String[][] members) {
	public NewRelicGraphData(String[][] members) {
		this.members = Arrays.stream(members).map(String[]::clone).toArray(String[][]::new);
	}

	@Override
	public String[][] members() {
		return Arrays.stream(this.members).map(String[]::clone).toArray(String[][]::new);
	}
}
