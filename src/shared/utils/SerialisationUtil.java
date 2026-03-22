package shared.utils;

import java.io.*;

public class SerialisationUtil {
    public static byte[] serialize(Object object) throws IOException{
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)){
            objectOutputStream.writeObject(object);
            objectOutputStream.flush();
        }
        return byteArrayOutputStream.toByteArray();
    }
    public static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException{
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes)){
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            return objectInputStream.readObject();
        }
    }
}
