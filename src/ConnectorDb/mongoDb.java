package ConnectorDb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class mongoDb {
    private static final String CONNECTION_STRING = "mongodb://localhost:27017";
    private static final String DATABASE_NAME = "belanjaDb";
    private static MongoClient mongoClient = null;

    public static MongoDatabase getDatabase() {
        try {
            if (mongoClient == null) {
                mongoClient = MongoClients.create(CONNECTION_STRING);
                System.out.println("Berhasil terhubung ke MongoDB");
            }
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            // Verifikasi koneksi dengan ping
            database.runCommand(new Document("ping", 1));
            return database;
        } catch (Exception e) {
            System.err.println("Gagal terhubung ke MongoDB: " + e.getMessage());
            return null;
        }
    }

    public static void close() {
        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
            System.out.println("Koneksi MongoDB ditutup");
        }
    }
}