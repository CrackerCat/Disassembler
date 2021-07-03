package fr.ralala.hexviewer.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.ui.adapters.RecentlyOpenRecyclerAdapter;
import fr.ralala.hexviewer.view.CenteredToolBar;

import static fr.ralala.hexviewer.ui.adapters.RecentlyOpenRecyclerAdapter.UriData;

public class RecentlyOpenActivity extends AppCompatActivity implements RecentlyOpenRecyclerAdapter.OnEventListener {
    private ApplicationCtx mApp = null;

    /**
     * Starts an activity.
     *
     * @param c                      Android context.
     * @param activityResultLauncher Activity Result Launcher.
     */
    public static void startActivity(final Context c, final ActivityResultLauncher<Intent> activityResultLauncher) {
        Intent intent = new Intent(c, RecentlyOpenActivity.class);
        activityResultLauncher.launch(intent);
    }

    /**
     * Set the base context for this ContextWrapper.
     * All calls will then be delegated to the base context.
     * Throws IllegalStateException if a base context has already been set.
     *
     * @param base The new base context for this wrapper.
     */
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(ApplicationCtx.getInstance().onAttach(base));
    }

    /**
     * Called when the activity is created.
     *
     * @param savedInstanceState Bundle
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_recently_open);
        setupToolbar(getString(R.string.app_name));
        mApp = ApplicationCtx.getInstance();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Lookup the recyclerview in activity layout
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        List<UriData> list = new ArrayList<>();
        final List<String> li = mApp.getRecentlyOpened();
        int index = 0;
        for (int i = li.size() - 1; i >= 0; i--) {
            list.add(new UriData(++index, li.get(i)));
        }
        RecentlyOpenRecyclerAdapter adapter = new RecentlyOpenRecyclerAdapter(this, list, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(adapter.getSwipeToDeleteCallback());
        itemTouchHelper.attachToRecyclerView(recyclerView);

        setTitle(getString(R.string.action_recently_open_title));
    }

    @SuppressWarnings("ConstantConditions")
    private void setupToolbar(String title) {
        CenteredToolBar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
    }

    /**
     * Called when the options item is clicked (home).
     *
     * @param item The selected menu.
     * @return boolean
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResult(RESULT_CANCELED);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when a click is captured.
     *
     * @param ud The associated item.
     */
    @Override
    public void onClick(@NonNull UriData ud) {
        Intent i = new Intent();
        i.setData(ud.uri);
        setResult(RESULT_OK, i);
        finish();
    }

    /**
     * Called when a click is captured.
     *
     * @param ud The associated item.
     */
    @Override
    public void onDelete(@NonNull UriData ud) {
        mApp.removeRecentlyOpened(ud.uri.toString());
        if (mApp.getRecentlyOpened().isEmpty()) {
            Intent i = new Intent();
            i.setData(null);
            setResult(RESULT_OK, i);
            finish();
        }
    }

}
