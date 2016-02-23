package net.hockeyapp.android.metrics;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.UUID;

class Persistence {

    /**
     * The tag for logging
     */
    private static final String TAG = "HA-MetricsPersistence";

    /**
     * Synchronization LOCK for setting static context.
     */
    private static final Object LOCK = new Object();

    /**
     * Path for storing telemetry data files.
     */
    private static final String BIT_TELEMETRY_DIRECTORY = "/net.hockeyapp.android/telemetry/";
    /**
     * Maximum numbers of telemetry files on disk.
     */
    private static final Integer MAX_FILE_COUNT = 50;
    /**
     * Directory of telemetry files.
     */
    protected final File mTelemetryDirectory;
    /**
     * A weak reference to the app context
     */
    private final WeakReference<Context> mWeakContext;
    /**
     * Sender module used to send out files.
     */
    protected WeakReference<Sender> mWeakSender;
    /**
     * List with paths of telemetry files which are currently used by the sender.
     */
    protected ArrayList<File> mServedFiles;

    /**
     * Restrict access to the default constructor
     *
     * @param context            android Context object
     * @param telemetryDirectory the directory where files should be saved
     */
    protected Persistence(Context context, File telemetryDirectory, Sender sender) {
        mWeakContext = new WeakReference<>(context);
        mServedFiles = new ArrayList<>(51);
        mTelemetryDirectory = telemetryDirectory;
        mWeakSender = new WeakReference<>(sender);
        createDirectoriesIfNecessary();
    }

    /**
     * Restrict access to the default constructor
     *
     * @param context android Context object
     */
    protected Persistence(Context context, Sender sender) {
        this(context, new File(context.getFilesDir().getAbsolutePath() + BIT_TELEMETRY_DIRECTORY), null);
        this.setSender(sender);
    }

    /**
     * Serializes a IJsonSerializable[] and calls:
     *
     * @param data the data to save to disk
     * @see Persistence#writeToDisk(String)
     */
    protected void persist(String[] data) {
        if (!this.isFreeSpaceAvailable()) {
            Log.w(TAG, "Failed to persist file: Too many files on disk.");
            getSender().triggerSending();
        } else {
            StringBuilder buffer = new StringBuilder();
            Boolean isSuccess;
            for (String aData : data) {
                if (buffer.length() > 0) {
                    buffer.append('\n');
                }
                buffer.append(aData);
            }

            String serializedData = buffer.toString();
            isSuccess = writeToDisk(serializedData);
            if (isSuccess) {
                getSender().triggerSending();
            }
        }
    }

    /**
     * Saves a string to disk.
     *
     * @param data the string to save
     * @return true if the operation was successful, false otherwise
     */
    protected boolean writeToDisk(String data) {
        String uuid = UUID.randomUUID().toString();
        Boolean isSuccess = false;
        FileOutputStream outputStream = null;
        try {
            synchronized (this.LOCK) {
                File filesDir = new File(mTelemetryDirectory + "/" + uuid);
                outputStream = new FileOutputStream(filesDir, true);
                outputStream.write(data.getBytes());
                Log.w(TAG, "Saving data to: " + filesDir.toString());
            }
            isSuccess = true;
        } catch (Exception e) {
            Log.w(TAG, "Failed to save data with exception: " + e.toString());
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return isSuccess;
    }

    /**
     * Retrieves the data from a given path.
     *
     * @param file reference to a file on disk
     * @return the next item from disk or empty string if anything goes wrong
     */
    protected String load(File file) {
        StringBuilder buffer = new StringBuilder();
        if (file != null) {
            BufferedReader reader = null;
            try {
                synchronized (this.LOCK) {
                    FileInputStream inputStream = new FileInputStream(file);
                    InputStreamReader streamReader = new InputStreamReader(inputStream);
                    reader = new BufferedReader(streamReader);
                    int c;
                    while ((c = reader.read()) != -1) {
                        buffer.append((char) c);
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Error reading telemetry data from file with exception message "
                        + e.getMessage());
            } finally {

                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    Log.w(TAG, "Error closing stream."
                            + e.getMessage());
                }
            }
        }

        return buffer.toString();
    }

    /**
     * @return reference to the next available file, null if no file is available
     */
    protected File nextAvailableFileInDirectory() {
        synchronized (this.LOCK) {
            if (mTelemetryDirectory != null) {
                File[] files = mTelemetryDirectory.listFiles();
                File file;

                if ((files != null) && (files.length > 0)) {
                    for (int i = 0; i <= files.length - 1; i++) {

                        file = files[i];
                        if (!mServedFiles.contains(file)) {
                            Log.i(TAG, "The directory " + file.toString() + " (ADDING TO SERVED AND RETURN)");
                            mServedFiles.add(file);
                            return file;
                        } else {
                            Log.i(TAG, "The directory " + file.toString() + " (WAS ALREADY SERVED)");
                        }
                    }
                }
            }
            if (mTelemetryDirectory != null) {
                Log.i(TAG, "The directory " + mTelemetryDirectory.toString() + " did not contain any " +
                        "unserved files");
            }
            return null;
        }
    }

    /**
     * delete a file from disk and remove it from the list of served files if deletion was successful
     *
     * @param file reference to the file we want to delete
     */
    protected void deleteFile(File file) {
        if (file != null) {
            synchronized (this.LOCK) {
                boolean deletedFile = file.delete();
                if (!deletedFile) {
                    Log.w(TAG, "Error deleting telemetry file " + file.toString());
                } else {
                    Log.w(TAG, "Successfully deleted telemetry file at: " + file.toString());
                    mServedFiles.remove(file);
                }
            }
        } else {
            Log.w(TAG, "Couldn't delete file, the reference to the file was null");
        }
    }

    /**
     * Make a file available to be served again
     *
     * @param file reference to the file that should be made available so it can be sent again later
     */
    protected void makeAvailable(File file) {
        synchronized (this.LOCK) {
            if (file != null) {
                mServedFiles.remove(file);
            }
        }
    }

    /**
     * Check if we haven't reached MAX_FILE_COUNT yet
     */
    protected Boolean isFreeSpaceAvailable() {
        synchronized (this.LOCK) {
            Context context = getContext();
            if ((context.getFilesDir()) != null) {
                File filesDir = context.getFilesDir();
                String path = filesDir.getAbsolutePath() + BIT_TELEMETRY_DIRECTORY;
                if (TextUtils.isEmpty(path) == false) {
                    File dir = new File(path);
                    if (dir != null) {
                        return (dir.listFiles().length < MAX_FILE_COUNT);
                    }
                }
            }
            return false;
        }
    }

    /**
     * Create local folders telemetry files if needed.
     */
    protected void createDirectoriesIfNecessary() {
        String successMessage = "Successfully created directory";
        String errorMessage = "Error creating directory";
        if (mTelemetryDirectory != null && !mTelemetryDirectory.exists()) {
            if (mTelemetryDirectory.mkdirs()) {
                Log.i(TAG, successMessage);
            } else {
                Log.i(TAG, errorMessage);
            }
        }
    }

    /**
     * Retrieves the weak context reference.
     *
     * @return the context object for this instance
     */
    private Context getContext() {
        Context context = null;
        if (mWeakContext != null) {
            context = mWeakContext.get();
        }

        return context;
    }

    protected Sender getSender() {
        Sender sender = null;
        if (mWeakSender != null) {
            sender = mWeakSender.get();
        }

        return sender;
    }

    protected void setSender(Sender sender) {
        this.mWeakSender = new WeakReference<>(sender);
    }
}
