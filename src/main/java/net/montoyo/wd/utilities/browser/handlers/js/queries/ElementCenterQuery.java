package net.montoyo.wd.utilities.browser.handlers.js.queries;

import com.google.gson.JsonObject;
import net.montoyo.wd.utilities.browser.handlers.js.JSQueryHandler;
import net.montoyo.wd.utilities.browser.handlers.js.Scripts;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefQueryCallback;

public class ElementCenterQuery extends JSQueryHandler {
    boolean exists = false;
    double x, y;
    long start = -1;

    String elementName;

    public ElementCenterQuery(String queryName, String name) {
        super(queryName);
        elementName = name;
    }

    @Override
    public boolean handle(CefBrowser browser, CefFrame frame, JsonObject data, boolean persistent, CefQueryCallback callback) {
        exists = data.getAsJsonPrimitive("exists").getAsBoolean();
        if (exists) {
            x = data.getAsJsonPrimitive("x").getAsDouble() + data.getAsJsonPrimitive("w").getAsDouble() / 2;
            y = data.getAsJsonPrimitive("y").getAsDouble() + data.getAsJsonPrimitive("h").getAsDouble() / 2;
        }

        start = -1;
        callback.success("");
        return true;
    }

    public void dispatch(CefBrowser browser) {
        if (start == -1) {
            browser.executeJavaScript(
                    Scripts.QUERY_ELEMENT
                            .replace("%type%", elementName)
                            .replace("%Type%", name),
                    "CenterQuery",
                    0
            );
            start = System.currentTimeMillis();
        } else {
            long ms = System.currentTimeMillis();
            if (start + 1000 < ms) {
                browser.executeJavaScript(
                        Scripts.QUERY_ELEMENT
                                .replace("%type%", elementName)
                                .replace("%Type%", name),
                        "KeyboardCamera",
                        0
                );
                start = System.currentTimeMillis();
            }
        }
    }

    public boolean hasFocused() {
        return exists;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
