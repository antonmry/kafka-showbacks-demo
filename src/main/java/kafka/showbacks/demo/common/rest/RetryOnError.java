package kafka.showbacks.demo.common.rest;

import com.google.common.base.Joiner;
import kafka.showbacks.demo.common.exception.KafkaShowBackDemoException;

import java.net.HttpURLConnection;
import java.util.Set;

public class RetryOnError {
	private static final int HTTP_TO_MANY_REQUESTS = 429;

	private static final Set<Integer> STATUS_CODE_ALLOWED_TO_RETRY = Set.of(HttpURLConnection.HTTP_CLIENT_TIMEOUT,
			HttpURLConnection.HTTP_UNAVAILABLE,
			HttpURLConnection.HTTP_BAD_GATEWAY,
			HttpURLConnection.HTTP_GATEWAY_TIMEOUT,
			HTTP_TO_MANY_REQUESTS);

	private int numRequestRetries;
	private int timeToRetryRequestMS;

	private final int initialNumRequestRetries;
	private final int initialTimeToRequestRetryMS;

	public RetryOnError(final int numRequestRetries,
	                    final int timeToRetryRequestMS) {
		this.numRequestRetries = numRequestRetries;
		this.timeToRetryRequestMS = timeToRetryRequestMS;
		this.initialNumRequestRetries = numRequestRetries;
		this.initialTimeToRequestRetryMS = timeToRetryRequestMS;
	}

	boolean shouldRetry(final int statusCode) throws KafkaShowBackDemoException {
		if ((numRequestRetries >= 0) && STATUS_CODE_ALLOWED_TO_RETRY.contains(statusCode)) {
			return true;
		} else if (numRequestRetries < 0) {
			throw new KafkaShowBackDemoException(Joiner.on(" ").join("Number of retries spend , code error number", statusCode));
		}
		return false;
	}

	private void waitUntilNextTry() throws InterruptedException {
		Thread.sleep(timeToRetryRequestMS);
	}

	void errorOccurred() throws InterruptedException {
		--numRequestRetries;
		//each time that try the connection wait the double of time defined
		timeToRetryRequestMS = timeToRetryRequestMS * 2;
		waitUntilNextTry();
	}

	void restart() {
		this.numRequestRetries = this.initialNumRequestRetries;
		this.timeToRetryRequestMS = this.initialTimeToRequestRetryMS;
	}
}
