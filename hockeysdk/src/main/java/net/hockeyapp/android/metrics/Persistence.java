package net.hockeyapp.android.metrics;

import android.content.Context;
import android.text.TextUtils;
import net.hockeyapp.android.utils.HockeyLog;

import java.io.*;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.UUID;

/**
 * <h3>Description</h3>
 * <p/>
 * Persistence layer to save and manage telemetry data on disk before sending.
 * Telemetry data is saved in batches which make up one file. There is a maximum total number
 * of telemetry files kept by the persistence, in order to not exceed disk storage limiations.
 * If too many files are kept, the persistence will reject further persistence calls, but will
 * not remove older telemetry files until they are sent.
 */
class Persistence {

    /**
     * The tag for logging.
     */
    private static final String TAG = "HA-MetricsPersistence";
    /**
     * Synchronization lock.
     */
    private static final Object LOCK = new Object();
    /**
     * Path for storing telemetry data files.
     */
    private static final String BIT_TELEMETRY_DIRECTORY = "/net.hockeyapp.android/telemetry/";
    /**
     * Maximum number of telemetry files to allow on disk.
     */
    private static final Integer MAX_FILE_COUNT = 50;
    /**
     * Directory of telemetry files.
     */
    protected final File mTelemetryDirectory;
    /**
     * A weak reference to the app context.
     */
    private final WeakReference<Context> mWeakContext;
    /**
     * Sender module used to send out files.
     */
    protected WeakReference<Sender> mWeakSender;
    /**
     * List with paths of telemetry files which are currently being used by the sender for transmission.
     */
    // TODO This looks like a violation of separation of concerns. Look into moving this to the sender.
    protected ArrayList<File> mServedFiles;

    /**
     * Creates and initializes a new instance.
     *
     * @param context               Android Context object.
     * @param telemetryDirectory    The directory where files should be saved.
     * @param sender                Sender instance which will take care of telemetry transmission.
     */
    protected Persistence(Context context, File telemetryDirectory, Sender sender) {
        mWeakContext = new WeakReference<>(context);
        mServedFiles = new ArrayList<>(51);
        mTelemetryDirectory = telemetryDirectory;
        mWeakSender = new WeakReference<>(sender);
        createDirectoriesIfNecessary();
    }

    /**
     * Creates and initializes a new instance.
     *
     * @param context   Android Context object.
     * @param sender    Sender instance which will take care of telemetry transmission.
     */
    protected Persistence(Context context, Sender sender) {
        this(context, new File(context.getFilesDir().getAbsolutePath() + BIT_TELEMETRY_DIRECTORY), null);
        this.setSender(sender);
    }

    /**
     * Persists serialized telemetry data to disk. Data points are joined by newlines, forming
     * line delimited JSON streaming data. Triggers sending of the persisted data if writing
     * was successful.
     *
     * @param data The data to save to disk.
     * @see Persistence#writeToDisk(String)
     */
    protected void persist(String[] data) {
        if (!this.isFreeSpaceAvailable()) {
            HockeyLog.warn(TAG, "Failed to persist file: Too many files on disk.");
            getSender().triggerSending();
        } else {
            StringBuilder buffer = new StringBuilder();
            boolean isSuccess;
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
     * Saves a string of serialized telemetry data objects to disk.
     * It will create a random UUID file in the storage directory
     * and save the data to this file.
     *
     * @param data The complete data string to save.
     * @return True if the operation was successful, false otherwise.
     */
    protected boolean writeToDisk(String data) {
        String uuid = UUID.randomUUID().toString();
        Boolean isSuccess = false;
        FileOutputStream outputStream = null;
        try {
            synchronized (LOCK) {
                File filesDir = new File(mTelemetryDirectory + "/" + uuid);
                outputStream = new FileOutputStream(filesDir, true);
                outputStream.write(data.getBytes());
                HockeyLog.warn(TAG, "Saving data to: " + filesDir.toString());
            }
            isSuccess = true;
        } catch (Exception e) {
            HockeyLog.warn(TAG, "Failed to save data with exception", e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
        return isSuccess;
    }

    /**
     * Retrieves string data from a given path.
     *
     * @param file Reference to a file on disk.
     * @return The next item from disk, or empty string if anything goes wrong.
     */
    protected String load(File file) {
        StringBuilder buffer = new StringBuilder();
        if (file != null) {
            BufferedReader reader = null;
            try {
                synchronized (LOCK) {
                    FileInputStream inputStream = new FileInputStream(file);
                    InputStreamReader streamReader = new InputStreamReader(inputStream);
                    reader = new BufferedReader(streamReader);
                    int c;
                    while ((c = reader.read()) != -1) {
                        buffer.append((char) c);
                    }
                }
            } catch (Exception e) {
                HockeyLog.warn(TAG, "Error reading telemetry data from file with exception message "
                        + e.getMessage());
            } finally {

                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    HockeyLog.warn(TAG, "Error closing stream."
                            + e.getMessage());
                }
            }
        }

        return buffer.toString();
    }

    /**
     * Checks, if there are telemetry files available for sending.
     *
     * @return True if files are available, false otherwise.
     */
    protected boolean hasFilesAvailable() {
        return nextAvailableFileInDirectory() != null;
    }

    /**
     * Gets the next file with telemetry data to transmit.
     *
     * @return Reference to the next available file, null if no file is available.
     */
    protected File nextAvailableFileInDirectory() {
        // TODO Separation of concerns. The persistence should provide all files, the sender would pick the right one.
        synchronized (LOCK) {
            if (mTelemetryDirectory != null) {
                File[] files = mTelemetryDirectory.listFiles();
                File file;

                if ((files != null) && (files.length > 0)) {
                    for (int i = 0; i <= files.length - 1; i++) {

                        file = files[i];
                        if (!mServedFiles.contains(file)) {
                            HockeyLog.info(TAG, "The directory " + file.toString() + " (ADDING TO SERVED AND RETURN)");
                            mServedFiles.add(file);
                            return file;
                        } else {
                            HockeyLog.info(TAG, "The directory " + file.toString() + " (WAS ALREADY SERVED)");
                        }
                    }
                }
            }
            if (mTelemetryDirectory != null) {
                HockeyLog.info(TAG, "The directory " + mTelemetryDirectory.toString() + " did not contain any " +
                        "unserved files");
            }
            return null;
        }
    }

    /**
     * Deletes a file from disk and removes it from the list of served files, if deletion was successful.
     *
     * @param file Reference to the file to delete.
     */
    protected void deleteFile(File file) {
        if (file != null) {
            synchronized (LOCK) {
                boolean deletedFile = file.delete();
                if (!deletedFile) {
                    HockeyLog.warn(TAG, "Error deleting telemetry file " + file.toString());
                } else {
                    HockeyLog.warn(TAG, "Successfully deleted telemetry file at: " + file.toString());
                    mServedFiles.remove(file);
                }
            }
        } else {
            HockeyLog.warn(TAG, "Couldn't delete file, the reference to the file was null");
        }
    }

    /**
     * Remove a file from the list of served files. Remove files from the served list
     * that should be made available so it can be sent again later.
     *
     * @param file Reference to the file to remove from the list.
     */
    protected void makeAvailable(File file) {
        synchronized (LOCK) {
            if (file != null) {
                mServedFiles.remove(file);
            }
        }
    }

    /**
     * Checks whether there is a slot left for a telemetry file.
     * @return True if there is still space for another telemetry file.
     */
    protected boolean isFreeSpaceAvailable() {
        // TODO Check for available disk space as well.
        synchronized (LOCK) {
            Context context = getContext();
            if (context.getFilesDir() != null) {
                File filesDir = context.getFilesDir();
                String path = filesDir.getAbsolutePath() + BIT_TELEMETRY_DIRECTORY;
                if (!TextUtils.isEmpty(path)) {
                    File dir = new File(path);
                    File[] files = dir.listFiles();
                    return files != null && files.length < MAX_FILE_COUNT;
                }
            }
            return false;
        }
    }

    /**
     * Create directory structure for telemetry data.
     */
    protected void createDirectoriesIfNecessary() {
        String successMessage = "Successfully created directory";
        String errorMessage = "Error creating directory";
        if (mTelemetryDirectory != null && !mTelemetryDirectory.exists()) {
            if (mTelemetryDirectory.mkdirs()) {
                HockeyLog.info(TAG, successMessage);
            } else {
                HockeyLog.info(TAG, errorMessage);
            }
        }
    }

    /**
     * Retrieves the context from the weak reference.
     *
     * @return The context object for this instance.
     */
    private Context getContext() {
        Context context = null;
        if (mWeakContext != null) {
            context = mWeakContext.get();
        }

        return context;
    }

    /**
     * Retrieves the sender from the weak reference.
     *
     * @return The sender object for this instance.
     */
    protected Sender getSender() {
        Sender sender = null;
        if (mWeakSender != null) {
            sender = mWeakSender.get();
        }

        return sender;
    }

    /**
     * Set the sender for this instance. Stores a weak reference to the sender.
     *
     * @param sender The sender to store for this instance.
     */
    protected void setSender(Sender sender) {
        this.mWeakSender = new WeakReference<>(sender);
    }
}
