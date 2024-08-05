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
package com.oneinstep.myspi.extend;

import com.oneinstep.myspi.core.URL;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * AbstractRegistryFactory. (SPI, Singleton, ThreadSafe)
 */
@Slf4j
public abstract class AbstractRegistryFactory implements RegistryFactory {

    private static final Lock LOCK = new ReentrantLock();

    @Override
    public Registry getRegistry(URL url) {

        Registry registry;

        // 加锁，保证注册中心访问过程中只有一个实例
        LOCK.lock();
        try {

            // 通过spi/ioc创建注册中心
            registry = createRegistry(url);
            if (registry == null) {
                throw new IllegalStateException("Can not create registry " + url);
            }

        } catch (Exception e) {
            throw new RuntimeException("Can not create registry " + url, e);
        } finally {
            // 释放锁
            LOCK.unlock();
        }

        return registry;
    }

    /**
     * 创建注册中心，由子类实现
     *
     * @param url registry url
     * @return registry
     */
    protected abstract Registry createRegistry(URL url);
}
