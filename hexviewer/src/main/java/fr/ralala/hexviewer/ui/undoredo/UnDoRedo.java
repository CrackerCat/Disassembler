package fr.ralala.hexviewer.ui.undoredo;

import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.Map;
import java.util.Stack;

import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.models.Line;
import fr.ralala.hexviewer.models.LineData;
import fr.ralala.hexviewer.models.LineFilter;
import fr.ralala.hexviewer.ui.activities.MainActivity;
import fr.ralala.hexviewer.ui.adapters.HexTextArrayAdapter;
import fr.ralala.hexviewer.ui.undoredo.commands.DeleteCommand;
import fr.ralala.hexviewer.ui.undoredo.commands.UpdateCommand;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Undo Redo Manager
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class UnDoRedo {
    private static final int CONTROL_UNDO = 0;
    private static final int CONTROL_REDO = 1;
    private final MainActivity mActivity;
    private final Control[] mControls;
    private final Stack<ICommand> mUndo;
    private final Stack<ICommand> mRedo;
    private int mReferenceIndex;

    public UnDoRedo(MainActivity activity) {
        mActivity = activity;
        mControls = new Control[2];
        mUndo = new Stack<>();
        mRedo = new Stack<>();
    }

    /**
     * Sets the controls.
     *
     * @param containerUndo FrameLayout
     * @param viewUndo      ImageView
     * @param containerRedo FrameLayout
     * @param viewRedo      ImageView
     */
    public void setControls(final FrameLayout containerUndo, final ImageView viewUndo, final FrameLayout containerRedo, final ImageView viewRedo) {
        mControls[CONTROL_UNDO] = new Control();
        mControls[CONTROL_UNDO].container = containerUndo;
        mControls[CONTROL_UNDO].img = viewUndo;
        mControls[CONTROL_UNDO].disable = R.drawable.ic_undo_disabled;
        mControls[CONTROL_UNDO].enable = R.drawable.ic_undo;
        mControls[CONTROL_REDO] = new Control();
        mControls[CONTROL_REDO].container = containerRedo;
        mControls[CONTROL_REDO].img = viewRedo;
        mControls[CONTROL_REDO].disable = R.drawable.ic_redo_disabled;
        mControls[CONTROL_REDO].enable = R.drawable.ic_redo;
    }

    /**
     * Tests if a change is detected.
     *
     * @return boolean
     */
    public boolean isChanged() {
        return mReferenceIndex != mUndo.size();
    }

    /**
     * Updates change index.
     */
    public void refreshChange() {
        mReferenceIndex = mUndo.size();
    }

    /**
     * Updates command.
     *
     * @param activity      MainActivity.
     * @param firstPosition The first position index.
     * @param entries       The entries.
     * @return The command.
     */
    public ICommand insertInUnDoRedoForUpdate(final MainActivity activity, final int firstPosition, List<LineData<Line>> entries) {
        ICommand cmd = new UpdateCommand(activity, firstPosition, entries);
        mUndo.push(cmd);
        manageControl(mControls[CONTROL_UNDO], true);
        manageControl(mControls[CONTROL_REDO], false);
        mRedo.clear();

        mActivity.setTitle(mActivity.getResources().getConfiguration());
        return cmd;
    }

    /**
     * Inserts delete command.
     *
     * @param adapter HexTextArrayAdapter.
     * @param entries The entries.
     * @return The command.
     */
    public ICommand insertInUnDoRedoForDelete(final HexTextArrayAdapter adapter, final Map<Integer, LineFilter<Line>> entries) {
        ICommand cmd = new DeleteCommand(adapter, entries);
        mUndo.push(cmd);
        manageControl(mControls[CONTROL_UNDO], true);
        manageControl(mControls[CONTROL_REDO], false);
        mRedo.clear();

        mActivity.setTitle(mActivity.getResources().getConfiguration());
        return cmd;
    }

    /**
     * Undo action
     */
    public void undo() {
        if (!mUndo.isEmpty()) {
            ICommand command = mUndo.pop();
            command.unExecute();
            mRedo.push(command);
            manageControl(mControls[CONTROL_REDO], true);
        }
        mActivity.setTitle(mActivity.getResources().getConfiguration());
        manageControl(mControls[CONTROL_UNDO], !mUndo.isEmpty());
    }

    /**
     * Redo action.
     */
    public void redo() {
        if (!mRedo.isEmpty()) {
            ICommand command = mRedo.pop();
            command.execute();
            mUndo.push(command);
            manageControl(mControls[CONTROL_UNDO], true);
        }
        mActivity.setTitle(mActivity.getResources().getConfiguration());
        manageControl(mControls[CONTROL_REDO], !mRedo.isEmpty());
    }

    /**
     * Clears the undo/redo stacks.
     */
    public void clear() {
        for (Control ctrl : mControls)
            manageControl(ctrl, false);
        mUndo.clear();
        mRedo.clear();
        mActivity.setTitle(mActivity.getResources().getConfiguration());
    }

    /**
     * Manages control state.
     *
     * @param control The control.
     * @param enabled Enabled ?
     */
    private void manageControl(final Control control, final boolean enabled) {
        if (control != null && control.img != null) {
            if (control.container != null)
                control.container.setEnabled(enabled);
            control.img.setImageDrawable(ContextCompat.getDrawable(mActivity, enabled ? control.enable : control.disable));
            control.img.setEnabled(enabled);
        }
    }

    private static class Control {
        private FrameLayout container;
        private ImageView img;
        private int enable;
        private int disable;
    }
}
