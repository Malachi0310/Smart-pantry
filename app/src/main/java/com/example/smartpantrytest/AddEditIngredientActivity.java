package com.example.smartpantrytest;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/**
 * Add/Edit Ingredient screen. Handles both creating a new pantry item and editing
 * an existing one, depending on whether a pantry item id was passed in via Intent extra.
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    private static final String[] UNITS = {"g", "kg", "ml", "l", "tsp", "tbsp", "cup", "unit"};

    private DatabaseHelper dbHelper;
    private EditText inputName, inputQuantity, inputExpiry;
    private Spinner inputUnit;
    private long editingId = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);

        dbHelper = new DatabaseHelper(this);

        Toolbar toolbar = findViewById(R.id.toolbarAddEdit);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        inputName = findViewById(R.id.inputIngredientName);
        inputQuantity = findViewById(R.id.inputIngredientQuantity);
        inputExpiry = findViewById(R.id.inputIngredientExpiry);
        inputUnit = findViewById(R.id.spinnerIngredientUnit);
        Button saveButton = findViewById(R.id.buttonSaveIngredient);

        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, UNITS);
        inputUnit.setAdapter(unitAdapter);

        editingId = getIntent().getLongExtra(PantryListActivity.EXTRA_PANTRY_ITEM_ID, -1);
        if (editingId != -1) {
            setTitle(R.string.title_edit_ingredient);
            populateFieldsForEdit(editingId);
        } else {
            setTitle(R.string.title_add_ingredient);
        }

        saveButton.setOnClickListener(v -> saveIngredient());
    }

    private void populateFieldsForEdit(long id) {
        PantryItem item = dbHelper.getPantryItem(id);
        if (item == null) return;
        inputName.setText(item.getName());
        inputQuantity.setText(formatQuantity(item.getQuantity()));
        inputExpiry.setText(item.getExpiryDate());
        int unitPosition = indexOf(UNITS, item.getUnit());
        if (unitPosition >= 0) inputUnit.setSelection(unitPosition);
    }

    private void saveIngredient() {
        String name = inputName.getText().toString().trim();
        String qtyText = inputQuantity.getText().toString().trim();
        String expiry = inputExpiry.getText().toString().trim();
        String unit = (String) inputUnit.getSelectedItem();

        // ---- input validation (Section 3.1 requirement) ----
        if (name.isEmpty()) {
            inputName.setError(getString(R.string.error_field_required));
            return;
        }
        if (qtyText.isEmpty()) {
            inputQuantity.setError(getString(R.string.error_field_required));
            return;
        }
        double quantity;
        try {
            quantity = Double.parseDouble(qtyText);
        } catch (NumberFormatException e) {
            inputQuantity.setError(getString(R.string.error_invalid_number));
            return;
        }
        if (quantity <= 0) {
            inputQuantity.setError(getString(R.string.error_positive_number));
            return;
        }

        PantryItem item = new PantryItem();
        item.setId(editingId);
        item.setName(name);
        item.setQuantity(quantity);
        item.setUnit(unit);
        item.setExpiryDate(expiry);

        if (editingId == -1) {
            dbHelper.addPantryItem(item);
            Toast.makeText(this, R.string.toast_ingredient_added, Toast.LENGTH_SHORT).show();
        } else {
            dbHelper.updatePantryItem(item);
            Toast.makeText(this, R.string.toast_ingredient_updated, Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private String formatQuantity(double q) {
        if (q == Math.floor(q)) return String.valueOf((int) q);
        return String.valueOf(q);
    }

    private int indexOf(String[] array, String value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equalsIgnoreCase(value)) return i;
        }
        return -1;
    }
}
