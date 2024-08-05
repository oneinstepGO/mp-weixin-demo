package com.oneinstep.myspi.core.inject;

import com.oneinstep.myspi.core.ExtensionLoader;
import com.oneinstep.myspi.core.SPI;

/**
 * SPI 扩展注入器
 */
public class SpiExtensionInjector implements ExtensionInjector {

    @Override
    public <T> T getInstance(final Class<T> type, final String name) {
        if (!type.isInterface() || !type.isAnnotationPresent(SPI.class)) {
            return null;
        }
        ExtensionLoader<T> loader = ExtensionLoader.getExtensionLoader(type);
        if (loader == null) {
            return null;
        }
        if (!loader.getSupportedExtensions().isEmpty()) {
            return loader.getAdaptiveExtension();
        }
        return null;
    }

}
