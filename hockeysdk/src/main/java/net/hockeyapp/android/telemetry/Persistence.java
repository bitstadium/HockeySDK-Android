package net.hockeyapp.android.telemetry;

import android.content.Context;
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
     * Synchronization LOCK for setting static context
     */
    private static final Object LOCK = new Object();

    private static final String BIT_TELEMETRY_DIRECTORY = "/com.microsoft.applicationinsights/telemetry/";

    private static final Integer MAX_FILE_COUNT = 50;

    private final ArrayList<File> servedFiles;

    /**
     * The tag for logging
     */
    private static final String TAG = "Persistence";

    /**
     * A weak reference to the app context
     */
    private WeakReference<Context> weakContext;

    /**
     * Restrict access to the default constructor
     *
     * @param context android Context object
     */
    protected Persistence(Context context) {
        this.weakContext = new WeakReference<Context>(context);
        createDirectoriesIfNecessary();
        this.servedFiles = new ArrayList<File>(51);
    }

    /**
     * Serializes a IJsonSerializable[] and calls:
     *
     * @param data the data to save to disk
     * @see Persistence#writeToDisk(String)
     */
    protected void persist(String[] data) {
        if (!this.isFreeSpaceAvailable()) {
            Log.w(TAG, "No free space on disk to flush data.");
            // TODO: Inform sender about available files
        }else{
            StringBuilder buffer = new StringBuilder();
            Boolean isSuccess;
            for (String aData : data) {
                if(buffer.length() > 0){
                    buffer.append('\n');
                }
                buffer.append(aData);
            }
            String serializedData = buffer.toString();
            isSuccess = writeToDisk(serializedData);
            if (isSuccess) {
                // TODO: Inform sender
            }
        }
    }

    /**
     * Saves a string to disk.
     *
     * @param data         the string to save
     * @return true if the operation was successful, false otherwise
     */
    protected boolean writeToDisk(String data) {
        String uuid = UUID.randomUUID().toString();
        Boolean isSuccess = false;
        Context context = getContext();
        if (context != null) {
            FileOutputStream outputStream = null;
            try {
                File filesDir = getContext().getFilesDir();
                filesDir = new File(filesDir + BIT_TELEMETRY_DIRECTORY + uuid);
                outputStream = new FileOutputStream(filesDir, true);
                outputStream.write(data.getBytes());

                isSuccess = true;
                Log.w(TAG, "Saving data to: " + filesDir.toString());
            } catch (Exception e) {
                //Do nothing
                Log.w(TAG, "Failed to save data with exception: " + e.toString());
            }finally {
                if(outputStream != null){
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
                FileInputStream inputStream = new FileInputStream(file);
                InputStreamReader streamReader = new InputStreamReader(inputStream);
                reader = new BufferedReader(streamReader);
                //comment: we can't use BufferedReader's readline() as this removes linebreaks that
                //are required for JSON stream
                int c;
                while ((c = reader.read()) != -1) {
                    //Cast c to char. As it's not -1, we won't get a problem
                    buffer.append((char) c);
                }
            } catch (Exception e) {
                Log.w(TAG, "Error reading telemetry data from file with exception message "
                      + e.getMessage());
            }finally {

                try{
                    if(reader != null) {
                        reader.close();
                    }
                }catch (IOException e){
                    Log.w(TAG, "Error closing stream."
                                + e.getMessage());
                }
            }
        }

        return buffer.toString();
    }

    /**
     * Get a reference to the next available file.
     *
     * @return the next available file.
     */
    protected File nextTelemetryFile() {

        synchronized (Persistence.LOCK) {
            Context context = getContext();
            if (context != null) {
                String path = context.getFilesDir() + BIT_TELEMETRY_DIRECTORY;
                File directory = new File(path);
                Log.i(TAG, "Returning Telemetry File: " + path);
                return this.nextAvailableFileInDirectory(directory);
            }
        }

        Log.w(TAG, "Couldn't provide next file, the context for persistence is null");
        return null;
    }

    /**
     * @param directory reference to the directory
     * @return reference to the next available file, null if no file is available
     */
    private File nextAvailableFileInDirectory(File directory) {
        synchronized (Persistence.LOCK) {
            if (directory != null) {
                File[] files = directory.listFiles();
                File file;

                if ((files != null) && (files.length > 0)) {
                    for (int i = 0; i <= files.length - 1; i++) {

                        file = files[i];
                        if (!this.servedFiles.contains(file)) {
                            Log.i(TAG, "The directory " + file.toString() + " (ADDING TO SERVED AND RETURN)");
                            this.servedFiles.add(file);
                            return file;
                        } else {
                            Log.i(TAG, "The directory " + file.toString() + " (WAS ALREADY SERVED)");
                        }
                    }
                }
            }
            if(directory != null) {
                Log.i(TAG, "The directory " + directory.toString() + " did not contain any unserved files");
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
            synchronized (Persistence.LOCK) {
                // always delete the file
                boolean deletedFile = file.delete();
                if (!deletedFile) {
                    Log.w(TAG, "Error deleting telemetry file " + file.toString());
                } else {
                    Log.w(TAG, "Successfully deleted telemetry file at: " + file.toString());
                    servedFiles.remove(file);
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
        synchronized (Persistence.LOCK) {
            if (file != null) {
                servedFiles.remove(file);
            }
        }
    }

    /**
     * Check if we haven't reached MAX_FILE_COUNT yet
     */
    protected Boolean isFreeSpaceAvailable() {
        synchronized (Persistence.LOCK) {
            Context context = getContext();
            if (context != null) {
                String path = (context.getFilesDir() + BIT_TELEMETRY_DIRECTORY);
                File dir = new File(path);
                return (dir.listFiles().length < MAX_FILE_COUNT);
            }
            return false;
        }
    }

    /**
     * Create local folders telemetry files if needed.
     */
    private void createDirectoriesIfNecessary() {
        String filesDirPath = getContext().getFilesDir().getPath();
        File dir = new File(filesDirPath + BIT_TELEMETRY_DIRECTORY);
        String successMessage = "Successfully created directory";
        String errorMessage = "Error creating directory";
        if (!dir.exists()) {
            if (dir.mkdirs()) {
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
        if (weakContext != null) {
            context = weakContext.get();
        }

        return context;
    }
}
