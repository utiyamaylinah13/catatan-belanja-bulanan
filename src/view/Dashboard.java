package view;

import MongoManager.MongoManager;
import Serealization.Serialization;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import internationalization.configLanguange;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import model.Belanjaitem;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 *
 * @author tiya
 */
public class Dashboard extends javax.swing.JFrame {

    private String fullname;
    private ResourceBundle bundle = configLanguange.getInstance().getBundle();
    private MongoCollection<Document> collection;
    private ArrayList<Belanjaitem<String>> listBelanja = new ArrayList<>();
    private DefaultTableModel model;
    private Belanjaitem<String> itemToUpdate;
    private String currentItemName;

    public Dashboard() {
        initComponents();
        updateLanguage();
        String[] kolom = {"Nama Item", "Jumlah", "Harga", "Total Harga", "Tanggal"};
        model = new DefaultTableModel(kolom, 0);
        tabUtama.setModel(model);
        loadData(); // Panggil method loadData yang baru
        startClock();
    }

    private void startClock() {
        Timer timer = new Timer(1000, e -> {
            Date now = new Date();
            SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");

            String currentTime = sdfTime.format(now);

            jLabel1.setText(currentTime);
        });
        timer.start();
    }
        
    private void loadData() {
        System.out.println("Memulai proses load data...");

        try {
            MongoManager mongo = new MongoManager();
            MongoDatabase db = mongo.getDatabase();

            if (db == null) {
                throw new Exception("Koneksi database null");
            }

            // Cek jika collection belum ada, buat baru
            if (!collectionExists(db, "belanja")) {
                db.createCollection("belanja");
                System.out.println("Collection 'belanja' dibuat baru");
            }

            MongoCollection<Document> collection = db.getCollection("belanja");
            FindIterable<Document> docs = collection.find();

            listBelanja.clear();
            model.setRowCount(0);

            int count = 0;
            for (Document doc : docs) {
                try {
                    Belanjaitem<String> item = Belanjaitem.fromDocument(doc);
                    listBelanja.add(item);

                    model.addRow(new Object[]{
                        item.getNamaItem(),
                        item.getQuantity(),
                        item.getPrice(),
                        item.getTotalPrice(),
                        item.getTanggal()
                    });
                    count++;
                } catch (Exception e) {
                    System.err.println("Error parsing dokumen: " + e.getMessage());
                }
            }
            System.out.println("Berhasil load " + count + " item dari MongoDB");

            // Simpan backup ke file
            Serialization.serializeToFile(listBelanja, "belanja.ser");

        } catch (Exception e) {
            System.err.println("Gagal load dari MongoDB: " + e.getMessage());
            e.printStackTrace();

            // Jika MongoDB error, load dari file backup
            loadSerializedData();
        }
    }

// Method bantu untuk cek collection
    private boolean collectionExists(MongoDatabase db, String collectionName) {
        for (String name : db.listCollectionNames()) {
            if (name.equalsIgnoreCase(collectionName)) {
                return true;
            }
        }
        return false;
    }

// Update method simpanDataKeMongoDanFile()
    private void simpanDataKeMongoDanFile(Belanjaitem<String> item) {
        try {
            // 1. Simpan ke MongoDB
            MongoManager mongo = new MongoManager();
            boolean success = mongo.insertItem("belanja", item.toDocument()); // Perhatikan perubahan di sini

            if (!success) {
                throw new Exception("Gagal menyimpan ke MongoDB");
            }

            // 2. Tambahkan ke list dan serialisasi
            listBelanja.add(item);
            Serialization.serializeToFile(listBelanja, "belanja.ser");

            // 3. Update tabel
            model.addRow(new Object[]{
                item.getNamaItem(),
                item.getQuantity(),
                item.getPrice(),
                item.getTotalPrice(),
                item.getTanggal()
            });

            JOptionPane.showMessageDialog(this, "Data berhasil disimpan!", "Sukses",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void setUserLogin(String fullname) {
        this.fullname = fullname;
        lblNameUSer.setText(fullname);
    }

    private void updateLanguage() {
        bundle = configLanguange.getInstance().getBundle();

        lblSelamatdatang.setText(bundle.getString("welcome.title"));
        lbLNameitem.setText(bundle.getString("label.barang"));
        lblDate.setText(bundle.getString("label.tanggal"));
        lblPrice.setText(bundle.getString("label.harga"));
        lblQuantity.setText(bundle.getString("label.jumlah"));
        lblTotalprice.setText(bundle.getString("label.jumlahharga"));
        btnAdd.setText(bundle.getString("button.tambah"));
        btnDelet.setText(bundle.getString("button.hapus"));
        btnLogout.setText(bundle.getString("button.keluar"));
        btnSave.setText(bundle.getString("button.save"));
        lblAddshopping.setText(bundle.getString("label.shopping"));
        btnupdate.setText(bundle.getString("button.edit"));
        lblDate1.setText(bundle.getString("label.tanggal"));
        lblNameitem1.setText(bundle.getString("label.barang"));
        lblQuantity1.setText(bundle.getString("label.jumlah"));
        lblPrice1.setText(bundle.getString("label.harga"));
        lblTotalprice1.setText(bundle.getString("label.jumlahharga"));
        lblEditshopping.setText(bundle.getString("label.edit"));

    }

    private void loadSerializedData() {
        System.out.println("Mencoba load dari file backup...");
        File file = new File("belanja.ser");

        if (!file.exists()) {
            System.out.println("File backup tidak ditemukan");
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            ArrayList<Belanjaitem<String>> loadedData = (ArrayList<Belanjaitem<String>>) ois.readObject();

            listBelanja.clear();
            listBelanja.addAll(loadedData);

            model.setRowCount(0);
            for (Belanjaitem<String> item : loadedData) {
                model.addRow(new Object[]{
                    item.getNamaItem(),
                    item.getQuantity(),
                    item.getPrice(),
                    item.getTotalPrice(),
                    item.getTanggal()
                });
            }
            System.out.println("Berhasil load " + loadedData.size() + " item dari backup");
        } catch (Exception e) {
            System.err.println("Gagal load dari file backup: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Gagal memuat data dari semua sumber",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

//    private void loadDataToTable() {
//        model.setRowCount(0); // clear existing data
//
//        try {
//            MongoManager mongo = new MongoManager();
//            MongoCollection<Document> collection = mongo.getCollection("belanja");
//
//            for (Document doc : collection.find()) {
//                String tanggal = doc.getString("date");
//                String namaItem = doc.getString("nameitem");
//
//                // Gunakan getInteger() untuk nilai yang disimpan sebagai integer
//                int quantity = doc.getInteger("quantity");
//                int price = doc.getInteger("price");
//                int total = doc.getInteger("totalprice");
//
//                model.addRow(new Object[]{
//                    namaItem,
//                    quantity,
//                    price,
//                    total,
//                    tanggal
//                });
//            }
//        } catch (Exception e) {
//            JOptionPane.showMessageDialog(this,
//                    "Gagal memuat data: " + e.getMessage(),
//                    "Error",
//                    JOptionPane.ERROR_MESSAGE);
//            e.printStackTrace();
//        }
//    }
//
//    private void simpanData() {
//        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("belanja.ser"))) {
//            // Tulis listBelanja yang sebenarnya, bukan null
//            oos.writeObject(listBelanja);
//            System.out.println("Data berhasil diserialisasi ke belanja.ser");
//        } catch (IOException e) {
//            e.printStackTrace();
//            JOptionPane.showMessageDialog(this,
//                    "Gagal menyimpan data ke file: " + e.getMessage(),
//                    "Error",
//                    JOptionPane.ERROR_MESSAGE);
//        }
//    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        Tambah = new javax.swing.JDialog();
        jPanel2 = new javax.swing.JPanel();
        lblAddshopping = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        lblDate = new javax.swing.JLabel();
        lbLNameitem = new javax.swing.JLabel();
        txtNameitem = new javax.swing.JTextField();
        lblQuantity = new javax.swing.JLabel();
        txtQuantity = new javax.swing.JTextField();
        lblPrice = new javax.swing.JLabel();
        txtPrice = new javax.swing.JTextField();
        lblTotalprice = new javax.swing.JLabel();
        txtTotalprice = new javax.swing.JTextField();
        btnSave = new javax.swing.JButton();
        txtDate = new com.toedter.calendar.JDateChooser();
        Edit = new javax.swing.JDialog();
        jPanel3 = new javax.swing.JPanel();
        lblEditshopping = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        lblDate1 = new javax.swing.JLabel();
        lblNameitem1 = new javax.swing.JLabel();
        txtNameitem1 = new javax.swing.JTextField();
        lblQuantity1 = new javax.swing.JLabel();
        textQuantity1 = new javax.swing.JTextField();
        lblPrice1 = new javax.swing.JLabel();
        txtPrice1 = new javax.swing.JTextField();
        lblTotalprice1 = new javax.swing.JLabel();
        txtTotalprice1 = new javax.swing.JTextField();
        btnSave1 = new javax.swing.JButton();
        txtDate1 = new com.toedter.calendar.JDateChooser();
        jPanel1 = new javax.swing.JPanel();
        lblSelamatdatang = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabUtama = new javax.swing.JTable();
        btnAdd = new javax.swing.JButton();
        btnDelet = new javax.swing.JButton();
        btnLogout = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        btnupdate = new javax.swing.JButton();
        lblNameUSer = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        jPanel2.setBackground(new java.awt.Color(255, 204, 255));

        lblAddshopping.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 18)); // NOI18N
        lblAddshopping.setText("Add shopping items");

        jSeparator1.setForeground(new java.awt.Color(204, 204, 204));

        lblDate.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 14)); // NOI18N
        lblDate.setText("Date");

        lbLNameitem.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 14)); // NOI18N
        lbLNameitem.setText("Name Items");

        txtNameitem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNameitemActionPerformed(evt);
            }
        });

        lblQuantity.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 14)); // NOI18N
        lblQuantity.setText("Quantity");

        txtQuantity.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtQuantityActionPerformed(evt);
            }
        });

        lblPrice.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 14)); // NOI18N
        lblPrice.setText("Price");

        txtPrice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPriceActionPerformed(evt);
            }
        });

        lblTotalprice.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 14)); // NOI18N
        lblTotalprice.setText("Total Price");

        txtTotalprice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtTotalpriceActionPerformed(evt);
            }
        });

        btnSave.setBackground(new java.awt.Color(153, 153, 255));
        btnSave.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 14)); // NOI18N
        btnSave.setText("Save");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(38, 38, 38)
                        .addComponent(lblAddshopping))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(btnSave)
                            .addComponent(lblTotalprice, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblPrice, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblQuantity, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblDate, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lbLNameitem, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtNameitem, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtQuantity, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtPrice, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtTotalprice, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtDate, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE))))
                .addContainerGap(25, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblAddshopping)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblDate)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtDate, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lbLNameitem)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtNameitem, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblQuantity)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtQuantity, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lblPrice)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lblTotalprice)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtTotalprice, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnSave)
                .addContainerGap(18, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout TambahLayout = new javax.swing.GroupLayout(Tambah.getContentPane());
        Tambah.getContentPane().setLayout(TambahLayout);
        TambahLayout.setHorizontalGroup(
            TambahLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        TambahLayout.setVerticalGroup(
            TambahLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(TambahLayout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jPanel3.setBackground(new java.awt.Color(255, 204, 255));

        lblEditshopping.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 18)); // NOI18N
        lblEditshopping.setText("Edit Shopping list");

        jSeparator2.setForeground(new java.awt.Color(153, 153, 153));

        lblDate1.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 14)); // NOI18N
        lblDate1.setText("Date");

        lblNameitem1.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 14)); // NOI18N
        lblNameitem1.setText("Name Items");

        txtNameitem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNameitem1ActionPerformed(evt);
            }
        });

        lblQuantity1.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 14)); // NOI18N
        lblQuantity1.setText("Quantity");

        lblPrice1.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 14)); // NOI18N
        lblPrice1.setText("Price");

        lblTotalprice1.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 14)); // NOI18N
        lblTotalprice1.setText("Total Price");

        btnSave1.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 14)); // NOI18N
        btnSave1.setText("Save");
        btnSave1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSave1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(txtDate1, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnSave1)
                            .addComponent(txtTotalprice1, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtPrice1, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(textQuantity1, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtNameitem1, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblTotalprice1)
                                    .addComponent(lblQuantity1)
                                    .addComponent(lblDate1)
                                    .addComponent(lblNameitem1)
                                    .addComponent(lblPrice1))
                                .addGap(159, 159, 159))))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(59, 59, 59)
                        .addComponent(lblEditshopping, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(21, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblEditshopping)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblDate1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtDate1, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15)
                .addComponent(lblNameitem1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtNameitem1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lblQuantity1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textQuantity1, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24)
                .addComponent(lblPrice1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtPrice1, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lblTotalprice1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtTotalprice1, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnSave1)
                .addContainerGap(13, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout EditLayout = new javax.swing.GroupLayout(Edit.getContentPane());
        Edit.getContentPane().setLayout(EditLayout);
        EditLayout.setHorizontalGroup(
            EditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        EditLayout.setVerticalGroup(
            EditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(EditLayout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 204, 255));

        lblSelamatdatang.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 18)); // NOI18N
        lblSelamatdatang.setText("Selamat Datang,");

        tabUtama.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4", "Title 5"
            }
        ));
        tabUtama.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tabUtamaMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tabUtama);

        btnAdd.setBackground(new java.awt.Color(102, 153, 255));
        btnAdd.setText("Add");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        btnDelet.setBackground(new java.awt.Color(204, 0, 0));
        btnDelet.setText("Delete");
        btnDelet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeletActionPerformed(evt);
            }
        });

        btnLogout.setText("Logout");
        btnLogout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogoutActionPerformed(evt);
            }
        });

        jLabel4.setIcon(new javax.swing.ImageIcon("E:\\DOWNLOAD\\WhatsApp_Image_2025-06-29_at_22.15.02-removebg-preview.png")); // NOI18N

        btnupdate.setBackground(new java.awt.Color(102, 153, 255));
        btnupdate.setText("Update");
        btnupdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnupdateActionPerformed(evt);
            }
        });

        lblNameUSer.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 18)); // NOI18N
        lblNameUSer.setText("User");

        jLabel1.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        jLabel1.setText("HH:MM:SS");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(btnAdd)
                                .addGap(27, 27, 27)
                                .addComponent(btnupdate)
                                .addGap(26, 26, 26)
                                .addComponent(btnDelet)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(lblSelamatdatang)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblNameUSer)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addGap(44, 44, 44))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(btnLogout)
                                .addContainerGap())))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSelamatdatang)
                    .addComponent(lblNameUSer))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 43, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnupdate)
                    .addComponent(btnDelet)
                    .addComponent(btnAdd))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnLogout)
                .addGap(12, 12, 12))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        Tambah.pack();
        Tambah.setLocationRelativeTo(this);
        Tambah.setModal(true);
        Tambah.setVisible(true);
    }//GEN-LAST:event_btnAddActionPerformed

    private void txtTotalpriceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTotalpriceActionPerformed
        try {
            int totalPrice = Integer.parseInt(txtTotalprice.getText());
            int price = Integer.parseInt(txtPrice.getText());

            if (price <= 0) {
                JOptionPane.showMessageDialog(this, "Harga tidak valid!", "Kesalahan", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int quantity = totalPrice / price;
            txtQuantity.setText(String.valueOf(quantity));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Total harga harus berupa angka!", "Kesalahan", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_txtTotalpriceActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        try {
            // Validasi input
            if (txtNameitem.getText().trim().isEmpty()
                    || txtQuantity.getText().trim().isEmpty()
                    || txtPrice.getText().trim().isEmpty()
                    || txtDate.getDate() == null) {

                JOptionPane.showMessageDialog(Tambah,
                        "Harap isi semua field!",
                        "Peringatan",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Parse data
            String namaItem = txtNameitem.getText().trim();
            int quantity = Integer.parseInt(txtQuantity.getText().trim());
            int price = Integer.parseInt(txtPrice.getText().trim());
            int totalPrice = quantity * price;
            String tanggal = new SimpleDateFormat("yyyy-MM-dd").format(txtDate.getDate());

            // Buat objek Belanjaitem
            Belanjaitem<String> item = new Belanjaitem<>(
                    namaItem,
                    String.valueOf(quantity),
                    String.valueOf(price),
                    String.valueOf(totalPrice),
                    tanggal
            );

            // Simpan ke MongoDB dan file
            simpanDataKeMongoDanFile(item);

            // Bersihkan form
            txtNameitem.setText("");
            txtQuantity.setText("");
            txtPrice.setText("");
            txtTotalprice.setText("");
            txtDate.setDate(null);
            Tambah.dispose();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(Tambah,
                    "Quantity dan Price harus berupa angka!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(Tambah,
                    "Error: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnSaveActionPerformed

    private void txtNameitem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNameitem1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNameitem1ActionPerformed

    private void btnupdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnupdateActionPerformed
        int selectedRow = tabUtama.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Silakan pilih item yang akan diupdate");
            return;
        }

        // Ambil data dari baris yang dipilih
        currentItemName = tabUtama.getValueAt(selectedRow, 0).toString();
        String quantity = tabUtama.getValueAt(selectedRow, 1).toString();
        String price = tabUtama.getValueAt(selectedRow, 2).toString();
        String totalPrice = tabUtama.getValueAt(selectedRow, 3).toString();
        String date = tabUtama.getValueAt(selectedRow, 4).toString();

        // Buat item untuk diupdate
        itemToUpdate = new Belanjaitem<>(currentItemName, quantity, price, totalPrice, date);

        // Isi dialog edit
        txtNameitem1.setText(currentItemName);
        textQuantity1.setText(quantity);
        txtPrice1.setText(price);
        txtTotalprice1.setText(totalPrice);

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            txtDate1.setDate(sdf.parse(date));
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this, "Error parsing tanggal: " + e.getMessage());
        }

        Edit.pack();
        Edit.setLocationRelativeTo(this);
        Edit.setModal(true);
        Edit.setVisible(true);
    }//GEN-LAST:event_btnupdateActionPerformed

    private void txtNameitemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNameitemActionPerformed

    }//GEN-LAST:event_txtNameitemActionPerformed

    private void txtQuantityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtQuantityActionPerformed
        try {
            int quantity = Integer.parseInt(txtQuantity.getText());
            int price = Integer.parseInt(txtPrice.getText());

            int totalPrice = quantity * price;
            txtTotalprice.setText(String.valueOf(totalPrice));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Masukkan angka yang valid untuk Quantity dan Price!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_txtQuantityActionPerformed

    private void txtPriceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPriceActionPerformed
        try {
            int price = Integer.parseInt(txtPrice.getText());
            int quantity = Integer.parseInt(txtQuantity.getText());

            int totalPrice = price * quantity;
            txtTotalprice.setText(String.valueOf(totalPrice));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Harga dan Jumlah harus berupa angka!", "Kesalahan", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_txtPriceActionPerformed

    private void tabUtamaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tabUtamaMouseClicked

    }//GEN-LAST:event_tabUtamaMouseClicked

    private void btnDeletActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeletActionPerformed
        int selectedRow = tabUtama.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Silakan pilih data yang ingin dihapus!");
            return;
        }

        // Ambil nilai nama item dari baris yang dipilih (kolom 0 adalah nameitem)
        String nameItem = tabUtama.getValueAt(selectedRow, 0).toString();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Yakin ingin menghapus item: " + nameItem + "?",
                "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            MongoManager mongo = new MongoManager();
            MongoCollection<Document> collection = mongo.getDatabase().getCollection("belanja");

            // Hapus dokumen dari MongoDB berdasarkan nameitem
            Document query = new Document("nameitem", nameItem);
            DeleteResult result = collection.deleteOne(query);

            if (result.getDeletedCount() == 1) {
                // Hapus baris dari JTable
                DefaultTableModel model = (DefaultTableModel) tabUtama.getModel();
                model.removeRow(selectedRow);
                JOptionPane.showMessageDialog(this, "Item berhasil dihapus.");
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menghapus item: Data tidak ditemukan di database.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menghapus item: " + e.getMessage());
            e.printStackTrace();
        }
    }//GEN-LAST:event_btnDeletActionPerformed

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutActionPerformed
        int confirm = JOptionPane.showConfirmDialog(this, "Apakah Anda yakin ingin logout?", "Konfirmasi Logout", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // Tutup frame sekarang (Dashboard atau yang aktif)
            dispose();

            // Tampilkan kembali form Login
            new Login().setVisible(true);
        }
    }//GEN-LAST:event_btnLogoutActionPerformed

    private void btnSave1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSave1ActionPerformed
        try {
            // Validasi input
            if (txtNameitem1.getText().isEmpty() || textQuantity1.getText().isEmpty()
                    || txtPrice1.getText().isEmpty() || txtDate1.getDate() == null) {
                JOptionPane.showMessageDialog(Edit, "Harap isi semua field");
                return;
            }

            // Buat item yang sudah diupdate
            String newName = txtNameitem1.getText().trim();
            String quantity = textQuantity1.getText().trim();
            String price = txtPrice1.getText().trim();
            String totalPrice = txtTotalprice1.getText().trim();
            String date = new SimpleDateFormat("yyyy-MM-dd").format(txtDate1.getDate());

            Belanjaitem<String> updatedItem = new Belanjaitem<>(
                    newName, quantity, price, totalPrice, date
            );

            // Update di MongoDB
            MongoManager mongo = new MongoManager();
            Document updateDoc = new Document()
                    .append("nameitem", newName)
                    .append("quantity", Integer.parseInt(quantity))
                    .append("price", Double.parseDouble(price))
                    .append("totalprice", Double.parseDouble(totalPrice))
                    .append("date", date);

            boolean success = mongo.updateItem("belanja", "nameitem", currentItemName, updateDoc);

            if (success) {
                // Update di list lokal dan tabel
                for (int i = 0; i < listBelanja.size(); i++) {
                    if (listBelanja.get(i).getNamaItem().equals(currentItemName)) {
                        listBelanja.set(i, updatedItem);
                        break;
                    }
                }

                // Refresh tabel
                loadData();
                JOptionPane.showMessageDialog(this, "Item berhasil diupdate");
                Edit.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal mengupdate item");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(Edit, "Error: " + e.getMessage());
            e.printStackTrace();
        }
    }//GEN-LAST:event_btnSave1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Dashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Dashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Dashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Dashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Dashboard().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JDialog Edit;
    private javax.swing.JDialog Tambah;
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnDelet;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnSave1;
    private javax.swing.JButton btnupdate;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel lbLNameitem;
    private javax.swing.JLabel lblAddshopping;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblDate1;
    private javax.swing.JLabel lblEditshopping;
    private javax.swing.JLabel lblNameUSer;
    private javax.swing.JLabel lblNameitem1;
    private javax.swing.JLabel lblPrice;
    private javax.swing.JLabel lblPrice1;
    private javax.swing.JLabel lblQuantity;
    private javax.swing.JLabel lblQuantity1;
    private javax.swing.JLabel lblSelamatdatang;
    private javax.swing.JLabel lblTotalprice;
    private javax.swing.JLabel lblTotalprice1;
    private javax.swing.JTable tabUtama;
    private javax.swing.JTextField textQuantity1;
    private com.toedter.calendar.JDateChooser txtDate;
    private com.toedter.calendar.JDateChooser txtDate1;
    private javax.swing.JTextField txtNameitem;
    private javax.swing.JTextField txtNameitem1;
    private javax.swing.JTextField txtPrice;
    private javax.swing.JTextField txtPrice1;
    private javax.swing.JTextField txtQuantity;
    private javax.swing.JTextField txtTotalprice;
    private javax.swing.JTextField txtTotalprice1;
    // End of variables declaration//GEN-END:variables

    private static class riwayat extends JDialog {

        public riwayat(Dashboard aThis, boolean b) {
        }
    }
}
