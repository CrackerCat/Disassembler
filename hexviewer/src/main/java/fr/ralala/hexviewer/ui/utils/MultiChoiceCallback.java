package fr.ralala.hexviewer.ui.utils;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ListView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.models.Line;
import fr.ralala.hexviewer.models.LineFilter;
import fr.ralala.hexviewer.ui.activities.MainActivity;
import fr.ralala.hexviewer.ui.adapters.HexTextArrayAdapter;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * MultiChoiceModeListener implementation
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class MultiChoiceCallback implements AbsListView.MultiChoiceModeListener {
    private final ListView mListView;
    private final HexTextArrayAdapter mAdapter;
    private final MainActivity mActivity;

    public MultiChoiceCallback(MainActivity mainActivity, final ListView listView, final HexTextArrayAdapter adapter) {
        mActivity = mainActivity;
        mListView = listView;
        mAdapter = adapter;
    }

    /**
     * Called when action mode is first created. The menu supplied will be used to generate action buttons for the action mode.
     *
     * @param mode ActionMode being created.
     * @param menu Menu used to populate action buttons.
     * @return true if the action mode should be created, false if entering this mode should be aborted.
     */
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.main_clear, menu);
        return true;
    }

    /**
     * Called to refresh an action mode's action menu whenever it is invalidated.
     *
     * @param mode ActionMode being prepared.
     * @param menu Menu used to populate action buttons.
     * @return true if the menu or action mode was updated, false otherwise.
     */
    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    /**
     * Called to report a user click on an action button.
     *
     * @param mode The current ActionMode.
     * @param item The item that was clicked.
     * @return true if this callback handled the event, false if the standard MenuItem invocation should continue.
     */
    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        if (item.getItemId() == R.id.action_clear) {
            final List<Integer> selected = mAdapter.getSelectedIds();
            Map<Integer, LineFilter<Line>> map = new HashMap<>();
            HexTextArrayAdapter adapter = mActivity.getAdapterHex();
            // Captures all selected ids with a loop
            for (int i = selected.size() - 1; i >= 0; i--) {
                int position = selected.get(i);
                map.put(position, adapter.getFilteredList().get(position));
            }
            mActivity.getUnDoRedo().insertInUnDoRedoForDelete(adapter, map).execute();
            mActivity.setTitle(mActivity.getResources().getConfiguration());
            // Close CAB
            mode.finish();
            return true;
        } else if (item.getItemId() == R.id.action_select_all) {
            final int count = mAdapter.getCount();
            for (int i = 0; i < count; i++) {
                mListView.setItemChecked(i, true);
            }
            return true;
        }
        return false;
    }

    /**
     * Called when an action mode is about to be exited and destroyed.
     *
     * @param mode The current ActionMode being destroyed.
     */
    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mAdapter.removeSelection();
    }

    /**
     * Called when an item is checked or unchecked during selection mode.
     *
     * @param mode     The ActionMode providing the selection mode.
     * @param position Adapter position of the item that was checked or unchecked.
     * @param id       Adapter ID of the item that was checked or unchecked.
     * @param checked  true if the item is now checked, false if the item is now unchecked.
     */
    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        final int checkedCount = mListView.getCheckedItemCount();
        mode.setTitle(String.format(mActivity.getString(R.string.items_selected), checkedCount));
        mAdapter.toggleSelection(position, checked);
    }
}
