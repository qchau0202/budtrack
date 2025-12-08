package vn.edu.tdtu.lhqc.budtrack.activities;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.content.Intent;
import java.io.IOException;
import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.controllers.auth.AuthController;
import vn.edu.tdtu.lhqc.budtrack.controllers.settings.SettingsHandler;
import vn.edu.tdtu.lhqc.budtrack.fragments.DashboardFragment;
import vn.edu.tdtu.lhqc.budtrack.fragments.HomeFragment;
import vn.edu.tdtu.lhqc.budtrack.fragments.ProfileFragment;
import vn.edu.tdtu.lhqc.budtrack.fragments.BudgetFragment;
import vn.edu.tdtu.lhqc.budtrack.fragments.TransactionCreateFragment;
import vn.edu.tdtu.lhqc.budtrack.utils.LanguageManager;
import vn.edu.tdtu.lhqc.budtrack.utils.ThemeManager;

public class MainActivity extends AppCompatActivity {

	private View navHome;
	private View navBudget;
	private View navDashboard;
	private View navProfile;
	private String currentFragmentTag;
	private View bottomBarContainer;

	// Cache fragments to avoid re-creating on each tab switch
	private Fragment homeFragment;
	private Fragment budgetFragment;
	private Fragment dashboardFragment;
	private Fragment profileFragment;
	private Fragment activeFragment;

	// --- OCR LAUNCHERS AND RECOGNIZER ---
	private ActivityResultLauncher<Void> takePictureLauncher;
	private ActivityResultLauncher<String> pickImageLauncher;
	private ActivityResultLauncher<String> requestPermissionLauncher;
	private TextRecognizer recognizer;

	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(LanguageManager.wrapContext(newBase));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LanguageManager.applySavedLanguage(this);
		ThemeManager.applySavedTheme(this);
		EdgeToEdge.enable(this);

		// Check if user is logged in, redirect to LoginActivity if not
		if (!AuthController.isLoggedIn(this)) {
			Intent intent = new Intent(this, LoginActivity.class);
			startActivity(intent);
			finish();
			return;
		}

		// Initialize weekly exchange rate update schedule if not already scheduled
		if (SettingsHandler.getNextUpdateTime(this) == 0) {
			SettingsHandler.scheduleWeeklyExchangeRateUpdate(this);
		}
		// If no exchange rate has ever been fetched, fetch once now (background)
		if (SettingsHandler.getExchangeRateLastUpdate(this) == 0) {
			Intent serviceIntent = vn.edu.tdtu.lhqc.budtrack.services.ExchangeRateUpdateService
					.createUpdateIntent(getApplicationContext());
			startService(serviceIntent);
		}

		setContentView(R.layout.activity_main);
		bottomBarContainer = findViewById(R.id.bottom_bar_container);

		// Handle window insets properly for EdgeToEdge
		View mainLayout = findViewById(R.id.main);
		ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, insets) -> {
			Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
			// Apply padding only to top for status bar
			v.setPadding(0, systemBars.top, 0, 0);
			return insets;
		});

		// --- OCR SETUP ---
		setupOcrLaunchers();

		// Initialize navigation views
		navHome = findViewById(R.id.nav_home);
		navBudget = findViewById(R.id.nav_budget);
		navDashboard = findViewById(R.id.nav_dashboard);
		navProfile = findViewById(R.id.nav_profile);
		FloatingActionButton fabAdd = findViewById(R.id.fab_add);

		// Set Home as active by default and load HomeFragment
		FragmentManager fm = getSupportFragmentManager();
		Fragment existingHome = fm.findFragmentByTag("HOME_FRAGMENT");

		if (savedInstanceState == null && existingHome == null) {
			// First time creation - create new fragments
			homeFragment = new HomeFragment();
			budgetFragment = new BudgetFragment();
			dashboardFragment = new DashboardFragment();
			profileFragment = new ProfileFragment();

			FragmentTransaction ft = fm.beginTransaction();
			ft.setReorderingAllowed(true);
			ft.add(R.id.fragment_container, homeFragment, "HOME_FRAGMENT");
			ft.add(R.id.fragment_container, budgetFragment, "BUDGET_FRAGMENT").hide(budgetFragment);
			ft.add(R.id.fragment_container, dashboardFragment, "DASHBOARD_FRAGMENT").hide(dashboardFragment);
			ft.add(R.id.fragment_container, profileFragment, "PROFILE_FRAGMENT").hide(profileFragment);
			ft.commit();

			activeFragment = homeFragment;
			currentFragmentTag = "HOME_FRAGMENT";
			highlightNavigation(currentFragmentTag);
		} else {
			// Restore fragments from saved state
			homeFragment = fm.findFragmentByTag("HOME_FRAGMENT");
			budgetFragment = fm.findFragmentByTag("BUDGET_FRAGMENT");
			dashboardFragment = fm.findFragmentByTag("DASHBOARD_FRAGMENT");
			profileFragment = fm.findFragmentByTag("PROFILE_FRAGMENT");

			// Determine which fragment is currently visible
			if (homeFragment != null && !homeFragment.isHidden()) {
				activeFragment = homeFragment;
				currentFragmentTag = "HOME_FRAGMENT";
			} else if (budgetFragment != null && !budgetFragment.isHidden()) {
				activeFragment = budgetFragment;
				currentFragmentTag = "BUDGET_FRAGMENT";
			} else if (dashboardFragment != null && !dashboardFragment.isHidden()) {
				activeFragment = dashboardFragment;
				currentFragmentTag = "DASHBOARD_FRAGMENT";
			} else if (profileFragment != null && !profileFragment.isHidden()) {
				activeFragment = profileFragment;
				currentFragmentTag = "PROFILE_FRAGMENT";
			} else {
				// Fallback to home fragment
				activeFragment = homeFragment;
				currentFragmentTag = "HOME_FRAGMENT";
			}
			highlightNavigation(currentFragmentTag);
		}

		// Set up click listeners
		navHome.setOnClickListener(v -> setNavSelected(R.id.nav_home));
		navBudget.setOnClickListener(v -> setNavSelected(R.id.nav_budget));
		navDashboard.setOnClickListener(v -> setNavSelected(R.id.nav_dashboard));
		navProfile.setOnClickListener(v -> setNavSelected(R.id.nav_profile));
		fabAdd.setOnClickListener(v -> showInputMethodSelectionBottomSheet());
	}

	private void setupOcrLaunchers() {
		recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

		requestPermissionLauncher = registerForActivityResult(
				new ActivityResultContracts.RequestPermission(),
				isGranted -> {
					if (isGranted) {
						takePictureLauncher.launch(null);
					} else {
						Toast.makeText(this, "Camera permission is required to scan receipts", Toast.LENGTH_LONG).show();
					}
				}
		);

		takePictureLauncher = registerForActivityResult(
				new ActivityResultContracts.TakePicturePreview(),
				bitmap -> {
					if (bitmap != null) {
						processImageForText(bitmap);
					} else {
						Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show();
					}
				}
		);

		pickImageLauncher = registerForActivityResult(
				new ActivityResultContracts.GetContent(),
				uri -> {
					if (uri != null) {
						try {
							Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
							processImageForText(bitmap);
						} catch (IOException e) {
							Toast.makeText(this, "Failed to load image from gallery", Toast.LENGTH_SHORT).show();
							e.printStackTrace();
						}
					}
				}
		);
	}

	private void processImageForText(Bitmap bitmap) {
		Toast.makeText(this, "Scanning receipt...", Toast.LENGTH_SHORT).show();
		InputImage image = InputImage.fromBitmap(bitmap, 0);
		recognizer.process(image)
				.addOnSuccessListener(visionText -> {
					String fullText = visionText.getText();
					if (fullText.isEmpty()) {
						Toast.makeText(this, "No text found on receipt. Please try manual input.", Toast.LENGTH_LONG).show();
						// Fallback to manual input
						openTransactionCreate(false, null);
					} else {
						// Trực tiếp mở Fragment và truyền văn bản để Fragment tự hiển thị dialog xác nhận
						openTransactionCreate(true, fullText);
					}
				})
				.addOnFailureListener(e -> {
					Toast.makeText(this, "Text recognition failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
					// Fallback to manual input on failure
					openTransactionCreate(false, null);
				});
	}

	private void setNavSelected(int navId) {
		FragmentManager fm = getSupportFragmentManager();

		// Determine target fragment
		Fragment target = null;
		String fragmentTag = null;
		if (navId == R.id.nav_home) {
			fragmentTag = "HOME_FRAGMENT";
			target = homeFragment != null ? homeFragment : fm.findFragmentByTag("HOME_FRAGMENT");
		} else if (navId == R.id.nav_budget) {
			fragmentTag = "BUDGET_FRAGMENT";
			target = budgetFragment != null ? budgetFragment : fm.findFragmentByTag("BUDGET_FRAGMENT");
		} else if (navId == R.id.nav_dashboard) {
			fragmentTag = "DASHBOARD_FRAGMENT";
			target = dashboardFragment != null ? dashboardFragment : fm.findFragmentByTag("DASHBOARD_FRAGMENT");
		} else if (navId == R.id.nav_profile) {
			fragmentTag = "PROFILE_FRAGMENT";
			target = profileFragment != null ? profileFragment : fm.findFragmentByTag("PROFILE_FRAGMENT");
		}

		// If target is null or already active and visible, do nothing
		if (target == null) {
			return;
		}

		// Check if target is already the active fragment and not hidden
		if (target == activeFragment && target.isAdded() && !target.isHidden()) {
			// Already showing this fragment, just update navigation highlight
			highlightNavigation(fragmentTag);
            currentFragmentTag = fragmentTag;
            return;
		}

		highlightNavigation(fragmentTag);
        currentFragmentTag = fragmentTag;

        FragmentTransaction ft = fm.beginTransaction();
		ft.setReorderingAllowed(true);

		// Remove any overlay fragments (Search, Notification, Map, etc.)
		for (Fragment fragment : fm.getFragments()) {
			if (fragment == null) continue;

			boolean isMainFragment = fragment == homeFragment
					|| fragment == budgetFragment
					|| fragment == dashboardFragment
					|| fragment == profileFragment;

			if (!isMainFragment && fragment.isAdded()) {
				ft.remove(fragment);
			}
		}

		// Hide all main fragments except the target
		if (homeFragment != null && homeFragment != target && !homeFragment.isHidden()) {
			ft.hide(homeFragment);
		}
		if (budgetFragment != null && budgetFragment != target && !budgetFragment.isHidden()) {
			ft.hide(budgetFragment);
		}
		if (dashboardFragment != null && dashboardFragment != target && !dashboardFragment.isHidden()) {
			ft.hide(dashboardFragment);
		}
		if (profileFragment != null && profileFragment != target && !profileFragment.isHidden()) {
			ft.hide(profileFragment);
		}

		// Show the target fragment if it's hidden
		if (target.isHidden() || !target.isAdded()) {
			ft.show(target);
		}

		ft.commit();
		activeFragment = target;

		// Clear any back stack entries related to overlays to avoid them
		// being resurrected when the user presses the system back button.
		if (fm.getBackStackEntryCount() > 0) {
			fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
		}
	}


	private void highlightNavigation(String fragmentTag) {
		navHome.setSelected("HOME_FRAGMENT".equals(fragmentTag));
		navBudget.setSelected("BUDGET_FRAGMENT".equals(fragmentTag));
		navDashboard.setSelected("DASHBOARD_FRAGMENT".equals(fragmentTag));
		navProfile.setSelected("PROFILE_FRAGMENT".equals(fragmentTag));
	}

	public void setBottomBarVisible(boolean visible) {
		if (bottomBarContainer != null) {
			bottomBarContainer.setVisibility(visible ? View.VISIBLE : View.GONE);
		}
	}

	private void showInputMethodSelectionBottomSheet() {
		BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
		View view = LayoutInflater.from(this).inflate(R.layout.view_bottom_sheet_input_method, null);
		dialog.setContentView(view);

		// Configure bottom sheet to expand fully and disable dragging to prevent accidental dismissal
		View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
		if (bottomSheet != null) {
			BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
			behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
			behavior.setSkipCollapsed(true);
			behavior.setDraggable(false); // Disable dragging to prevent accidental dismissal
		}

		// Manual input button
		view.findViewById(R.id.card_manual_input).setOnClickListener(v -> {
			dialog.dismiss();
			openTransactionCreate(false, null);
		});

		// OCR scan button
		view.findViewById(R.id.card_ocr_scan).setOnClickListener(v -> {
			dialog.dismiss();
			// Directly start the OCR flow
			showImageSourceDialog();
		});

		dialog.show();
	}

	private void showImageSourceDialog() {
		CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
		new MaterialAlertDialogBuilder(this)
				.setTitle("Scan Receipt")
				.setItems(options, (dialog, item) -> {
					if (options[item].equals("Take Photo")) {
						if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
							takePictureLauncher.launch(null);
						} else {
							requestPermissionLauncher.launch(Manifest.permission.CAMERA);
						}
					} else if (options[item].equals("Choose from Gallery")) {
						pickImageLauncher.launch("image/*");
					} else if (options[item].equals("Cancel")) {
						dialog.dismiss();
					}
				})
				.show();
	}


	private void openTransactionCreate(boolean isOCR, String ocrText) {
		TransactionCreateFragment transactionCreateFragment = new TransactionCreateFragment();

		Bundle args = new Bundle();
		args.putString("transaction_type", "expense");
		args.putBoolean("is_ocr", isOCR);
		if (ocrText != null) {
			args.putString("ocr_text", ocrText); // Pass the scanned text
		}
		transactionCreateFragment.setArguments(args);

		transactionCreateFragment.show(getSupportFragmentManager(), TransactionCreateFragment.TAG);
	}
}