/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.oneinstep.spi.core.utils;


import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 *  classLoader 资源加载器
 */
@Slf4j
public class ClassLoaderResourceLoader {

    private static SoftReference<Map<ClassLoader, Map<String, Set<URL>>>> classLoaderResourcesCache = null;

    public static Map<ClassLoader, Set<URL>> loadResources(String fileName, Collection<ClassLoader> classLoaders)
            throws InterruptedException {
        Map<ClassLoader, Set<URL>> resources = new ConcurrentHashMap<>();
        CountDownLatch countDownLatch = new CountDownLatch(classLoaders.size());
        for (ClassLoader classLoader : classLoaders) {
            resources.put(classLoader, loadResources(fileName, classLoader));
            countDownLatch.countDown();
        }
        countDownLatch.await();
        return Collections.unmodifiableMap(new LinkedHashMap<>(resources));
    }

    public static Set<URL> loadResources(String fileName, ClassLoader currentClassLoader) {
        Map<ClassLoader, Map<String, Set<URL>>> classLoaderCache;
        if (classLoaderResourcesCache == null || (classLoaderCache = classLoaderResourcesCache.get()) == null) {
            synchronized (ClassLoaderResourceLoader.class) {
                if (classLoaderResourcesCache == null || (classLoaderCache = classLoaderResourcesCache.get()) == null) {
                    classLoaderCache = new ConcurrentHashMap<>();
                    classLoaderResourcesCache = new SoftReference<>(classLoaderCache);
                }
            }
        }
        if (!classLoaderCache.containsKey(currentClassLoader)) {
            classLoaderCache.putIfAbsent(currentClassLoader, new ConcurrentHashMap<>());
        }
        Map<String, Set<URL>> urlCache = classLoaderCache.get(currentClassLoader);
        if (!urlCache.containsKey(fileName)) {
            Set<URL> set = new LinkedHashSet<>();
            Enumeration<URL> urls;
            try {
                urls = currentClassLoader.getResources(fileName);
                if (urls != null) {
                    while (urls.hasMoreElements()) {
                        URL url = urls.nextElement();
                        set.add(url);
                    }
                }
            } catch (IOException e) {
                log.error("Failed to load resources from {}", fileName, e);
            }
            urlCache.put(fileName, set);
        }
        return urlCache.get(fileName);
    }

}
