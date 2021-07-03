package fr.ralala.hexviewer.ui.tasks;


import android.app.Activity;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.models.Line;
import fr.ralala.hexviewer.models.LineData;
import fr.ralala.hexviewer.ui.utils.UIHelper;
import fr.ralala.hexviewer.utils.SysHelper;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Task used to save a file.
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class TaskSave extends ProgressTask<TaskSave.Request, TaskSave.Result> {
    private static final int MAX_LENGTH = SysHelper.MAX_BY_ROW * 10000;
    private final SaveResultListener mListener;
    private OutputStream mOutputStream = null;
    private ParcelFileDescriptor mParcelFileDescriptor = null;

    public TaskSave(final Activity activity, final SaveResultListener listener) {
        super(activity, false);
        mListener = listener;
    }

    /**
     * Called after the execution of the task.
     *
     * @param result The result.
     */
    @Override
    protected void onPostExecute(final Result result) {
        super.onPostExecute(result);
        final Activity a = mActivityRef.get();
        if (mCancel.get()) {
            if (result.uri != null) {
                final DocumentFile dfile = DocumentFile.fromSingleUri(a, result.uri);
                if (dfile != null && dfile.exists() && !dfile.delete()) {
                    Log.e(this.getClass().getSimpleName(), "File delete error");
                }
            }
            UIHelper.toast(a, a.getString(R.string.operation_canceled));
        } else if (result.exception == null)
            UIHelper.toast(a, a.getString(R.string.save_success));
        else
            UIHelper.toast(a, a.getString(R.string.exception) + ": " + result.exception);
        if (mListener != null)
            mListener.onSaveResult(result.uri, result.exception == null && !mCancel.get());
    }

    /**
     * Closes the stream.
     */
    private void close() {
        if (mOutputStream != null) {
            try {
                mOutputStream.close();
            } catch (final IOException e) {
                Log.e(this.getClass().getSimpleName(), "Exception: " + e.getMessage(), e);
            }
            mOutputStream = null;
        }
        if (mParcelFileDescriptor != null) {
            try {
                mParcelFileDescriptor.close();
            } catch (final IOException e) {
                Log.e(this.getClass().getSimpleName(), "Exception: " + e.getMessage(), e);
            }
            mParcelFileDescriptor = null;
        }

    }

    /**
     * Called when the task is cancelled.
     */
    @Override
    protected void onCancelled() {
        super.onCancelled();
        close();
        final Activity a = mActivityRef.get();
        UIHelper.toast(a, a.getString(R.string.operation_canceled));
    }

    /**
     * Called after the execution of the process.
     *
     * @param requests The requests.
     * @return Null or the exception message
     */
    @Override
    protected Result doInBackground(final Request... requests) {
        final Activity activity = mActivityRef.get();
        final Result result = new Result();
        final Request request = requests[0];
        result.uri = request.mUri;
        publishProgress(0L);
        try {
            mParcelFileDescriptor = activity.getContentResolver().openFileDescriptor(result.uri, "wt");
            List<Byte> bytes = new ArrayList<>();
            for (LineData<Line> entry : request.mEntries)
                bytes.addAll(entry.getValue().getRaw());
            final byte[] data = SysHelper.toByteArray(bytes, mCancel);
            if (!mCancel.get()) {
                mOutputStream = new FileOutputStream(mParcelFileDescriptor.getFileDescriptor());
                mTotalSize = data.length;
                final long count = mTotalSize / MAX_LENGTH;
                final long remain = mTotalSize - (count * MAX_LENGTH);

                long offset = 0;
                for (long i = 0; i < count && !mCancel.get(); i++) {
                    mOutputStream.write(data, (int) offset, MAX_LENGTH);
                    publishProgress((long) MAX_LENGTH);
                    offset += MAX_LENGTH;
                }
                if (!mCancel.get() && remain > 0) {
                    mOutputStream.write(data, (int) offset, (int) remain);
                    publishProgress(remain);
                }
                mOutputStream.flush();
            }
        } catch (final Exception e) {
            result.exception = e.getMessage();
        } finally {
            close();
        }
        return result;
    }

    public interface SaveResultListener {
        void onSaveResult(Uri uri, boolean success);
    }

    public static class Result {
        private String exception = null;
        private Uri uri = null;
    }

    public static class Request {
        private final Uri mUri;
        private final List<LineData<Line>> mEntries;

        public Request(Uri uri, List<LineData<Line>> entries) {
            mUri = uri;
            mEntries = entries;
        }

    }
}
