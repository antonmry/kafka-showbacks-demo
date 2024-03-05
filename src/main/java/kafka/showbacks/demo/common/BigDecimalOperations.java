package kafka.showbacks.demo.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public final class BigDecimalOperations {
	private static final Logger log = LogManager.getLogger();

	private static final MathContext DEFAULT_MATH_CONTEXT = new MathContext(34, RoundingMode.DOWN);
	private static final BigDecimal ONE_HUNDRED_VALUES = new BigDecimal("100");

	public static final int IS_LESS_BIG_DECIMAL = -1;
	public static final int IS_GREATER_BIG_DECIMAL = 1;
	public static final int IS_EQUAL_BIG_DECIMAL = 0;

	public static BigDecimal subtract(final BigDecimal subtract, final BigDecimal subtrahend) {
		return subtract.subtract(subtrahend, DEFAULT_MATH_CONTEXT);
	}

	public static BigDecimal multiply(final BigDecimal multiply, final BigDecimal multiplicand) {
		return multiply.multiply(multiplicand, DEFAULT_MATH_CONTEXT);
	}

	public static BigDecimal add(final BigDecimal firstValue, final BigDecimal secondValue) {
		return firstValue.add(secondValue, DEFAULT_MATH_CONTEXT);
	}

	public static BigDecimal divide(final BigDecimal dividend, final BigDecimal divisor) {
		if (divisor.compareTo(BigDecimal.ZERO) == IS_EQUAL_BIG_DECIMAL) {
			log.warn("Divisor  is zero, so we can not calculate the division.");
			return BigDecimal.ZERO;
		}
		try {
			return dividend.divide(divisor, DEFAULT_MATH_CONTEXT);
		} catch (ArithmeticException arithmeticException) {
			log.error("Error dividing, dividend: {}; divisor: {}; error: {}",
					dividend, divisor, arithmeticException.getMessage());
		}
		return BigDecimal.ZERO;
	}

	public static BigDecimal getPercentage(final BigDecimal valueToGetPercentage, final BigDecimal totalValue) {
		if (totalValue.compareTo(BigDecimal.ZERO) == IS_EQUAL_BIG_DECIMAL) {
			log.warn("Total value is zero, so we can not get the  percentage.");
			return BigDecimal.ZERO;
		}
		try {
			return valueToGetPercentage
					.divide(totalValue, DEFAULT_MATH_CONTEXT).multiply(ONE_HUNDRED_VALUES, DEFAULT_MATH_CONTEXT);
		} catch (ArithmeticException arithmeticException) {
			log.error("Error calculating the percentage, valueToGetPercentage: {}; totalValue: {}; error: {}",
					valueToGetPercentage, totalValue, arithmeticException.getMessage());
		}

		return BigDecimal.ZERO;
	}

	public static BigDecimal getValueFromPercentage(final BigDecimal totalValue, final BigDecimal percentage) {
		try {
			return totalValue.multiply(percentage, DEFAULT_MATH_CONTEXT).divide(ONE_HUNDRED_VALUES, DEFAULT_MATH_CONTEXT);
		} catch (ArithmeticException arithmeticException) {
			log.error("Error , valueToGetPercentage: {}; totalValue: {}; error: {}",
					percentage, totalValue, arithmeticException.getMessage());
		}

		return BigDecimal.ZERO;
	}
}
