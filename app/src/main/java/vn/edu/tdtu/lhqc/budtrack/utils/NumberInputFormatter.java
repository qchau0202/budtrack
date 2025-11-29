package vn.edu.tdtu.lhqc.budtrack.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Utility class for formatting number inputs in EditText fields.
 * Automatically formats numbers with thousand separators (dots) as the user types.
 */
public final class NumberInputFormatter {

    private NumberInputFormatter() {
        // Private constructor to prevent instantiation
    }

    /**
     * Creates a DecimalFormat instance with "." (dot) as the thousand separator.
     * 
     * @return A DecimalFormat instance configured with dot as grouping separator
     */
    private static DecimalFormat createFormatter() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        DecimalFormat formatter = new DecimalFormat("#,##0.###", symbols);
        formatter.setGroupingUsed(true);
        formatter.setGroupingSize(3);
        return formatter;
    }

    /**
     * Attaches a text formatter to an EditText that formats numbers with dots as the user types.
     * Supports both double and long values.
     * 
     * @param editText The EditText to attach the formatter to
     */
    public static void attach(EditText editText) {
        attach(editText, null);
    }

    /**
     * Attaches a text formatter to an EditText that formats numbers with dots as the user types.
     * Supports both double and long values.
     * 
     * @param editText The EditText to attach the formatter to
     * @param initialValue The initial formatted value (optional, can be null)
     */
    public static void attach(EditText editText, String initialValue) {
        if (editText == null) {
            return;
        }

        final String[] currentValue = {initialValue != null ? initialValue : ""};
        final boolean[] isFormatting = {false};

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No-op
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No-op
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Prevent infinite loop during formatting
                if (isFormatting[0]) {
                    return;
                }

                // Skip if the value hasn't changed
                if (s.toString().equals(currentValue[0])) {
                    return;
                }

                isFormatting[0] = true;
                editText.removeTextChangedListener(this);

                // Remove dots (thousand separators), commas (if any), and whitespace for parsing
                String cleanString = s.toString().replaceAll("[.,\\s]", "");

                if (!cleanString.isEmpty()) {
                    try {
                        // Try parsing as double first to handle decimal values
                        double parsed = Double.parseDouble(cleanString);
                        DecimalFormat formatter = createFormatter();
                        String formatted = formatter.format(parsed);
                        
                        // If original was an integer, remove decimal part (comma and zeros)
                        if (cleanString.matches("\\d+")) {
                            // Remove comma decimal separator and zeros
                            formatted = formatted.replaceAll(",\\d+$", "");
                        }
                        
                        currentValue[0] = formatted;
                        editText.setText(formatted);
                        editText.setSelection(formatted.length());
                    } catch (NumberFormatException e) {
                        // Invalid number format, keep current value
                        currentValue[0] = s.toString();
                    }
                } else {
                    currentValue[0] = "";
                    editText.setText("");
                }

                editText.addTextChangedListener(this);
                isFormatting[0] = false;
            }
        });
    }

    /**
     * Attaches a text formatter that only accepts integer/long values (no decimals).
     * 
     * @param editText The EditText to attach the formatter to
     * @param initialValue The initial formatted value (optional, can be null)
     */
    public static void attachIntegerFormatter(EditText editText, String initialValue) {
        if (editText == null) {
            return;
        }

        final String[] currentValue = {initialValue != null ? initialValue : ""};
        final boolean[] isFormatting = {false};

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No-op
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No-op
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Prevent infinite loop during formatting
                if (isFormatting[0]) {
                    return;
                }

                // Skip if the value hasn't changed
                if (s.toString().equals(currentValue[0])) {
                    return;
                }

                isFormatting[0] = true;
                editText.removeTextChangedListener(this);

                String cleanString = s.toString().replaceAll("[.,\\s]", "");

                if (!cleanString.isEmpty()) {
                    try {
                        long parsed = Long.parseLong(cleanString);
                        DecimalFormat formatter = createFormatter();
                        formatter.setMaximumFractionDigits(0);
                        String formatted = formatter.format(parsed);
                        currentValue[0] = formatted;
                        editText.setText(formatted);
                        editText.setSelection(formatted.length());
                    } catch (NumberFormatException e) {
                        // Invalid number format, keep current value
                        currentValue[0] = s.toString();
                    }
                } else {
                    currentValue[0] = "";
                    editText.setText("");
                }

                editText.addTextChangedListener(this);
                isFormatting[0] = false;
            }
        });
    }
}

