package model;

import java.io.Serializable;
import org.bson.Document;

public class Belanjaitem<T extends Comparable<T>> implements Serializable {

    private static final long serialVersionUID = 1L;

    private T namaItem;
    private T quantity;
    private T price;
    private T totalPrice;
    private T tanggal;

    public Belanjaitem(T namaItem, T quantity, T price, T totalPrice, T tanggal) {
        this.namaItem = namaItem;
        this.quantity = quantity;
        this.price = price;
        this.totalPrice = totalPrice;
        this.tanggal = tanggal;
    }

    // Getter dan Setter
    public T getNamaItem() {
        return namaItem;
    }

    public void setNamaItem(T namaItem) {
        this.namaItem = namaItem;
    }

    public T getQuantity() {
        return quantity;
    }

    public void setQuantity(T quantity) {
        this.quantity = quantity;
    }

    public T getPrice() {
        return price;
    }

    public void setPrice(T price) {
        this.price = price;
    }

    public T getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(T totalPrice) {
        this.totalPrice = totalPrice;
    }

    public T getTanggal() {
        return tanggal;
    }

    public void setTanggal(T tanggal) {
        this.tanggal = tanggal;
    }

    // Konversi ke Document MongoDB
    public Document toDocument() {
        return new Document()
                .append("nameitem", namaItem.toString())
                .append("quantity", quantity instanceof Number ? quantity : Integer.valueOf(quantity.toString()))
                .append("price", price instanceof Number ? price : Double.valueOf(price.toString()))
                .append("totalprice", totalPrice instanceof Number ? totalPrice : Double.valueOf(totalPrice.toString()))
                .append("date", tanggal.toString());
    }

    // Buat dari Document MongoDB
    public static Belanjaitem<String> fromDocument(Document doc) {
        Object price = doc.get("price");
        Object total = doc.get("totalprice");

        return new Belanjaitem<>(
                doc.getString("nameitem"),
                String.valueOf(doc.getInteger("quantity")),
                price instanceof Integer ? String.valueOf(((Integer) price).doubleValue()) : String.valueOf(price),
                total instanceof Integer ? String.valueOf(((Integer) total).doubleValue()) : String.valueOf(total),
                doc.getString("date")
        );
    }
}
