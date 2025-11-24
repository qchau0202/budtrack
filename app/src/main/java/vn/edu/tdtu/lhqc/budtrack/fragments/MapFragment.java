package vn.edu.tdtu.lhqc.budtrack.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.button.MaterialButton;

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
    private View expenseDetailsPanel;
    private TextView tvExpenseName;
    private TextView tvExpenseAmount;
    private TextView tvExpenseAddress;
    private TextView tvExpenseCategory;
    private TextView tvExpenseDate;
    private TextView tvExpenseNote;
    private MaterialButton btnViewDetails;
    private ImageButton btnCloseDetails;
    private EditText etMapSearch;

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(requireContext(),
                PreferenceManager.getDefaultSharedPreferences(requireContext()));
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        // Sample data - replace with real expenses later (Ho Chi Minh City locations)
        expenseLocations = new ArrayList<>();
        expenseLocations.add(new ExpenseLocation(10.762622, 106.660172, "Nhà Hàng Ngon", 150000, 
                "Food", "160 Pasteur, Bến Nghé, Quận 1, Hồ Chí Minh", "01 Jan 2025", "Lunch with team"));
        expenseLocations.add(new ExpenseLocation(10.7769, 106.7009, "Vincom Center", 500000,
                "Shopping", "72 Lê Thánh Tôn, Bến Nghé, Quận 1, Hồ Chí Minh", "02 Jan 2025", "Monthly groceries"));
        expenseLocations.add(new ExpenseLocation(10.8231, 106.6297, "Trạm Xăng Petrolimex", 200000,
                "Transport", "123 Nguyễn Huệ, Bến Nghé, Quận 1, Hồ Chí Minh", "03 Jan 2025", "Fuel refill"));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_map, container, false);

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setBottomBarVisible(false);
        }

        setupHeader(root);
        setupExpenseDetailsPanel(root);
        setupMap(root);
        requestLocationPermission();

        return root;
    }

    private void setupHeader(View root) {
        ImageButton backButton = root.findViewById(R.id.btn_back_map);
        ImageButton searchButton = root.findViewById(R.id.btn_map_search);
        etMapSearch = root.findViewById(R.id.et_map_search);

        backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        searchButton.setOnClickListener(v -> {
            String query = etMapSearch.getText().toString().trim();
            if (!query.isEmpty()) {
                performSearch(query);
            }
            hideKeyboardAndClearFocus();
        });

        // Handle search action from keyboard
        etMapSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = etMapSearch.getText().toString().trim();
                if (!query.isEmpty()) {
                    performSearch(query);
                }
                hideKeyboardAndClearFocus();
                return true;
            }
            return false;
        });
    }

    private void performSearch(String query) {
        // TODO: Implement search functionality
        Toast.makeText(requireContext(), "Searching for: " + query, Toast.LENGTH_SHORT).show();
    }

    private void setupExpenseDetailsPanel(View root) {
        expenseDetailsPanel = root.findViewById(R.id.expense_details_panel);
        tvExpenseName = root.findViewById(R.id.tv_expense_name);
        tvExpenseAmount = root.findViewById(R.id.tv_expense_amount);
        tvExpenseAddress = root.findViewById(R.id.tv_expense_address);
        tvExpenseCategory = root.findViewById(R.id.tv_expense_category);
        tvExpenseDate = root.findViewById(R.id.tv_expense_date);
        tvExpenseNote = root.findViewById(R.id.tv_expense_note);
        btnViewDetails = root.findViewById(R.id.btn_view_details);
        btnCloseDetails = root.findViewById(R.id.btn_close_details);

        btnCloseDetails.setOnClickListener(v -> hideExpenseDetails());
        btnViewDetails.setOnClickListener(v -> {
            Toast.makeText(requireContext(), R.string.feature_coming_soon, Toast.LENGTH_SHORT).show();
        });
    }

    private void showExpenseDetails(ExpenseLocation expense) {
        tvExpenseName.setText(expense.name);
        tvExpenseAmount.setText(formatCurrency(expense.amount));
        tvExpenseAddress.setText(expense.address);
        tvExpenseCategory.setText(expense.category);
        tvExpenseDate.setText(expense.date);
        
        // Set category chip background color based on category
        int categoryBgResId = getCategoryBackgroundResId(expense.category);
        tvExpenseCategory.setBackgroundResource(categoryBgResId);
        
        // Show note or hide if empty
        if (expense.note != null && !expense.note.isEmpty()) {
            tvExpenseNote.setText(expense.note);
            tvExpenseNote.setVisibility(View.VISIBLE);
        } else {
            tvExpenseNote.setVisibility(View.GONE);
        }
        
        expenseDetailsPanel.setVisibility(View.VISIBLE);
    }

    private int getCategoryBackgroundResId(String category) {
        if (category == null) {
            return R.drawable.bg_category_chip;
        }
        
        switch (category.toLowerCase()) {
            case "food":
                return R.drawable.bg_category_tab_food; // Yellow background
            case "shopping":
                return R.drawable.bg_category_tab_shopping; // Red background
            case "transport":
                return R.drawable.bg_category_tab_transport; // Green background
            case "home":
                return R.drawable.bg_category_tab; // Default
            default:
                return R.drawable.bg_category_chip;
        }
    }

    private void hideExpenseDetails() {
        expenseDetailsPanel.setVisibility(View.GONE);
    }

    private void setupMap(View root) {
        mapView = root.findViewById(R.id.osm_map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        
        mapView.setMultiTouchControls(true);
        
        // Hide keyboard when map is tapped
        mapView.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                hideKeyboardAndClearFocus();
            }
            return false; // Let the map handle the touch event
        });
        
        GeoPoint defaultPoint = new GeoPoint(10.762622, 106.660172);
        mapView.getController().setZoom(12.0);
        mapView.getController().setCenter(defaultPoint);

        addExpenseMarkers();
    }
    
    private void hideKeyboardAndClearFocus() {
        etMapSearch.clearFocus();
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(etMapSearch.getWindowToken(), 0);
        }
    }

    private void addExpenseMarkers() {
        for (ExpenseLocation expense : expenseLocations) {
            Marker marker = new Marker(mapView);
            marker.setPosition(new GeoPoint(expense.latitude, expense.longitude));
            marker.setTitle(expense.name);
            marker.setSnippet(formatCurrency(expense.amount));
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            
            // Set custom icon - create a new instance for each marker and tint it with darker green
            Drawable markerIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_location_on_24dp);
            if (markerIcon != null) {
                markerIcon = markerIcon.mutate(); // Create a mutable copy
                markerIcon.setTint(ContextCompat.getColor(requireContext(), R.color.secondary_green));
                
                // Scale up the marker icon for better visibility (2.5x size)
                int scaledWidth = (int) (markerIcon.getIntrinsicWidth() * 1.5f);
                int scaledHeight = (int) (markerIcon.getIntrinsicHeight() * 1.5f);
                
                // Convert to bitmap with larger size
                Bitmap markerBitmap = drawableToBitmap(markerIcon, scaledWidth, scaledHeight);
                if (markerBitmap != null) {
                    marker.setIcon(new android.graphics.drawable.BitmapDrawable(getResources(), markerBitmap));
                }
            }
            
            // Store expense data in marker
            marker.setRelatedObject(expense);
            
            // Add click listener
            marker.setOnMarkerClickListener((marker1, mapView) -> {
                ExpenseLocation clickedExpense = (ExpenseLocation) marker1.getRelatedObject();
                if (clickedExpense != null) {
                    // Hide keyboard and clear focus when marker is clicked
                    hideKeyboardAndClearFocus();
                    showExpenseDetails(clickedExpense);
                    // Center map on marker
                    mapView.getController().animateTo(marker1.getPosition());
                }
                return true;
            });
            
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
            
            // Set custom person icon for current location
            Drawable personIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_person_24dp);
            if (personIcon != null) {
                // Tint the icon with red to differentiate from green expense markers
                personIcon = personIcon.mutate();
                personIcon.setTint(ContextCompat.getColor(requireContext(), R.color.primary_red));
                
                // Scale up the person icon for better visibility (3x size for current location)
                int personSize = (int) (54 * getResources().getDisplayMetrics().density); // 72dp equivalent
                Bitmap personBitmap = drawableToBitmap(personIcon, personSize, personSize);
                if (personBitmap != null) {
                    myLocationOverlay.setPersonIcon(personBitmap);
                }
            }
            
            myLocationOverlay.enableMyLocation();
            myLocationOverlay.enableFollowLocation();
            mapView.getOverlays().add(myLocationOverlay);
            myLocationOverlay.runOnFirstFix(() -> {
                GeoPoint userPoint = myLocationOverlay.getMyLocation();
                if (userPoint != null) {
                    mapView.post(() -> mapView.getController().animateTo(userPoint));
                }
            });
        }
        mapView.invalidate();
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        return drawableToBitmap(drawable, 0, 0);
    }

    private Bitmap drawableToBitmap(Drawable drawable, int width, int height) {
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                Bitmap originalBitmap = bitmapDrawable.getBitmap();
                // If custom size specified, scale the bitmap
                if (width > 0 && height > 0) {
                    return Bitmap.createScaledBitmap(originalBitmap, width, height, true);
                }
                return originalBitmap;
            }
        }

        // Use custom size if provided, otherwise use intrinsic size
        if (width <= 0 || height <= 0) {
            width = drawable.getIntrinsicWidth();
            height = drawable.getIntrinsicHeight();
            if (width <= 0 || height <= 0) {
                width = 48; // Default size
                height = 48;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);

        return bitmap;
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
        final String category;
        final String address;
        final String date;
        final String note;

        ExpenseLocation(double latitude, double longitude, String name, double amount, 
                       String category, String address, String date, String note) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.name = name;
            this.amount = amount;
            this.category = category;
            this.address = address;
            this.date = date;
            this.note = note;
        }
    }
}

