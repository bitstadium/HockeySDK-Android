package net.hockeyapp.android.objects;

import net.hockeyapp.android.Constants;
import net.hockeyapp.android.utils.HockeyLog;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Model Object that represents a class method.
 * Includes helper methods to for reading a CrashReport
 */
public class CrashReport {

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);

    private static final String FIELD_CRASH_REPORTER_KEY = "CrashReporter Key";
    private static final String FIELD_APP_START_DATE = "Start Date";
    private static final String FIELD_APP_CRASH_DATE = "Date";
    private static final String FIELD_OS_VERSION = "Android";
    private static final String FIELD_OS_BUILD = "Android Build";
    private static final String FIELD_DEVICE_MANUFACTURER = "Manufacturer";
    private static final String FIELD_DEVICE_MODEL = "Model";
    private static final String FIELD_APP_PACKAGE = "Package";
    private static final String FIELD_APP_VERSION_NAME = "Version Name";
    private static final String FIELD_APP_VERSION_CODE = "Version Code";
    private static final String FIELD_THREAD_NAME = "Thread";

    private final String crashIdentifier;

    private String reporterKey;

    private Date appStartDate;
    private Date appCrashDate;

    private String osVersion;
    private String osBuild;
    private String deviceManufacturer;
    private String deviceModel;

    private String appPackage;
    private String appVersionName;
    private String appVersionCode;

    private String threadName;

    private String throwableStackTrace;

    public CrashReport(String crashIdentifier) {
        this.crashIdentifier = crashIdentifier;
    }

    public CrashReport(String crashIdentifier, Throwable throwable) {
        this(crashIdentifier);

        final Writer stackTraceResult = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stackTraceResult);
        throwable.printStackTrace(printWriter);
        throwableStackTrace = stackTraceResult.toString();
    }

    public static CrashReport fromFile(File file) throws IOException {
        String crashIdentifier = file.getName().substring(0, file.getName().indexOf(".stacktrace"));
        return fromReader(crashIdentifier, new FileReader(file));
    }

    //TODO move this somewhere else to make sure CrashReport is a stupid POJO without reading/writing logic?
    public static CrashReport fromReader(String crashIdentifier, Reader in) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(in);

        CrashReport result = new CrashReport(crashIdentifier);

        String readLine, headerName, headerValue;
        boolean headersProcessed = false;
        StringBuilder stackTraceBuilder = new StringBuilder();
        while ((readLine = bufferedReader.readLine()) != null) {
            if (!headersProcessed) {

                if (readLine.isEmpty()) {
                    // empty line denotes break between headers and stack trace
                    headersProcessed = true;
                    continue;
                }

                int colonIndex = readLine.indexOf(":");
                if (colonIndex < 0) {
                    HockeyLog.error("Malformed header line when parsing crash details: \"" + readLine + "\"");
                }

                headerName = readLine.substring(0, colonIndex).trim();
                headerValue = readLine.substring(colonIndex + 1, readLine.length()).trim();

                if (headerName.equals(FIELD_CRASH_REPORTER_KEY)) {
                    result.setReporterKey(headerValue);
                } else if (headerName.equals(FIELD_APP_START_DATE)) {
                    try {
                        result.setAppStartDate(DATE_FORMAT.parse(headerValue));
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                } else if (headerName.equals(FIELD_APP_CRASH_DATE)) {
                    try {
                        result.setAppCrashDate(DATE_FORMAT.parse(headerValue));
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                } else if (headerName.equals(FIELD_OS_VERSION)) {
                    result.setOsVersion(headerValue);
                } else if (headerName.equals(FIELD_OS_BUILD)) {
                    result.setOsBuild(headerValue);
                } else if (headerName.equals(FIELD_DEVICE_MANUFACTURER)) {
                    result.setDeviceManufacturer(headerValue);
                } else if (headerName.equals(FIELD_DEVICE_MODEL)) {
                    result.setDeviceModel(headerValue);
                } else if (headerName.equals(FIELD_APP_PACKAGE)) {
                    result.setAppPackage(headerValue);
                } else if (headerName.equals(FIELD_APP_VERSION_NAME)) {
                    result.setAppVersionName(headerValue);
                } else if (headerName.equals(FIELD_APP_VERSION_CODE)) {
                    result.setAppVersionCode(headerValue);
                } else if (headerName.equals(FIELD_THREAD_NAME)) {
                    result.setThreadName(headerValue);
                }

            } else {
                stackTraceBuilder.append(readLine).append("\n");
            }
        }
        result.setThrowableStackTrace(stackTraceBuilder.toString());

        return result;
    }

    public void writeCrashReport() {
        String path = Constants.FILES_PATH + "/" + crashIdentifier + ".stacktrace";
        HockeyLog.debug("Writing unhandled exception to: " + path);

        BufferedWriter writer = null;

        try {
            writer = new BufferedWriter(new FileWriter(path));

            writeHeader(writer, FIELD_APP_PACKAGE, appPackage);
            writeHeader(writer, FIELD_APP_VERSION_CODE, appVersionCode);
            writeHeader(writer, FIELD_APP_VERSION_NAME, appVersionName);
            writeHeader(writer, FIELD_OS_VERSION, osVersion);
            writeHeader(writer, FIELD_OS_BUILD, osBuild);
            writeHeader(writer, FIELD_DEVICE_MANUFACTURER, deviceManufacturer);
            writeHeader(writer, FIELD_DEVICE_MODEL, deviceModel);
            writeHeader(writer, FIELD_THREAD_NAME, threadName);
            writeHeader(writer, FIELD_CRASH_REPORTER_KEY, reporterKey);

            writeHeader(writer, FIELD_APP_START_DATE, DATE_FORMAT.format(appStartDate));
            writeHeader(writer, FIELD_APP_CRASH_DATE, DATE_FORMAT.format(appCrashDate));

            writer.write("\n");
            writer.write(throwableStackTrace);

            writer.flush();

        } catch (IOException e) {
            HockeyLog.error("Error saving crash report!", e);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e1) {
                HockeyLog.error("Error saving crash report!", e1);
            }
        }


    }

    private void writeHeader(Writer writer, String name, String value) throws IOException {
        writer.write(name + ": " + value + "\n");
    }

    public String getCrashIdentifier() {
        return crashIdentifier;
    }

    public String getReporterKey() {
        return reporterKey;
    }

    public void setReporterKey(String reporterKey) {
        this.reporterKey = reporterKey;
    }

    public Date getAppStartDate() {
        return appStartDate;
    }

    public void setAppStartDate(Date appStartDate) {
        this.appStartDate = appStartDate;
    }

    public Date getAppCrashDate() {
        return appCrashDate;
    }

    public void setAppCrashDate(Date appCrashDate) {
        this.appCrashDate = appCrashDate;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getOsBuild() {
        return osBuild;
    }

    public void setOsBuild(String osBuild) {
        this.osBuild = osBuild;
    }

    public String getDeviceManufacturer() {
        return deviceManufacturer;
    }

    public void setDeviceManufacturer(String deviceManufacturer) {
        this.deviceManufacturer = deviceManufacturer;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getAppPackage() {
        return appPackage;
    }

    public void setAppPackage(String appPackage) {
        this.appPackage = appPackage;
    }

    public String getAppVersionName() {
        return appVersionName;
    }

    public void setAppVersionName(String appVersionName) {
        this.appVersionName = appVersionName;
    }

    public String getAppVersionCode() {
        return appVersionCode;
    }

    public void setAppVersionCode(String appVersionCode) {
        this.appVersionCode = appVersionCode;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public String getThrowableStackTrace() {
        return throwableStackTrace;
    }

    public void setThrowableStackTrace(String throwableStackTrace) {
        this.throwableStackTrace = throwableStackTrace;
    }

}
