package model;


public class item {
    private String nama;
    private T harga;
    private int jumlah;
    
    public item(String nama, T harga, int jumlah){
        this.nama = nama;
        this.harga = harga;
        this.jumlah = jumlah;
    }
    
    public double getTotal(){
        return Double.perDouble(harga.toString())*jumlah;
        
    }
}
