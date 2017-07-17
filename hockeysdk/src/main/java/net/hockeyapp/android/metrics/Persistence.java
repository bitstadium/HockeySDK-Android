package net.hockeyapp.android.metrics;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.annotation.WorkerThread;

import net.hockeyapp.android.utils.HockeyLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
     * Path for storing telemetry data files.
     */
    private static final String BIT_TELEMETRY_DIRECTORY = "/net.hockeyapp.android/telemetry/";
    /**
     * Maximum number of telemetry files to allow on disk.
     */
    private static final Integer MAX_FILE_COUNT = 50;
    /**
     * A weak reference to the app context.
     */
    private final WeakReference<Context> mWeakContext;
    /**
     * Sender module used to send out files.
     */
    private WeakReference<Sender> mWeakSender;
    /**
     * List with paths of telemetry files which are currently being used by the sender for transmission.
     */
    // TODO This looks like a violation of separation of concerns. Look into moving this to the sender.
    @VisibleForTesting
    ArrayList<File> mServedFiles;

    /**
     * Creates and initializes a new instance.
     *
     * @param context               Android Context object.
     * @param sender                Sender instance which will take care of telemetry transmission.
     */
    Persistence(@NonNull Context context, Sender sender) {
        mWeakContext = new WeakReference<>(context);
        mServedFiles = new ArrayList<>(MAX_FILE_COUNT + 1);
        mWeakSender = new WeakReference<>(sender);
    }

    /**
     * Persists serialized telemetry data to disk. Data points are joined by newlines, forming
     * line delimited JSON streaming data. Triggers sending of the persisted data if writing
     * was successful.
     *
     * @param data The data to save to disk.
     * @see Persistence#writeToDisk(String)
     */
    @WorkerThread
    @VisibleForTesting
    void persist(String[] data) {
        if (!this.isFreeSpaceAvailable()) {
            HockeyLog.warn(TAG, "Failed to persist file: Too many files on disk.");
        } else {
            StringBuilder buffer = new StringBuilder();
            for (String aData : data) {
                if (buffer.length() > 0) {
                    buffer.append('\n');
                }
                buffer.append(aData);
            }
            if (!writeToDisk(buffer.toString())) {
                return;
            }
        }
        Sender sender = getSender();
        if (sender != null) {
            sender.triggerSending();
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
    @WorkerThread
    boolean writeToDisk(String data) {
        File dir = getTelemetryDirectory();
        if (dir == null) {
            return false;
        }
        String uuid = UUID.randomUUID().toString();
        Boolean isSuccess = false;
        FileOutputStream outputStream = null;
        try {
            synchronized (this) {
                File file = new File(dir, uuid);
                outputStream = new FileOutputStream(file, true);
                outputStream.write(data.getBytes());
                HockeyLog.warn(TAG, "Saving data to: " + file.toString());
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
    @WorkerThread
    String load(File file) {
        StringBuilder buffer = new StringBuilder();
        if (file != null) {
            BufferedReader reader = null;
            try {
                synchronized (this) {
                    FileInputStream inputStream = new FileInputStream(file);
                    InputStreamReader streamReader = new InputStreamReader(inputStream);
                    reader = new BufferedReader(streamReader);
                    int c;
                    while ((c = reader.read()) != -1) {
                        buffer.append((char) c);
                    }
                }
            } catch (Exception e) {
                HockeyLog.warn(TAG, "Error reading telemetry data from file", e);
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException ignored) {
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
    @WorkerThread
    boolean hasFilesAvailable() {
        return nextAvailableFileInDirectory() != null;
    }

    /**
     * Gets the next file with telemetry data to transmit.
     *
     * @return Reference to the next available file, null if no file is available.
     */
    @WorkerThread
    @Nullable
    synchronized File nextAvailableFileInDirectory() {
        // TODO Separation of concerns. The persistence should provide all files, the sender would pick the right one.
        File dir = getTelemetryDirectory();
        File[] files = dir != null ? dir.listFiles() : null;
        if (files != null) {
            for (File file : files) {
                if (!mServedFiles.contains(file)) {
                    HockeyLog.info(TAG, "The directory " + file + " (ADDING TO SERVED AND RETURN)");
                    mServedFiles.add(file);
                    return file;
                } else {
                    HockeyLog.info(TAG, "The directory " + file + " (WAS ALREADY SERVED)");
                }
            }
        }
        HockeyLog.info(TAG, "The directory " + dir + " did not contain any unserved files");
        return null;
    }

    /**
     * Deletes a file from disk and removes it from the list of served files, if deletion was successful.
     *
     * @param file Reference to the file to delete.
     */
    @WorkerThread
    synchronized void deleteFile(File file) {
        if (file != null) {
            boolean deletedFile = file.delete();
            if (!deletedFile) {
                HockeyLog.warn(TAG, "Error deleting telemetry file " + file.toString());
            } else {
                HockeyLog.warn(TAG, "Successfully deleted telemetry file at: " + file.toString());
                mServedFiles.remove(file);
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
    synchronized void makeAvailable(File file) {
        if (file != null) {
            mServedFiles.remove(file);
        }
    }

    /**
     * Checks whether there is a slot left for a telemetry file.
     *
     * @return True if there is still space for another telemetry file.
     */
    @WorkerThread
    private synchronized boolean isFreeSpaceAvailable() {
        // TODO Check for available disk space as well.
        File dir = getTelemetryDirectory();
        File[] files = dir != null ? dir.listFiles() : null;
        return files != null && files.length < MAX_FILE_COUNT;
    }

    @Nullable
    @VisibleForTesting
    File getTelemetryDirectory() {
        Context context = getContext();
        if (context != null && context.getFilesDir() != null) {
            File dir = new File(context.getFilesDir(), BIT_TELEMETRY_DIRECTORY);
            if (dir.exists() || dir.mkdirs()) {
                return dir;
            }
            HockeyLog.error("Couldn't create directory for telemetry data");
        }
        return null;
    }

    /**
     * Retrieves the context from the weak reference.
     *
     * @return The context object for this instance.
     */
    @Nullable
    private Context getContext() {
        return mWeakContext.get();
    }

    /**
     * Retrieves the sender from the weak reference.
     *
     * @return The sender object for this instance.
     */
    @Nullable
    protected Sender getSender() {
        return mWeakSender != null ? mWeakSender.get() : null;
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
