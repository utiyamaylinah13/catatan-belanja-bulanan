package MongoManager;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import ConnectorDb.mongoDb;
import model.Belanjaitem;

public class MongoManager {

    private MongoDatabase db;

    public MongoManager() {
        db = mongoDb.getDatabase();
    }

    public MongoDatabase getDatabase() {
        return db;
    }

    public boolean login(String username, String password) {
        MongoCollection<Document> userCol = db.getCollection("users");
        Document user = userCol.find(new Document("username", username).append("password", password)).first();
        return user != null;
    }

    public boolean registerUserDocument(Document doc) {
        MongoCollection<Document> userCol = db.getCollection("users");
        Document existing = userCol.find(new Document("username", doc.getString("username"))).first();
        if (existing != null) {
            return false;
        }
        userCol.insertOne(doc);
        return true;
    }

    public String getNama(String username) {
        MongoCollection<Document> userCol = db.getCollection("users");
        Document user = userCol.find(new Document("username", username)).first();
        return user != null ? user.getString("fullname") : "";
    }

    // Method generik untuk insert dokumen
    public <T> boolean insertItem(String collectionName, T item) {
        try {
            if (item instanceof Document document) {
                db.getCollection(collectionName).insertOne(document);
            } else if (item instanceof Belanjaitem) {
                db.getCollection(collectionName).insertOne(((Belanjaitem<?>) item).toDocument());
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Method update yang diperbaiki
    public boolean updateItem(String collectionName, String fieldName, Object fieldValue, Document updateData) {
        try {
            MongoCollection<Document> collection = db.getCollection(collectionName);
            Document filter = new Document(fieldName, fieldValue);
            UpdateResult result = collection.updateOne(filter, new Document("$set", updateData));
            return result.getModifiedCount() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Document findItemByName(String NamaItem) {
        try {
            if (db == null) {
                throw new Exception("Database not connected");
            }

            MongoCollection<Document> collection = db.getCollection("belanja");
            if (collection == null) {
                throw new Exception("Collection not found");
            }

            Document query = new Document("nameitem", NamaItem);
            Document result = collection.find(query).first();

            if (result == null) {
                System.out.println("Item tidak ditemukan: " + NamaItem);
            }
            return result;
        } catch (Exception e) {
            System.err.println("Error dalam findItemByName: " + e.getMessage());
            return null;
        }
    }

    public MongoCollection<Document> getCollection(String collectionName) {
        return db.getCollection(collectionName);
    }
}
