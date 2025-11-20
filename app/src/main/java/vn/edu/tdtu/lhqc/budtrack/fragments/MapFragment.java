package vn.edu.tdtu.lhqc.budtrack.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.activities.MainActivity;

// Fragment to display expense locations on an OpenStreetMap view.
public class MapFragment extends Fragment {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2001;

    private MapView mapView;
    private MyLocationNewOverlay myLocationOverlay;
    private List<ExpenseLocation> expenseLocations;

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(requireContext(),
                PreferenceManager.getDefaultSharedPreferences(requireContext()));
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        // Sample data - replace with real expenses later
        expenseLocations = new ArrayList<>();
        expenseLocations.add(new ExpenseLocation(10.762622, 106.660172, "Restaurant", 150000));
        expenseLocations.add(new ExpenseLocation(10.7769, 106.7009, "Shopping Mall", 500000));
        expenseLocations.add(new ExpenseLocation(10.8231, 106.6297, "Gas Station", 200000));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_map, container, false);

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setBottomBarVisible(false);
        }

        setupHeader(root);
        setupMap(root);
        requestLocationPermission();

        return root;
    }

    private void setupHeader(View root) {
        ImageButton backButton = root.findViewById(R.id.btn_back_map);
        EditText searchField = root.findViewById(R.id.et_map_search);
        ImageButton searchButton = root.findViewById(R.id.btn_map_search);

        backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        searchButton.setOnClickListener(v ->
                Toast.makeText(requireContext(), R.string.feature_coming_soon, Toast.LENGTH_SHORT).show());
    }

    private void setupMap(View root) {
        mapView = root.findViewById(R.id.osm_map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        GeoPoint defaultPoint = new GeoPoint(10.762622, 106.660172);
        mapView.getController().setZoom(12.0);
        mapView.getController().setCenter(defaultPoint);

        addExpenseMarkers();
    }

    private void addExpenseMarkers() {
        if (mapView == null) return;

        for (ExpenseLocation expense : expenseLocations) {
            Marker marker = new Marker(mapView);
            marker.setPosition(new GeoPoint(expense.latitude, expense.longitude));
            marker.setTitle(expense.name);
            marker.setSnippet(formatCurrency(expense.amount));
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mapView.getOverlays().add(marker);
        }
        mapView.invalidate();
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            enableUserLocationOverlay();
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void enableUserLocationOverlay() {
        if (mapView == null) return;

        if (myLocationOverlay == null) {
            GpsMyLocationProvider provider = new GpsMyLocationProvider(requireContext());
            provider.addLocationSource(android.location.LocationManager.NETWORK_PROVIDER);
            myLocationOverlay = new MyLocationNewOverlay(provider, mapView);
            myLocationOverlay.enableMyLocation();
            myLocationOverlay.enableFollowLocation();
            mapView.getOverlays().add(myLocationOverlay);
            myLocationOverlay.runOnFirstFix(() -> {
                GeoPoint userPoint = myLocationOverlay.getMyLocation();
                if (userPoint != null) {
                    mapView.post(() -> mapView.getController().animateTo(userPoint));
                }
            });
        } else {
            myLocationOverlay.enableMyLocation();
            myLocationOverlay.enableFollowLocation();
        }
        mapView.invalidate();
    }

    private String formatCurrency(double amount) {
        java.text.NumberFormat formatter = java.text.NumberFormat.getNumberInstance(Locale.getDefault());
        formatter.setGroupingUsed(true);
        return formatter.format(amount) + " VND";
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
        if (myLocationOverlay != null) {
            myLocationOverlay.disableMyLocation();
            myLocationOverlay.disableFollowLocation();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mapView != null) {
            mapView.onDetach();
            mapView = null;
        }
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setBottomBarVisible(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableUserLocationOverlay();
            } else {
                Toast.makeText(requireContext(),
                        getString(R.string.location_permission_denied), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static class ExpenseLocation {
        final double latitude;
        final double longitude;
        final String name;
        final double amount;

        ExpenseLocation(double latitude, double longitude, String name, double amount) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.name = name;
            this.amount = amount;
        }
    }
}

