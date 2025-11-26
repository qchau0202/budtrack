package vn.edu.tdtu.lhqc.budtrack.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.ui.GeneralHeaderController;
import vn.edu.tdtu.lhqc.budtrack.fragments.TransactionFragmentCreate;

public class NotificationFragment extends Fragment {

    // Notification types
    public enum NotificationType {
        EXPENSE_REMINDER,
        BUDGET_OVERSPENDING,
        BUDGET_WARNING,
        BUDGET_LIMIT,
        DAILY_SUMMARY,
        WEEKLY_SUMMARY
    }

    // Notification data model
    private static class NotificationData {
        NotificationType type;
        String title;
        String message;
        String time;
        int iconResId;
        int iconBackgroundColorResId; // Color resource ID for circular background
        boolean isUnread;
        String groupLabel; // "Today", "Yesterday", etc.

        NotificationData(NotificationType type, String title, String message, String time,
                        int iconResId, int iconBackgroundColorResId, boolean isUnread, String groupLabel) {
            this.type = type;
            this.title = title;
            this.message = message;
            this.time = time;
            this.iconResId = iconResId;
            this.iconBackgroundColorResId = iconBackgroundColorResId;
            this.isUnread = isUnread;
            this.groupLabel = groupLabel;
        }
    }

    public NotificationFragment() {
        // Required empty public constructor
    }

    public static NotificationFragment newInstance() {
        return new NotificationFragment();
    }

    public static NotificationFragment newInstance(String param1, String param2) {
        NotificationFragment fragment = new NotificationFragment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_notification, container, false);

        GeneralHeaderController.setup(root, this);
        setupNotifications(root);

        return root;
    }

    private void setupNotifications(View root) {
        LinearLayout notificationsContainer = root.findViewById(R.id.notifications_container);

        if (notificationsContainer == null) {
            return;
        }

        // Create sample notification data grouped by time
        Map<String, List<NotificationData>> groupedNotifications = createGroupedNotifications();

        // Clear existing views
        notificationsContainer.removeAllViews();

        // Add grouped notifications
        for (Map.Entry<String, List<NotificationData>> entry : groupedNotifications.entrySet()) {
            // Add group header
            View groupHeader = createGroupHeader(notificationsContainer, entry.getKey());
            notificationsContainer.addView(groupHeader);

            // Add notifications in this group
            for (NotificationData notification : entry.getValue()) {
                View notificationView = createNotificationView(notificationsContainer, notification);
                notificationsContainer.addView(notificationView);
            }
        }
    }

    private Map<String, List<NotificationData>> createGroupedNotifications() {
        Map<String, List<NotificationData>> grouped = new LinkedHashMap<>();

        // Today's notifications
        List<NotificationData> today = new ArrayList<>();
        
        // Budget Overspending Alert
        today.add(new NotificationData(
            NotificationType.BUDGET_OVERSPENDING,
            getString(R.string.notification_budget_overspending_title),
            getString(R.string.notification_budget_overspending_message, 
                getString(R.string.budget_personal), formatCurrency(1150000)),
            "10:00 AM",
            R.drawable.ic_notifications_24dp,
            R.color.category_tab_red,
            true,
            getString(R.string.notification_today)
        ));

        // Budget Warning
        today.add(new NotificationData(
            NotificationType.BUDGET_WARNING,
            getString(R.string.notification_budget_warning_title),
            getString(R.string.notification_budget_warning_message,
                85, getString(R.string.budget_daily), formatCurrency(1500000)),
            "10:50 AM",
            R.drawable.ic_account_balance_wallet_24dp,
            R.color.category_tab_yellow,
            true,
            getString(R.string.notification_today)
        ));

        // Daily Expense Reminder
        today.add(new NotificationData(
            NotificationType.EXPENSE_REMINDER,
            getString(R.string.notification_expense_reminder_title),
            getString(R.string.notification_expense_reminder_message),
            "11:25 AM",
            R.drawable.ic_add_circle_24dp,
            R.color.category_tab_green,
            true,
            getString(R.string.notification_today)
        ));

        // Daily Summary
        today.add(new NotificationData(
            NotificationType.DAILY_SUMMARY,
            getString(R.string.notification_daily_summary_title),
            getString(R.string.notification_daily_summary_message,
                formatCurrency(450000), 3),
            "2:30 PM",
            R.drawable.ic_analytics_24dp,
            R.color.category_tab_green,
            false,
            getString(R.string.notification_today)
        ));

        grouped.put(getString(R.string.notification_today), today);

        // Yesterday's notifications
        List<NotificationData> yesterday = new ArrayList<>();
        
        // Budget Limit Reached
        yesterday.add(new NotificationData(
            NotificationType.BUDGET_LIMIT,
            getString(R.string.notification_budget_limit_title),
            getString(R.string.notification_budget_limit_message,
                95, getString(R.string.budget_personal)),
            "Yesterday",
            R.drawable.ic_account_balance_wallet_24dp,
            R.color.category_tab_yellow,
            false,
            getString(R.string.notification_yesterday)
        ));

        // Weekly Summary
        yesterday.add(new NotificationData(
            NotificationType.WEEKLY_SUMMARY,
            getString(R.string.notification_weekly_summary_title),
            getString(R.string.notification_weekly_summary_message,
                formatCurrency(12500000), formatCurrency(2500000) + " more"),
            "Yesterday",
            R.drawable.ic_analytics_24dp,
            R.color.category_tab_green,
            false,
            getString(R.string.notification_yesterday)
        ));

        grouped.put(getString(R.string.notification_yesterday), yesterday);

        return grouped;
    }

    private View createGroupHeader(ViewGroup parent, String label) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View header = inflater.inflate(R.layout.item_notification_group_header, parent, false);
        TextView tvHeader = header.findViewById(R.id.tv_group_header);
        TextView tvMarkAllRead = header.findViewById(R.id.tv_mark_all_read);
        
        if (tvHeader != null) {
            tvHeader.setText(label);
        }
        
        if (tvMarkAllRead != null) {
            tvMarkAllRead.setOnClickListener(v -> markAllNotificationsAsRead(label));
        }
        
        return header;
    }
    
    private void markAllNotificationsAsRead(String groupLabel) {
        // Find all notifications in this group and mark them as read
        LinearLayout container = getView() != null ? getView().findViewById(R.id.notifications_container) : null;
        if (container == null) return;
        
        boolean foundGroup = false;
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            TextView groupHeader = child.findViewById(R.id.tv_group_header);
            
            if (groupHeader != null && groupLabel.equals(groupHeader.getText().toString())) {
                foundGroup = true;
                // Mark all notifications in this group as read
                for (int j = i + 1; j < container.getChildCount(); j++) {
                    View nextChild = container.getChildAt(j);
                    View badge = nextChild.findViewById(R.id.badge_unread);
                    if (badge != null) {
                        badge.setVisibility(View.GONE);
                    } else if (nextChild.findViewById(R.id.tv_group_header) != null) {
                        // Reached next group, stop
                        break;
                    }
                }
                break;
            }
        }
    }

    private View createNotificationView(ViewGroup parent, NotificationData notification) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View notificationView = inflater.inflate(R.layout.item_notification, parent, false);

        ImageView iconView = notificationView.findViewById(R.id.iv_notification_icon);
        TextView titleView = notificationView.findViewById(R.id.tv_notification_title);
        TextView messageView = notificationView.findViewById(R.id.tv_notification_message);
        TextView timeView = notificationView.findViewById(R.id.tv_notification_time);
        View unreadBadge = notificationView.findViewById(R.id.badge_unread);

        // Set icon with circular colored background
        if (iconView != null) {
            iconView.setImageResource(notification.iconResId);
            
            // Create circular background with color programmatically
            GradientDrawable circularBackground = new GradientDrawable();
            circularBackground.setShape(GradientDrawable.OVAL);
            circularBackground.setColor(ContextCompat.getColor(requireContext(), notification.iconBackgroundColorResId));
            iconView.setBackground(circularBackground);
            
            // Icon is white on colored background
            iconView.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.primary_white),
                android.graphics.PorterDuff.Mode.SRC_IN
            );
        }

        // Set unread badge
        if (unreadBadge != null) {
            unreadBadge.setVisibility(notification.isUnread ? View.VISIBLE : View.GONE);
        }

        // Set title
        if (titleView != null) {
            titleView.setText(notification.title);
        }

        // Set message
        if (messageView != null) {
            messageView.setText(notification.message);
        }

        // Set time
        if (timeView != null) {
            timeView.setText(notification.time);
        }

        // Make the whole card clickable
        View card = notificationView.findViewById(R.id.card_notification);
        if (card != null) {
            card.setOnClickListener(v -> handleNotificationClick(notification));
        }

        return notificationView;
    }

    private void handleNotificationClick(NotificationData notification) {
        switch (notification.type) {
            case EXPENSE_REMINDER:
                navigateToAddTransaction();
                break;
            case BUDGET_OVERSPENDING:
            case BUDGET_WARNING:
            case BUDGET_LIMIT:
                // Navigate to budget fragment
                navigateToBudget();
                break;
            case DAILY_SUMMARY:
            case WEEKLY_SUMMARY:
                // Navigate to dashboard
                navigateToDashboard();
                break;
            default:
                // Default action - mark as read or show details
                break;
        }
    }

    private void navigateToAddTransaction() {
        TransactionFragmentCreate transactionFragmentCreate = new TransactionFragmentCreate();
        transactionFragmentCreate.show(requireActivity().getSupportFragmentManager(), TransactionFragmentCreate.TAG);
    }

    private void navigateToBudget() {
        requireActivity().onBackPressed(); // Go back first
        // TODO: If needed, navigate to specific budget tab
        // This could be enhanced to navigate directly to budget screen
    }

    private void navigateToDashboard() {
        requireActivity().onBackPressed(); // Go back first
        // TODO: Navigate to dashboard if needed
    }

    private String formatCurrency(double amount) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
        formatter.setGroupingUsed(true);
        return formatter.format(amount) + " VND";
    }
}
