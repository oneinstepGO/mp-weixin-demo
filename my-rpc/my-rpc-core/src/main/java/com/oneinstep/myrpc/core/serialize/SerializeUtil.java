package com.oneinstep.myrpc.core.serialize;

import com.oneinstep.myrpc.core.dto.RpcRequest;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * JDK serialization util
 */
@UtilityClass
@Slf4j
public class SerializeUtil {

    /**
     * Serialize object to byte array
     *
     * @param obj object
     * @return byte array
     */
    public static byte[] serialize(Object obj) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(obj);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            log.error("Failed to serialize object", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Deserialize byte array to object
     *
     * @param bytes byte array
     * @param clazz object class
     * @param <T>   object type
     * @return object
     */
    public static <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
            return clazz.cast(objectInputStream.readObject());
        } catch (IOException | ClassNotFoundException e) {
            log.error("Failed to deserialize byte array to object", e);
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        RpcRequest request = new RpcRequest();
        request.setRequestId("f0ee3c9d-4b63-4a98-b171-8bf68abdb02c");
        request.setClassName("com.oneinstep.myrpc.demo.api.ExampleService");
        request.setMethodName("sayHello");
        request.setParameterTypes(new Class<?>[]{String.class});
        request.setParameters(new Object[]{"World"});

        byte[] serializedData = serialize(request);
        log.info("Serialized Data: {}", serializedData);

        RpcRequest deserializedRequest = deserialize(serializedData, RpcRequest.class);
        log.info("Deserialized Request: {}", deserializedRequest);
    }
}
