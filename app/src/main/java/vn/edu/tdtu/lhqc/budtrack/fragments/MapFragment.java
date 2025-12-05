package vn.edu.tdtu.lhqc.budtrack.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.activities.MainActivity;
import vn.edu.tdtu.lhqc.budtrack.controllers.transaction.TransactionManager;
import vn.edu.tdtu.lhqc.budtrack.models.Transaction;
import vn.edu.tdtu.lhqc.budtrack.utils.CurrencyUtils;

// Fragment to display expense locations on an OpenStreetMap view.
public class MapFragment extends Fragment {

    public static final String RESULT_KEY_LOCATION = "location_selected";
    public static final String RESULT_LOCATION_ADDRESS = "location_address";
    public static final String RESULT_LOCATION_LAT = "location_lat";
    public static final String RESULT_LOCATION_LNG = "location_lng";
    
    private static final String ARG_SELECTION_MODE = "selection_mode";
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
    
    // Location selection mode
    private boolean isSelectionMode = false;
    private Marker selectedLocationMarker = null;
    private View locationSelectionPanel;
    private TextView tvSelectedLocationAddress;
    private MaterialButton btnConfirmLocation;
    private Double selectedLocationLat = null;
    private Double selectedLocationLng = null;
    private String selectedLocationAddress = null;

    public static MapFragment newInstance() {
        return new MapFragment();
    }
    
    public static MapFragment newInstanceForLocationSelection() {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_SELECTION_MODE, true);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(requireContext(),
                PreferenceManager.getDefaultSharedPreferences(requireContext()));
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        // Check if in selection mode
        if (getArguments() != null) {
            isSelectionMode = getArguments().getBoolean(ARG_SELECTION_MODE, false);
        }

        // Only load expense locations if not in selection mode
        expenseLocations = new ArrayList<>();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_map, container, false);

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setBottomBarVisible(false);
        }

        setupHeader(root);
        if (isSelectionMode) {
            setupLocationSelectionPanel(root);
        } else {
            setupExpenseDetailsPanel(root);
        }

        setupMap(root);
        requestLocationPermission();

        return root;
    }

    private void setupHeader(View root) {
        ImageButton backButton = root.findViewById(R.id.btn_back_map);
        ImageButton searchButton = root.findViewById(R.id.btn_map_search);
        ImageButton filterButton = root.findViewById(R.id.btn_map_filter);
        etMapSearch = root.findViewById(R.id.et_map_search);

        backButton.setOnClickListener(v -> handleBackPress());

        // Hide filter button in selection mode, but keep search
        if (isSelectionMode) {
            if (filterButton != null) filterButton.setVisibility(View.GONE);
        }

        // Setup search functionality for both modes
        if (searchButton != null) {
        searchButton.setOnClickListener(v -> {
                String query = etMapSearch != null ? etMapSearch.getText().toString().trim() : "";
            if (!query.isEmpty()) {
                performSearch(query);
            }
            hideKeyboardAndClearFocus();
        });
        }

        // Handle search action from keyboard
        if (etMapSearch != null) {
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
    }

    private void performSearch(String query) {
        if (isSelectionMode) {
            // In selection mode, search for locations using Geocoder
            performLocationSearch(query);
        } else {
            // In normal mode, search for transactions
            performTransactionSearch(query);
        }
    }

    private void performLocationSearch(String query) {
        // Use Geocoder to search for location on a background thread to avoid ANR
        new Thread(() -> {
            try {
                android.location.Geocoder geocoder =
                        new android.location.Geocoder(requireContext(), java.util.Locale.getDefault());
                java.util.List<android.location.Address> addresses =
                        geocoder.getFromLocationName(query, 1);

                if (!isAdded()) return;

                requireActivity().runOnUiThread(() -> {
                    if (!isAdded()) return;

                    if (addresses != null && !addresses.isEmpty()) {
                        android.location.Address address = addresses.get(0);
                        GeoPoint geoPoint = new GeoPoint(address.getLatitude(), address.getLongitude());

                        // Center map on found location
                        mapView.getController().animateTo(geoPoint);
                        mapView.getController().setZoom(15.0);

                        // Automatically select this location
                        selectLocationOnMap(geoPoint);
                    } else {
                        Toast.makeText(requireContext(),
                                "Location not found: " + query,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(),
                                "Error searching location: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    private void performTransactionSearch(String query) {
        if (getContext() == null) return;

        // Search transactions by merchant name, category name, note, or address
        List<Transaction> allTransactions = TransactionManager.getTransactions(getContext());
        List<Transaction> matchingTransactions = new ArrayList<>();

        String queryLower = query.toLowerCase(Locale.getDefault());

        for (Transaction transaction : allTransactions) {
            boolean matches = false;

            // Search in merchant name
            String merchantName = transaction.getMerchantName();
            if (merchantName != null && merchantName.toLowerCase(Locale.getDefault()).contains(queryLower)) {
                matches = true;
            }

            // Search in category name
            if (!matches) {
                String categoryName = transaction.getCategoryName();
                if (categoryName != null && categoryName.toLowerCase(Locale.getDefault()).contains(queryLower)) {
                    matches = true;
                }
            }

            // Search in note
            if (!matches) {
                String note = transaction.getNote();
                if (note != null && note.toLowerCase(Locale.getDefault()).contains(queryLower)) {
                    matches = true;
                }
            }

            // Search in address
            if (!matches) {
                String address = transaction.getAddress();
                if (address != null && address.toLowerCase(Locale.getDefault()).contains(queryLower)) {
                    matches = true;
                }
            }

            if (matches) {
                matchingTransactions.add(transaction);
            }
        }

        // Sort newest to oldest by date (nulls last), fallback by id desc
        matchingTransactions.sort((t1, t2) -> {
            Date d1 = t1.getDate();
            Date d2 = t2.getDate();
            if (d1 == null && d2 == null) return Long.compare(t2.getId(), t1.getId());
            if (d1 == null) return 1;
            if (d2 == null) return -1;
            int cmp = d2.compareTo(d1); // newest first
            if (cmp != 0) return cmp;
            return Long.compare(t2.getId(), t1.getId());
        });

        // Show results in bottom sheet
        if (getActivity() != null) {
            TransactionSearchBottomSheet searchSheet =
                    TransactionSearchBottomSheet.newInstance(query, matchingTransactions);
            searchSheet.show(getActivity().getSupportFragmentManager(), TransactionSearchBottomSheet.TAG);
        }
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

        // This inline panel is no longer used for transaction taps;
        // tapping a marker will show a full bottom sheet instead.
        btnCloseDetails.setOnClickListener(v -> hideExpenseDetails());
        btnViewDetails.setOnClickListener(v -> {
            hideExpenseDetails();
        });

        // Hide by default; it can still be reused in future if needed.
        if (expenseDetailsPanel != null) {
            expenseDetailsPanel.setVisibility(View.GONE);
        }
    }

    private void showExpenseDetails(ExpenseLocation expense) {
        tvExpenseName.setText(expense.name);
        tvExpenseAmount.setText(CurrencyUtils.formatCurrency(requireContext(), expense.amount));
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
            default:
                return R.drawable.bg_category_chip;
        }
    }

    private void hideExpenseDetails() {
        expenseDetailsPanel.setVisibility(View.GONE);
    }
    
    private void setupLocationSelectionPanel(View root) {
        locationSelectionPanel = root.findViewById(R.id.location_selection_panel);
        if (locationSelectionPanel == null) {
            // Panel should exist in layout
            return;
        }
        tvSelectedLocationAddress = root.findViewById(R.id.tv_selected_location_address);
        btnConfirmLocation = root.findViewById(R.id.btn_confirm_location);
        
        if (btnConfirmLocation != null) {
            btnConfirmLocation.setOnClickListener(v -> confirmLocationSelection());
        }
        
        locationSelectionPanel.setVisibility(View.VISIBLE);
    }

    private void setupMap(View root) {
        mapView = root.findViewById(R.id.osm_map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        
        mapView.setMultiTouchControls(true);
        
        if (isSelectionMode) {
            // In selection mode, allow tapping on map to select location
            mapView.getOverlays().add(new RotationGestureOverlay(mapView));
            // Add custom overlay to handle tap events
            mapView.getOverlays().add(new Overlay() {
                @Override
                public boolean onSingleTapConfirmed(MotionEvent e, MapView mapView) {
                    if (isSelectionMode) {
                        GeoPoint geoPoint = (GeoPoint) mapView.getProjection().fromPixels(
                                (int) e.getX(), (int) e.getY());
                        selectLocationOnMap(geoPoint);
                        return true;
                    }
                    return false;
                }
            });
        } else {
            // Hide keyboard when map is tapped
            mapView.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    hideKeyboardAndClearFocus();
                }
                return false; // Let the map handle the touch event
            });
        }
        
        GeoPoint defaultPoint = new GeoPoint(10.762622, 106.660172);
        mapView.getController().setZoom(12.0);
        mapView.getController().setCenter(defaultPoint);

        if (!isSelectionMode) {
            // Load all transactions that have location information and add markers
            loadExpenseLocationsFromTransactions(root.getContext());
            addExpenseMarkers();
        }
    }

    /**
     * Load all transactions that have location data and adapt them into ExpenseLocation items
     * for display on the map.
     */
    private void loadExpenseLocationsFromTransactions(Context context) {
        if (context == null) return;

        List<Transaction> transactions = TransactionManager.getTransactions(context);
        expenseLocations.clear();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());

        for (Transaction transaction : transactions) {
            if (transaction == null || !transaction.hasLocation()) {
                continue;
            }

            Double lat = transaction.getLatitude();
            Double lng = transaction.getLongitude();
            if (lat == null || lng == null) {
                continue;
            }

            String name = transaction.getMerchantName();
            if (name == null || name.trim().isEmpty()) {
                // Fallback to category name or a generic label
                name = transaction.getCategoryName();
                if (name == null || name.trim().isEmpty()) {
                    name = getString(R.string.unknown);
                }
            }

            String category = transaction.getCategoryName();
            if (category == null || category.trim().isEmpty()) {
                category = transaction.getType() != null ? transaction.getType().name() : "";
            }

            String address = transaction.getAddress();
            if (address == null || address.trim().isEmpty()) {
                address = String.format(Locale.getDefault(), "%.6f, %.6f", lat, lng);
            }

            String dateText = "";
            Date date = transaction.getDate();
            if (date != null) {
                dateText = dateFormat.format(date);
            }

            String note = transaction.getNote();

            expenseLocations.add(new ExpenseLocation(
                    transaction.getId(),
                    lat,
                    lng,
                    name,
                    transaction.getAmount(),
                    category,
                    address,
                    dateText,
                    note
            ));
        }
    }

    private void selectLocationOnMap(GeoPoint point) {
        // Remove previous selection marker
        if (selectedLocationMarker != null) {
            mapView.getOverlays().remove(selectedLocationMarker);
        }
        
        // Create new marker for selected location
        selectedLocationMarker = new Marker(mapView);
        selectedLocationMarker.setPosition(point);
        selectedLocationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        
        // Set custom icon with primary green color
        Drawable markerIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_location_on_24dp);
        if (markerIcon != null) {
            markerIcon = markerIcon.mutate();
            markerIcon.setTint(ContextCompat.getColor(requireContext(), R.color.primary_green));
            
            int scaledWidth = (int) (markerIcon.getIntrinsicWidth() * 2.0f);
            int scaledHeight = (int) (markerIcon.getIntrinsicHeight() * 2.0f);
            
            Bitmap markerBitmap = drawableToBitmap(markerIcon, scaledWidth, scaledHeight);
            if (markerBitmap != null) {
                selectedLocationMarker.setIcon(new android.graphics.drawable.BitmapDrawable(getResources(), markerBitmap));
            }
        }
        
        mapView.getOverlays().add(selectedLocationMarker);
        mapView.invalidate();
        
        // Get address from coordinates (reverse geocoding)
        getAddressFromCoordinates(point.getLatitude(), point.getLongitude());
    }
    
    private void getAddressFromCoordinates(double lat, double lng) {
        // Always have a fallback address as coordinates
        String fallbackAddress = String.format(Locale.getDefault(), "%.6f, %.6f", lat, lng);

        new Thread(() -> {
            String resolvedAddress = fallbackAddress;

            // Try to use Android Geocoder if available (background thread to avoid ANR)
            try {
                android.location.Geocoder geocoder =
                        new android.location.Geocoder(requireContext(), java.util.Locale.getDefault());
                java.util.List<android.location.Address> addresses =
                        geocoder.getFromLocation(lat, lng, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    android.location.Address addressObj = addresses.get(0);
                    StringBuilder addressBuilder = new StringBuilder();
                    for (int i = 0; i <= addressObj.getMaxAddressLineIndex(); i++) {
                        if (i > 0) addressBuilder.append(", ");
                        addressBuilder.append(addressObj.getAddressLine(i));
                    }
                    resolvedAddress = addressBuilder.toString();
                }
            } catch (Exception e) {
                // Ignore geocoder errors, fall back to coordinates
            }

            final String finalAddress = resolvedAddress;

            if (!isAdded()) return;

            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) return;

                // Update UI
                if (tvSelectedLocationAddress != null) {
                    tvSelectedLocationAddress.setText(finalAddress);
                }

                // Store selected location
                selectedLocationLat = lat;
                selectedLocationLng = lng;
                selectedLocationAddress = finalAddress;
            });
        }).start();
    }
    
    private void confirmLocationSelection() {
        if (selectedLocationMarker == null || selectedLocationLat == null || selectedLocationLng == null) {
            if (getContext() != null) {
                Toast.makeText(getContext(), getString(R.string.please_select_location), Toast.LENGTH_SHORT).show();
            }
            return;
        }
        
        android.content.Context context = getContext();
        if (context == null || getActivity() == null) {
            return;
        }
        
        // Store location in SharedPreferences temporarily so it can be retrieved
        // even if the calling fragment (bottom sheet) was dismissed
        android.content.SharedPreferences prefs = context.getSharedPreferences("location_selection", android.content.Context.MODE_PRIVATE);
        prefs.edit()
                .putString("address", selectedLocationAddress != null ? selectedLocationAddress : "")
                .putFloat("lat", selectedLocationLat.floatValue())
                .putFloat("lng", selectedLocationLng.floatValue())
                .putBoolean("has_location", true)
                .apply();
        
        // Also send via Fragment Result for immediate listeners
        Bundle result = new Bundle();
        result.putString(RESULT_LOCATION_ADDRESS, selectedLocationAddress != null ? selectedLocationAddress : "");
        result.putDouble(RESULT_LOCATION_LAT, selectedLocationLat);
        result.putDouble(RESULT_LOCATION_LNG, selectedLocationLng);
        getActivity().getSupportFragmentManager().setFragmentResult(RESULT_KEY_LOCATION, result);
        
        // Check if we should re-open transaction create fragment
        android.content.SharedPreferences statePrefs = context.getSharedPreferences("transaction_state", android.content.Context.MODE_PRIVATE);
        boolean shouldReopen = statePrefs.getBoolean("should_reopen", false);
        
        if (shouldReopen) {
            // Don't clear the flag here - let TransactionCreateFragment handle it after restoring state
            // The state will be cleared when transaction is saved or fragment is dismissed without saving
            
            // Re-open transaction create fragment - it will restore state from SharedPreferences
            TransactionCreateFragment transactionFragment = new TransactionCreateFragment();
            // State is already saved in SharedPreferences, fragment will restore it
            transactionFragment.show(getActivity().getSupportFragmentManager(), TransactionCreateFragment.TAG);
        }
        
        // Navigate back
        getActivity().onBackPressed();
    }
    
    private void handleBackPress() {
        // If in selection mode, check if we should re-open transaction fragment
        if (isSelectionMode) {
            android.content.Context context = getContext();
            if (context != null && getActivity() != null) {
                android.content.SharedPreferences statePrefs = context.getSharedPreferences("transaction_state", android.content.Context.MODE_PRIVATE);
                boolean shouldReopen = statePrefs.getBoolean("should_reopen", false);
                
                if (shouldReopen) {
                    // Don't clear the flag here - let TransactionCreateFragment handle it after restoring state
                    
                    // Manually pop this fragment from back stack
                    if (getActivity().getSupportFragmentManager().getBackStackEntryCount() > 0) {
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                    
                    // Re-open transaction create fragment - it will restore state from SharedPreferences
                    TransactionCreateFragment transactionFragment = new TransactionCreateFragment();
                    // State is already saved in SharedPreferences, fragment will restore it
                    transactionFragment.show(getActivity().getSupportFragmentManager(), TransactionCreateFragment.TAG);
                    return; // Don't call onBackPressed
                }
            }
        }
        
        // Navigate back normally if not in selection mode or shouldn't reopen
        if (getActivity() != null) {
            getActivity().onBackPressed();
        }
    }
    
    private void hideKeyboardAndClearFocus() {
        if (etMapSearch != null) {
        etMapSearch.clearFocus();
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(etMapSearch.getWindowToken(), 0);
            }
        }
    }

    private void addExpenseMarkers() {
        for (ExpenseLocation expense : expenseLocations) {
            Marker marker = new Marker(mapView);
            marker.setPosition(new GeoPoint(expense.latitude, expense.longitude));
            marker.setTitle(expense.name);
            marker.setSnippet(CurrencyUtils.formatCurrency(requireContext(), expense.amount));
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
                if (clickedExpense != null && getActivity() != null) {
                    // Hide keyboard and clear focus when marker is clicked
                    hideKeyboardAndClearFocus();

                    // Show full transaction details in a bottom sheet
                    TransactionDetailBottomSheet sheet =
                            TransactionDetailBottomSheet.newInstance(clickedExpense.transactionId);
                    sheet.show(getActivity().getSupportFragmentManager(), TransactionDetailBottomSheet.TAG);

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
            Drawable personIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_nearby_24dp);
            if (personIcon != null) {
                // Tint the icon with red to differentiate from green expense markers
                personIcon = personIcon.mutate();
                personIcon.setTint(ContextCompat.getColor(requireContext(), R.color.primary_black));
                
                // Scale up the person icon for better visibility (3x size for current location)
                int personSize = (int) (48 * getResources().getDisplayMetrics().density); // 72dp equivalent
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
        final long transactionId;
        final double latitude;
        final double longitude;
        final String name;
        final double amount;
        final String category;
        final String address;
        final String date;
        final String note;

        ExpenseLocation(long transactionId,
                        double latitude,
                        double longitude,
                        String name,
                        double amount,
                        String category,
                        String address,
                        String date,
                        String note) {
            this.transactionId = transactionId;
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

