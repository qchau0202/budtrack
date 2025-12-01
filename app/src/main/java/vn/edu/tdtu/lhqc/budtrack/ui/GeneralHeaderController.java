package vn.edu.tdtu.lhqc.budtrack.ui;

import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.fragments.MapFragment;
import vn.edu.tdtu.lhqc.budtrack.fragments.NotificationFragment;
import vn.edu.tdtu.lhqc.budtrack.fragments.SearchFragment;

/**
 * Simple helper that wires up the shared header view with default behaviours.
 * Screens can optionally override any of the button actions by providing callbacks.
 */
public final class GeneralHeaderController {

    private GeneralHeaderController() {
        // Utility class
    }

    public static void setup(View root, Fragment fragment) {
        setup(root, fragment, HeaderCallbacks.EMPTY);
    }

    public static void setup(View root, Fragment fragment, HeaderCallbacks callbacks) {
        if (root == null || fragment == null) {
            return;
        }

        View header = root.findViewById(R.id.header_container);
        if (header == null) {
            header = root;
        }

        HeaderCallbacks safeCallbacks = callbacks != null ? callbacks : HeaderCallbacks.EMPTY;

        bindButton(header, R.id.btn_search, () -> {
            if (!safeCallbacks.onSearchClick(fragment)) {
                openSearch(fragment);
            }
        });

        bindButton(header, R.id.btn_notifications, () -> {
            if (!safeCallbacks.onNotificationsClick(fragment)) {
                openNotifications(fragment);
            }
        });

        bindButton(header, R.id.btn_map, () -> {
            if (!safeCallbacks.onMapClick(fragment)) {
                openMap(fragment);
            }
        });
    }

    private static void bindButton(View header, int buttonId, Runnable action) {
        View button = header.findViewById(buttonId);
        if (button != null && action != null) {
            button.setOnClickListener(v -> action.run());
        }
    }

    private static void openSearch(Fragment fragment) {
        if (!fragment.isAdded()) {
            return;
        }
        FragmentActivity activity = fragment.getActivity();
        if (activity == null) {
            return;
        }
        activity.getSupportFragmentManager()
                .beginTransaction()
                // Add on top of the current main fragment so that we can
                // cleanly remove it when switching bottom‑nav tabs.
                .add(R.id.fragment_container, SearchFragment.newInstance(), "SEARCH_FRAGMENT")
                .addToBackStack("SEARCH_FRAGMENT")
                .commit();
    }

    private static void openMap(Fragment fragment) {
        if (!fragment.isAdded()) {
            return;
        }
        FragmentActivity activity = fragment.getActivity();
        if (activity == null) {
            return;
        }
        activity.getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, MapFragment.newInstance(), "MAP_FRAGMENT")
                .addToBackStack("MAP_FRAGMENT")
                .commit();
    }

    private static void openNotifications(Fragment fragment) {
        if (!fragment.isAdded()) {
            return;
        }
        FragmentActivity activity = fragment.getActivity();
        if (activity == null) {
            return;
        }
        activity.getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, NotificationFragment.newInstance(), "NOTIFICATION_FRAGMENT")
                .addToBackStack("NOTIFICATION_FRAGMENT")
                .commit();
    }

    public interface HeaderCallbacks {
        HeaderCallbacks EMPTY = new HeaderCallbacks() { };

        default boolean onSearchClick(Fragment fragment) {
            return false;
        }

        default boolean onNotificationsClick(Fragment fragment) {
            return false;
        }

        default boolean onMapClick(Fragment fragment) {
            return false;
        }
    }
}

