package Serealization;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 *
 * @author tiya
 */
public class Serialization {
    public static void serializeToFile(Object obj, String filename) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(
            new FileOutputStream(filename))) {
            oos.writeObject(obj);
        }
    }

    public static Object deserializeFromFile(String filename) 
            throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(
             new FileInputStream(filename))) {
            return ois.readObject();
        }
    }
    
}
