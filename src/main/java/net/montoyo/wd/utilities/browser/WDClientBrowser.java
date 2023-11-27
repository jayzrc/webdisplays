package net.montoyo.wd.utilities.browser;

import com.cinemamod.mcef.MCEFBrowser;
import com.cinemamod.mcef.MCEFClient;
import net.montoyo.wd.utilities.browser.handlers.js.queries.ElementCenterQuery;
import net.montoyo.wd.utilities.browser.handlers.js.JSQueryHandler;

import java.util.HashMap;

public class WDClientBrowser extends MCEFBrowser implements WDBrowser {
    ElementCenterQuery focusedEl = new ElementCenterQuery("ActiveElement", "document.activeElement");
    HashMap<String, JSQueryHandler> handlerHashMap = new HashMap<>();

    public WDClientBrowser(MCEFClient client, String url, boolean transparent) {
        super(client, url, transparent);
    }

    @Override
    public HashMap<String, JSQueryHandler> queryHandlers() {
        return handlerHashMap;
    }

    @Override
    public ElementCenterQuery focusedElement() {
        return focusedEl;
    }
}
