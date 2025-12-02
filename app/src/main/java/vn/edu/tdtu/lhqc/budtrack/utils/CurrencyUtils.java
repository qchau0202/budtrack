package vn.edu.tdtu.lhqc.budtrack.utils;

import android.content.Context;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import vn.edu.tdtu.lhqc.budtrack.controllers.settings.SettingsHandler;

/**
 * Utility class for currency and number formatting operations.
 * Provides methods for formatting currency amounts with proper localization and grouping.
 * Uses "." (dot) as the thousand separator.
 * Supports dynamic currency selection (VND or USD) with automatic conversion.
 */
public final class CurrencyUtils {

    private CurrencyUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Creates a NumberFormat instance with "." (dot) as the thousand separator.
     * 
     * @return A NumberFormat instance configured with dot as grouping separator
     */
    private static NumberFormat createFormatter() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        DecimalFormat formatter = new DecimalFormat("#,##0.###", symbols);
        formatter.setGroupingUsed(true);
        formatter.setGroupingSize(3);
        return formatter;
    }

    /**
     * Converts amount from VND to the selected currency.
     * @param context Application context
     * @param amountVnd Amount in VND (stored currency)
     * @return Amount in selected currency
     */
    private static double convertToSelectedCurrency(Context context, long amountVnd) {
        String currency = SettingsHandler.getCurrency(context);
        if ("USD".equals(currency)) {
            float exchangeRate = SettingsHandler.getExchangeRate(context);
            if (exchangeRate > 0) {
                return amountVnd / exchangeRate;
            }
        }
        return amountVnd;
    }

    /**
     * Converts amount from VND to the selected currency.
     * @param context Application context
     * @param amountVnd Amount in VND (stored currency)
     * @return Amount in selected currency
     */
    private static double convertToSelectedCurrency(Context context, double amountVnd) {
        String currency = SettingsHandler.getCurrency(context);
        if ("USD".equals(currency)) {
            float exchangeRate = SettingsHandler.getExchangeRate(context);
            if (exchangeRate > 0) {
                return amountVnd / exchangeRate;
            }
        }
        return amountVnd;
    }

    /**
     * Gets the currency symbol/name for display.
     * @param context Application context
     * @return Currency string (VND or USD)
     */
    private static String getCurrencySymbol(Context context) {
        return SettingsHandler.getCurrency(context);
    }

    /**
     * Formats a currency amount with thousand separators and currency suffix.
     * Automatically converts from VND (stored) to selected currency if needed.
     * Uses "." (dot) as the thousand separator.
     * 
     * @param context Application context
     * @param amountVnd The amount in VND to format (can be negative)
     * @return Formatted string like "1.234.567 VND" or "1,234.56 USD"
     */
    public static String formatCurrency(Context context, double amountVnd) {
        if (context == null) {
            // Fallback to VND if context is null
            NumberFormat formatter = createFormatter();
            String formatted = formatter.format(Math.abs(amountVnd));
            return (amountVnd < 0 ? "-" : "") + formatted + " VND";
        }
        
        double amount = convertToSelectedCurrency(context, amountVnd);
        String currency = getCurrencySymbol(context);
        NumberFormat formatter = createFormatter();
        
        // For USD, show 2 decimal places; for VND, show no decimals
        if ("USD".equals(currency)) {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
            symbols.setGroupingSeparator(',');
            symbols.setDecimalSeparator('.');
            DecimalFormat usdFormatter = new DecimalFormat("#,##0.00", symbols);
            usdFormatter.setGroupingUsed(true);
            usdFormatter.setGroupingSize(3);
            String formatted = usdFormatter.format(Math.abs(amount));
            return (amountVnd < 0 ? "-" : "") + formatted + " USD";
        } else {
            String formatted = formatter.format(Math.abs(amount));
            return (amountVnd < 0 ? "-" : "") + formatted + " VND";
        }
    }

    /**
     * Formats a currency amount with thousand separators and currency suffix.
     * Automatically converts from VND (stored) to selected currency if needed.
     * Uses "." (dot) as the thousand separator.
     * Overloaded method for long values.
     * 
     * @param context Application context
     * @param amountVnd The amount in VND to format (can be negative)
     * @return Formatted string like "1.234.567 VND" or "1,234.56 USD"
     */
    public static String formatCurrency(Context context, long amountVnd) {
        return formatCurrency(context, (double) amountVnd);
    }

    /**
     * Formats a currency amount with thousand separators and VND suffix.
     * Uses "." (dot) as the thousand separator.
     * @deprecated Use formatCurrency(Context, long) instead for dynamic currency support
     * 
     * @param amount The amount to format (can be negative)
     * @return Formatted string like "1.234.567 VND" or "-1.234.567 VND"
     */
    @Deprecated
    public static String formatCurrency(double amount) {
        NumberFormat formatter = createFormatter();
        String formatted = formatter.format(Math.abs(amount));
        return (amount < 0 ? "-" : "") + formatted + " VND";
    }

    /**
     * Formats a currency amount with thousand separators and VND suffix.
     * Uses "." (dot) as the thousand separator.
     * @deprecated Use formatCurrency(Context, long) instead for dynamic currency support
     * Overloaded method for long values.
     * 
     * @param amount The amount to format (can be negative)
     * @return Formatted string like "1.234.567 VND" or "-1.234.567 VND"
     */
    @Deprecated
    public static String formatCurrency(long amount) {
        NumberFormat formatter = createFormatter();
        String formatted = formatter.format(Math.abs(amount));
        return (amount < 0 ? "-" : "") + formatted + " VND";
    }

    /**
     * Formats a number with thousand separators (without currency suffix).
     * Uses "." (dot) as the thousand separator.
     * 
     * @param number The number to format
     * @return Formatted string like "1.234.567"
     */
    public static String formatNumber(double number) {
        NumberFormat formatter = createFormatter();
        return formatter.format(number);
    }

    /**
     * Formats a number with thousand separators (without currency suffix).
     * Uses "." (dot) as the thousand separator.
     * Overloaded method for long values.
     * 
     * @param number The number to format
     * @return Formatted string like "1.234.567"
     */
    public static String formatNumber(long number) {
        NumberFormat formatter = createFormatter();
        return formatter.format(number);
    }

    /**
     * Formats a number with thousand separators using "." (dot) as separator.
     * This is typically used for EditText formatting where dots are used as separators.
     * 
     * @param number The number to format
     * @return Formatted string like "1.234.567"
     */
    public static String formatNumberUS(double number) {
        NumberFormat formatter = createFormatter();
        return formatter.format(number);
    }

    /**
     * Formats a number with thousand separators using "." (dot) as separator.
     * This is typically used for EditText formatting where dots are used as separators.
     * Overloaded method for long values.
     * 
     * @param number The number to format
     * @return Formatted string like "1.234.567"
     */
    public static String formatNumberUS(long number) {
        NumberFormat formatter = createFormatter();
        return formatter.format(number);
    }

    /**
     * Parses a formatted number string (with dots as thousand separators, commas as decimal separators, spaces) to a double.
     * 
     * @param formattedNumber The formatted number string (e.g., "1.234.567" or "1.234.567,89")
     * @return The parsed double value
     * @throws NumberFormatException if the string cannot be parsed
     */
    public static double parseFormattedNumber(String formattedNumber) throws NumberFormatException {
        if (formattedNumber == null || formattedNumber.trim().isEmpty()) {
            return 0.0;
        }
        String trimmed = formattedNumber.trim();
        // Check if there's a comma (decimal separator)
        int commaIndex = trimmed.lastIndexOf(',');
        if (commaIndex >= 0) {
            // Has decimal part: remove dots (thousand separators) from integer part, replace comma with dot
            String integerPart = trimmed.substring(0, commaIndex).replaceAll("[.\\s]", "");
            String decimalPart = trimmed.substring(commaIndex + 1).replaceAll("[.\\s]", "");
            return Double.parseDouble(integerPart + "." + decimalPart);
        } else {
            // No decimal part: just remove dots (thousand separators) and spaces
            String cleanString = trimmed.replaceAll("[.\\s]", "");
            return Double.parseDouble(cleanString);
        }
    }

    /**
     * Parses a formatted number string (with dots as thousand separators, spaces) to a long.
     * Removes all formatting characters before parsing.
     * 
     * @param formattedNumber The formatted number string (e.g., "1.234.567")
     * @return The parsed long value
     * @throws NumberFormatException if the string cannot be parsed
     */
    public static long parseFormattedNumberLong(String formattedNumber) throws NumberFormatException {
        if (formattedNumber == null || formattedNumber.trim().isEmpty()) {
            return 0L;
        }
        // Remove dots (thousand separators), commas (if any), and whitespace
        String cleanString = formattedNumber.replaceAll("[.,\\s]", "").trim();
        return Long.parseLong(cleanString);
    }
}

