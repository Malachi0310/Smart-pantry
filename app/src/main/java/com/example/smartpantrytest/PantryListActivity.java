package com.example.smartpantrytest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * Pantry List screen (launcher activity). Shows every ingredient the user currently
 * has and lets them add, edit or delete items - the "Read" screen plus the entry
 * point into Create/Update/Delete for the CRUD requirement (Section 3.2).
 */
public class PantryListActivity extends AppCompatActivity implements PantryAdapter.OnPantryItemActionListener {

    public static final String EXTRA_PANTRY_ITEM_ID = "extra_pantry_item_id";

    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private TextView emptyStateText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry_list);

        Toolbar toolbar = findViewById(R.id.toolbarPantryList);
        setSupportActionBar(toolbar);
        setTitle(R.string.title_pantry_list);

        dbHelper = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.recyclerPantry);
        emptyStateText = findViewById(R.id.textPantryEmpty);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fab = findViewById(R.id.fabAddIngredient);
        fab.setOnClickListener(v -> startActivity(new Intent(this, AddEditIngredientActivity.class)));

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_pantry);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_pantry) {
                return true;
            } else if (id == R.id.nav_suggestions) {
                startActivity(new Intent(this, SuggestedRecipesActivity.class));
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh every time this screen becomes visible again, since items may
        // have just been added/edited/deleted on the Add/Edit screen.
        loadPantryItems();
    }

    private void loadPantryItems() {
        List<PantryItem> items = dbHelper.getAllPantryItems();
        recyclerView.setAdapter(new PantryAdapter(items, this));
        emptyStateText.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onEdit(PantryItem item) {
        Intent intent = new Intent(this, AddEditIngredientActivity.class);
        intent.putExtra(EXTRA_PANTRY_ITEM_ID, item.getId());
        startActivity(intent);
    }

    @Override
    public void onDelete(PantryItem item) {
        dbHelper.deletePantryItem(item.getId());
        loadPantryItems();
    }
}
