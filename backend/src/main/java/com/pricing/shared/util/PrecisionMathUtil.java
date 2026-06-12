package com.pricing.shared.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public final class PrecisionMathUtil {

    private static final int INTERNAL_SCALE = 10;
    private static final int PRESENTATION_SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    private static final MathContext MATH_CONTEXT = new MathContext(20, ROUNDING_MODE);

    private PrecisionMathUtil() {}

    public static BigDecimal multiply(BigDecimal a, BigDecimal b) {
        return a.multiply(b, MATH_CONTEXT).setScale(INTERNAL_SCALE, ROUNDING_MODE);
    }

    public static BigDecimal add(BigDecimal a, BigDecimal b) {
        return a.add(b).setScale(INTERNAL_SCALE, ROUNDING_MODE);
    }

    public static BigDecimal subtract(BigDecimal a, BigDecimal b) {
        return a.subtract(b).setScale(INTERNAL_SCALE, ROUNDING_MODE);
    }

    public static BigDecimal divide(BigDecimal a, BigDecimal b) {
        return a.divide(b, INTERNAL_SCALE, ROUNDING_MODE);
    }

    public static BigDecimal percentage(BigDecimal value, BigDecimal percentRate) {
        return multiply(value, percentRate.divide(BigDecimal.valueOf(100), INTERNAL_SCALE, ROUNDING_MODE));
    }

    public static BigDecimal applyPercentage(BigDecimal value, BigDecimal percentRate) {
        return add(value, percentage(value, percentRate));
    }

    public static BigDecimal round(BigDecimal value) {
        return value.setScale(PRESENTATION_SCALE, ROUNDING_MODE);
    }

    public static BigDecimal of(String value) {
        return new BigDecimal(value).setScale(INTERNAL_SCALE, ROUNDING_MODE);
    }

    public static BigDecimal of(double value) {
        return BigDecimal.valueOf(value).setScale(INTERNAL_SCALE, ROUNDING_MODE);
    }
}
