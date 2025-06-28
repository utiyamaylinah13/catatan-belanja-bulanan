package database;
import java.sql.*;

public class RelationalManager {
    private Connection conn;

    public RelationalManager(String url) throws SQLException {
        conn = DriverManager.getConnection(url);
    }

    public void insertItem(String nama, double harga, int jumlah) throws SQLException {
        String sql = "INSERT INTO belanja (nama, harga, jumlah) VALUES (?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, nama);
        stmt.setDouble(2, harga);
        stmt.setInt(3, jumlah);
        stmt.executeUpdate();
    }

    // Tambahkan read/update/delete
}
