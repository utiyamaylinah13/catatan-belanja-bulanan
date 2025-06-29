package model;

public class Item<T> {

    private String nama;
    private T harga;
    private int jumlah;

    public Item(String nama, T harga, int jumlah) {
        this.nama = nama;
        this.harga = harga;
        this.jumlah = jumlah;
    }

    public double getTotal() {
        return Double.parseDouble(harga.toString()) * jumlah;
    }

    public String getNama() {
        return nama;
    }

    public T getHarga() {
        return harga;
    }

    public int getJumlah() {
        return jumlah;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public void setHarga(T harga) {
        this.harga = harga;
    }

    public void setJumlah(int jumlah) {
        this.jumlah = jumlah;
    }
}
