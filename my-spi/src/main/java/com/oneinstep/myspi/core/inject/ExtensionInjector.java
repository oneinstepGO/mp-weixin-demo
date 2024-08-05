package com.oneinstep.myspi.core.inject;


/**
 * 扩展点注入器接口
 */
public interface ExtensionInjector {

    /**
     * 获取指定类型和名称的对象实例。
     *
     * @param type object type.
     * @param name object name.
     * @return object instance.
     */
    <T> T getInstance(final Class<T> type, final String name);

}