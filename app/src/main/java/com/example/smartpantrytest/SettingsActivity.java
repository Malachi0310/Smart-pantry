package com.example.smartpantrytest;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.Switch;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/**
 * Settings screen (Section 2.2 minimum-screens requirement). Preferences are saved
 * with SharedPreferences so they persist between app launches.
 */
public class SettingsActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "smart_pantry_prefs";
    public static final String KEY_EXPIRY_ALERTS = "expiry_alerts_enabled";
    public static final String KEY_UNIT_SYSTEM = "unit_system"; // "metric" or "imperial"

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbarSettings);
        setSupportActionBar(toolbar);
        setTitle(R.string.title_settings);
        toolbar.setNavigationOnClickListener(v -> finish());

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        Switch expiryAlertsSwitch = findViewById(R.id.switchExpiryAlerts);
        RadioGroup unitSystemGroup = findViewById(R.id.radioGroupUnitSystem);

        expiryAlertsSwitch.setChecked(prefs.getBoolean(KEY_EXPIRY_ALERTS, true));
        boolean isMetric = prefs.getString(KEY_UNIT_SYSTEM, "metric").equals("metric");
        unitSystemGroup.check(isMetric ? R.id.radioMetric : R.id.radioImperial);

        expiryAlertsSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                prefs.edit().putBoolean(KEY_EXPIRY_ALERTS, isChecked).apply());

        unitSystemGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String value = (checkedId == R.id.radioMetric) ? "metric" : "imperial";
            prefs.edit().putString(KEY_UNIT_SYSTEM, value).apply();
        });
    }
}
