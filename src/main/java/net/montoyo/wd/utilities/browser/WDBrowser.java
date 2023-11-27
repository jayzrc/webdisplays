package net.montoyo.wd.utilities.browser;

import com.cinemamod.mcef.MCEF;
import net.montoyo.wd.utilities.browser.handlers.js.queries.ElementCenterQuery;
import net.montoyo.wd.utilities.browser.handlers.js.JSQueryHandler;
import org.cef.browser.CefBrowser;

import java.util.HashMap;
import java.util.Map;

public interface WDBrowser {
    static CefBrowser createBrowser(String url, boolean transparent) {
        WDClientBrowser browser = new WDClientBrowser(MCEF.getClient(), url, transparent);
        browser.setCloseAllowed();
        browser.createImmediately();
        registerQueries(browser);
        return browser;
    }

    static void registerQueries(WDBrowser browser) {
        Map<String, JSQueryHandler> handlerMap = browser.queryHandlers();

        JSQueryHandler handler = browser.pointerLock();
        handlerMap.put(handler.getName(), handler);
    }

    HashMap<String, JSQueryHandler> queryHandlers();
    ElementCenterQuery pointerLock();
}
