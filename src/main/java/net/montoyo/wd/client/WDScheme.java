/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.client;

import net.montoyo.wd.miniserv.Constants;
import net.montoyo.wd.miniserv.client.Client;
import net.montoyo.wd.miniserv.client.ClientTaskGetFile;
import net.montoyo.wd.utilities.Log;
import net.montoyo.wd.utilities.serialization.Util;
import org.cef.callback.CefCallback;
import org.cef.handler.CefResourceHandler;
import org.cef.misc.IntRef;
import org.cef.misc.StringRef;
import org.cef.network.CefRequest;
import org.cef.network.CefResponse;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.UUID;

public class WDScheme implements CefResourceHandler {

    private static final String ERROR_PAGE = "<!DOCTYPE html><html><head></head><body><h1>%d %s</h1><hr /><i>Miniserv powered by WebDisplays</i></body></html>";
    private ClientTaskGetFile task;
    private boolean isErrorPage;

    String url;

    public WDScheme(String url) {
        this.url = url;
    }

    @Override
    public boolean processRequest(CefRequest cefRequest, CefCallback cefCallback) {
        url = cefRequest.getURL();

        int pos = url.indexOf('/');
        if(pos < 0)
            return false;

        String uuidStr = url.substring(0, pos);
        String fileStr = url.substring(pos + 1);

        try {
            fileStr = URLDecoder.decode(fileStr, "UTF-8");
        } catch(UnsupportedEncodingException ex) {
            Log.warningEx("UTF-8 isn't supported... yeah... and I'm a billionaire...", ex);
        }

        if(uuidStr.isEmpty() || Util.isFileNameInvalid(fileStr))
            return false;

        UUID uuid;
        try {
            uuid = UUID.fromString(uuidStr);
        } catch(IllegalArgumentException ex) {
            return false; //Invalid UUID
        }

        task = new ClientTaskGetFile(uuid, fileStr);
        return Client.getInstance().addTask(task) ? true : false;
    }

    @Override
    public void getResponseHeaders(CefResponse cefResponse, IntRef intRef, StringRef stringRef) {
        Log.info("Waiting for response...");
        int status = task.waitForResponse();
        Log.info("Got response %d", status);

        if(status == 0) {
            //OK
            int extPos = task.getFileName().lastIndexOf('.');
            if(extPos >= 0) {
                String mime = mapMime(task.getFileName().substring(extPos + 1));

                if(mime != null)
                    cefResponse.setMimeType(mime);
            }

            cefResponse.setStatus(200);
            cefResponse.setStatusText("OK");
            cefResponse.setHeaderByName("content-length", "" + -1, true);
            return;
        }

        int errCode;
        String errStr;

        if(status == Constants.GETF_STATUS_NOT_FOUND) {
            errCode = 404;
            errStr = "Not Found";
        } else {
            errCode = 500;
            errStr = "Internal Server Error";
        }

        cefResponse.setStatus(errCode);
        cefResponse.setStatusText(errStr);

        try {
            dataToWrite = String.format(ERROR_PAGE, errCode, errStr).getBytes("UTF-8");
            dataOffset = 0;
            amountToWrite = dataToWrite.length;
            isErrorPage = true;
            cefResponse.setHeaderByName("content-length", "" + amountToWrite, true);
        } catch(UnsupportedEncodingException ex) {
            cefResponse.setHeaderByName("content-length", "" + 0, true);
//            cefResponse.setResponseLength(0);
        }
    }

    private byte[] dataToWrite;
    private int dataOffset;
    private int amountToWrite;

    @Override
    public boolean readResponse(byte[] bytes, int i, IntRef intRef, CefCallback cefCallback) {
        if(dataToWrite == null) {
            if(isErrorPage) {
//                data.setAmountRead(0);
                return false;
            }

            dataToWrite = task.waitForData();
            dataOffset = 3; //packet ID + size
            amountToWrite = task.getDataLength();

            if(amountToWrite <= 0) {
                dataToWrite = null;
//                data.setAmountRead(0);
                return false;
            }
        }

//        int toWrite = data.getBytesToRead();
//        if(toWrite > amountToWrite)
//            toWrite = amountToWrite;

//        System.arraycopy(dataToWrite, dataOffset, data.getDataArray(), 0, toWrite);
//        data.setAmountRead(toWrite);

//        dataOffset += toWrite;
//        amountToWrite -= toWrite;

        if(amountToWrite <= 0) {
            if(!isErrorPage)
                task.nextData();

            dataToWrite = null;
        }

        return true;
    }

    @Override
    public void cancel() {
    }

    public static String mapMime(String ext) {
        switch (ext) {
            case "htm":
            case "html":
                return "text/html";

            case "css":
                return "text/css";

            case "js":
                return "text/javascript";

            case "png":
                return "image/png";

            case "jpg":
            case "jpeg":
                return "image/jpeg";

            case "gif":
                return "image/gif";

            case "svg":
                return "image/svg+xml";

            case "xml":
                return "text/xml";

            case "txt":
                return "text/plain";

            default:
                return null;
        }
    }
}
