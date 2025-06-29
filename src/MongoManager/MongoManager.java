package mongomanager;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import org.bson.Document;

public class MongoManager {

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> users;

    // Konstruktor koneksi MongoDB
    public MongoManager(String uri, String dbName) {
        MongoClient MongoClient1 = mongoClient;
        MongoClient1 = MongoClients.create("mongodb://localhost:27017");
        database = MongoClient1.getDatabase("belanjakuDb");
        users = database.getCollection("users");
    }

    // Fungsi login: cek username dan password (hashed)
    public boolean login(String username, String hashedPassword) {
        Document user = users.find(
                Filters.and(
                        Filters.eq("username", username),
                        Filters.eq("password", hashedPassword)
                )
        ).first();
        return false;

    }

    // Ambil nama berdasarkan username
    public String getNama(String username) {
        Document user = users.find(Filters.eq("username", username)).first();
        return user != null ? user.getString("nama") : null;
    }

    // Ambil fullname berdasarkan username (jika digunakan)
    public String getFullName(String username) {
        Document user = users.find(Filters.eq("username", username)).first();
        return user != null ? user.getString("fullname") : null;
    }

    // Registrasi user (versi sederhana)
    public boolean registerUser(String username, String hashedPassword, String nama) {
        Document existing = users.find(Filters.eq("username", username)).first();
        if (existing != null) {
            return false; // Username sudah dipakai
        }

        Document newUser = new Document("username", username)
                .append("password", hashedPassword)
                .append("nama", nama)
                .append("fullname", nama); // default fullname = nama jika tidak dimasukkan terpisah

        users.insertOne(newUser);
        return true;
    }

    // Registrasi user pakai dokumen (versi fleksibel)
    public boolean registerUserDocument(Document userDoc) {
        String username = userDoc.getString("username");
        Document existing = users.find(Filters.eq("username", username)).first();
        if (existing != null) {
            return false; // Username sudah ada
        }
        users.insertOne(userDoc);
        return true;
    }

    // Tutup koneksi MongoDB
    public void close() {
        mongoClient.close();
    }
}
