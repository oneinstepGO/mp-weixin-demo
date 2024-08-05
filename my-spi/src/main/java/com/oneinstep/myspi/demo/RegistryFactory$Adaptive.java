package com.oneinstep.myspi.demo;

import com.oneinstep.myspi.core.ExtensionLoader;

public class RegistryFactory$Adaptive implements com.oneinstep.myspi.demo.RegistryFactory {

    public com.oneinstep.myspi.demo.Registry getRegistry(com.oneinstep.myspi.core.URL arg0)  {
        if (arg0 == null) throw new IllegalArgumentException("url == null");
        com.oneinstep.myspi.core.URL url = arg0;
        String extName = url.getProtocol();
        if(extName == null) throw new IllegalStateException("Failed to get extension (com.oneinstep.myspi.demo.RegistryFactory) name from url (" + url + ") use keys([protocol])");
        com.oneinstep.myspi.demo.RegistryFactory extension = ExtensionLoader.getExtensionLoader(com.oneinstep.myspi.demo.RegistryFactory.class).getExtension(extName);
        return extension.getRegistry(arg0);
    }

}