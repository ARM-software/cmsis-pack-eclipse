package com.arm.cmsis.pack.ui.console;

import com.arm.cmsis.pack.error.CmsisConsoleStrategy;
import com.arm.cmsis.pack.error.ICmsisConsole;

/**
 * Strategy for RteConsole
 */
public class RteConsoleStrategy extends CmsisConsoleStrategy {

    @Override
    public ICmsisConsole createDefaultCmsisConsole() {
        return RteConsole.openConsole();
    }

}
