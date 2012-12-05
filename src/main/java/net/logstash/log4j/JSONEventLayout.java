package net.logstash.log4j;

import net.logstash.log4j.data.HostData;

import java.util.*;
import java.util.Date;
import java.text.DateFormat;

import net.minidev.json.JSONObject;
import org.apache.commons.lang.*;
import org.apache.log4j.helpers.ISO8601DateFormat;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.apache.log4j.spi.LocationInfo;

public class JSONEventLayout extends Layout {

    private String tags;
    private boolean ignoreThrowable = false;

    private boolean activeIgnoreThrowable = ignoreThrowable;
    private String hostname;
    private long timestamp;
    private String ndc;
    private Map mdc;
    private List<String> thro;
    private LocationInfo info;
    private HashMap<String, Object> fieldData;

    private JSONObject logstashEvent;


    public String format(LoggingEvent loggingEvent) {
        hostname = new HostData().getHostName();
        timestamp = loggingEvent.getTimeStamp();
        info = loggingEvent.getLocationInformation();
        fieldData = new HashMap<String, Object>();
        mdc = loggingEvent.getProperties();
        ndc = loggingEvent.getNDC();

        logstashEvent = new JSONObject();

        logstashEvent.put("@source_host",hostname);
        logstashEvent.put("@message",loggingEvent.getRenderedMessage());

        if(loggingEvent.getThrowableInformation() != null) {
            final ThrowableInformation throwableInformation = loggingEvent.getThrowableInformation();
            if( throwableInformation.getThrowableStrRep() != null) {
                String stackTrace = StringUtils.join(throwableInformation.getThrowableStrRep(),"\n");
                addFieldData("stacktrace",stackTrace);
            }
        }


        if(loggingEvent.locationInformationExists()) {
            info = loggingEvent.getLocationInformation();
            addFieldData("file",info.getFileName());
            addFieldData("line_number",info.getLineNumber());
            addFieldData("class",info.getClassName());
            addFieldData("method",info.getMethodName());
        }

        addFieldData("mdc",mdc);
        addFieldData("ndc",ndc);
        addFieldData("level",loggingEvent.getLevel().toString());
        addFieldData("timestamp",timestamp);

        logstashEvent.put("@fields",fieldData);
        return logstashEvent.toString() + "\n";
    }

    public boolean ignoresThrowable() {
        return ignoreThrowable;
    }

    public void activateOptions() {
        activeIgnoreThrowable = ignoreThrowable;
    }

    private void addFieldData(String keyname, Object keyval){
        if(null != keyval){
            fieldData.put(keyname, keyval);
        }
    }
}