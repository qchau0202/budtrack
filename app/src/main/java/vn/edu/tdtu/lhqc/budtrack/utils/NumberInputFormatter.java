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
 * Decimal separator is assumed to be the comma (,).
 */
public final class NumberInputFormatter {

    public NumberInputFormatter() {
        // Private constructor to prevent instantiation
    }

    /**
     * Creates a DecimalFormat instance with "." (dot) as the thousand separator.
     * * @return A DecimalFormat instance configured with dot as grouping separator
     */
    private static DecimalFormat createFormatter() {
        // Use default locale but override symbols for VND/EU format (dot grouping, comma decimal)
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        // Pattern: #,##0.###
        DecimalFormat formatter = new DecimalFormat("#,##0.###", symbols);
        formatter.setGroupingUsed(true);
        formatter.setGroupingSize(3);
        return formatter;
    }

    /**
     * Attaches a text formatter to an EditText that formats numbers with dots as the user types.
     * Supports both double and long values.
     * * @param editText The EditText to attach the formatter to
     */
    public static void attach(EditText editText) {
        attach(editText, null);
    }

    /**
     * Attaches a text formatter to an EditText that formats numbers with dots as the user types.
     * Supports both double and long values.
     * * @param editText The EditText to attach the formatter to
     * @param initialValue The initial formatted value (optional, can be null)
     */
    public static void attach(EditText editText, String initialValue) {
        if (editText == null) {
            return;
        }

        // We don't need currentValue array if we check against isFormatting
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

                isFormatting[0] = true;

                // Remove the listener temporarily to prevent infinite loop
                editText.removeTextChangedListener(this);

                String originalString = s.toString();
                String formattedString = originalString; // Default: no change

                // 1. Chuẩn bị chuỗi để phân tích (US Format: dấu chấm thập phân)
                // Loại bỏ tất cả dấu chấm (nghìn), thay dấu phẩy (thập phân) bằng dấu chấm
                String parsableString = originalString.replace(".", "").replace(',', '.').replaceAll("\\s", "");

                try {
                    if (parsableString.isEmpty() || parsableString.equals(".")) {
                        // Nếu rỗng hoặc chỉ có dấu chấm (do người dùng gõ)
                        formattedString = "";
                    } else {
                        // 2. Kiểm tra nếu chuỗi chỉ kết thúc bằng dấu chấm (VD: "123.")
                        // Chỉ parse nếu nó không kết thúc bằng dấu thập phân (để người dùng có thể nhập)
                        boolean endsWithDecimal = originalString.endsWith(",");

                        // Parse số
                        double parsedNumber = Double.parseDouble(parsableString);
                        DecimalFormat formatter = createFormatter();

                        // 3. Cấu hình độ chính xác thập phân
                        if (parsableString.contains(".")) {
                            // Nếu có phần thập phân, giữ lại độ chính xác mà người dùng đang gõ
                            int decimalPlaces = parsableString.length() - parsableString.indexOf('.') - 1;
                            formatter.setMaximumFractionDigits(decimalPlaces);
                            formatter.setMinimumFractionDigits(decimalPlaces);
                        } else {
                            // Nếu là số nguyên, không hiển thị phần thập phân
                            formatter.setMaximumFractionDigits(0);
                        }

                        formattedString = formatter.format(parsedNumber);

                        // 4. Khôi phục dấu thập phân nếu người dùng đang nhập dở
                        if (endsWithDecimal) {
                            // Nếu chuỗi gốc kết thúc bằng dấu phẩy (decimal separator)
                            formattedString += ",";
                        }
                    }
                } catch (NumberFormatException e) {
                    // Nếu chuỗi không hợp lệ (ví dụ: "1,2,3" sau khi loại bỏ dấu chấm)
                    // Ta giữ nguyên chuỗi cũ và chỉ loại bỏ các ký tự không hợp lệ
                    formattedString = originalString.replaceAll("[^0-9,.]", "");
                }


                // 5. Cập nhật EditText nếu có sự thay đổi
                if (!originalString.equals(formattedString)) {
                    editText.setText(formattedString);
                    // Đặt con trỏ về cuối
                    editText.setSelection(formattedString.length());
                }

                // Add the listener back
                editText.addTextChangedListener(this);
                isFormatting[0] = false;
            }
        });
    }
    public static String formatUSStringToVNDEdittext(String usFormattedString) {
        if (usFormattedString == null || usFormattedString.isEmpty()) {
            return "";
        }

        // 1. Loại bỏ tất cả dấu phẩy (thousand separators trong chuẩn US)
        String stringWithoutCommas = usFormattedString.replaceAll(",", "");

        // 2. Chuyển dấu chấm thập phân (decimal separator trong chuẩn US) thành dấu phẩy (chuẩn VND/EU)
        String vndFormattedString = stringWithoutCommas.replace('.', ',');

        // 3. Sử dụng DecimalFormat để áp dụng lại dấu chấm phân cách hàng nghìn (chuẩn VND/EU)
        try {
            // Chuẩn bị cho việc parse: thay dấu phẩy bằng dấu chấm để Double.parseDouble hiểu
            String parsableString = vndFormattedString.replace(',', '.');
            double parsed = Double.parseDouble(parsableString);

            // Tạo formatter chuẩn VND/EU
            DecimalFormat formatter = createFormatter();

            // Nếu có phần thập phân, giữ lại 2 chữ số
            if (vndFormattedString.contains(",")) {
                formatter.setMaximumFractionDigits(2);
                formatter.setMinimumFractionDigits(2);
            } else {
                formatter.setMaximumFractionDigits(0);
            }

            return formatter.format(parsed);

        } catch (NumberFormatException e) {
            // Nếu không thể parse (chỉ có dấu chấm/phẩy), trả về nguyên trạng
            return vndFormattedString;
        }
    }

    /**
     * Attaches a text formatter that only accepts integer/long values (no decimals).
     * * @param editText The EditText to attach the formatter to
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
                editText.removeTextChangedListener(this); // Remove listener

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

                editText.addTextChangedListener(this); // Add listener back
                isFormatting[0] = false;
            }
        });
    }
}