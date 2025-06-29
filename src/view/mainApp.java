package view;

import database.RelationalManager;
import database.MongoManager;
import model.Item;
import thread.AutoSaveThread;
import util.*;

public class mainApp {
    public static void main(String[] args) throws Exception {
        Localutil.setLocale("id"); // atau "en"

        RelationalManager sql = new RelationalManager("jdbc:sqlite:belanja.db");
        MongoManager mongo = new MongoManager("mongodb://localhost:27017", "belanjaDB", "items");

        AutoSaveThread autoSave = new AutoSaveThread();
        autoSave.start();

        Item<Double> item = new Item<>("Sabun", 5000.0, 2);
        sql.insertItem(item.getNama(), item.getHarga(), item.getJumlah());
        mongo.insert(item.getNama(), item.getHarga(), item.getJumlah());

        String enc = CryptoUtil.encrypt("Data sensitif");
        System.out.println("Encrypted: " + enc);
        System.out.println("Decrypted: " + CryptoUtil.decrypt(enc));

        String hash = HashUtil.hashSHA256("password123");
        System.out.println("Hash: " + hash);

        System.out.println(Localutil.get("title"));
    }
}
