import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
// MENGHUBUNGKAN DATABASE PEMESANAN KERETA API
import java.text.ParseException; //untuk menangani kesalahan konversi data String
import java.text.SimpleDateFormat;
import java.sql.*;

public class TicketBookingSystem {
    Queue<String[]> tiketQueue;
    Connection connection;

    public TicketBookingSystem() {
        tiketQueue = new LinkedList<>();
        connection = createDatabaseConnection();
    }
    //    method jdbc koneksi ke database
    private Connection createDatabaseConnection(){
        Connection conn = null;
        Statement stmt = null;

        try {
            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/train_booking", "root", "");
            System.out.println("Connecting successful!");
        } catch (SQLException e) { //untuk error sql
            e.printStackTrace();
        } catch (Exception e) { //untuk umum
            e.printStackTrace();
        } finally { // untuk menutup objek statement setelah digunakan, walaupun tidak digunakan penutupan sumber daya
                    // tindakan ini perlu dilakukan untuk meegah masalah kebocoran.
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException e2) {
            }
        }
        return conn;
    }
    public void enqueueTiket(String[] tiket) {
        tiketQueue.offer(tiket);
    }

    public String[] dequeueTiket() {
        return tiketQueue.poll();
    }

    public boolean isTiketEmpty() {
        try{
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM tiket_bookings");
            if (resultSet.next()){
                int count = resultSet.getInt(1);
                return count == 0;
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return true;
    }
    // menambahkan data pemesanan tiket kereta api kedalam database
    public void menyimpanTiketKeDatabase(String[] tiket) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            java.util.Date parsedDate = dateFormat.parse(tiket[5]);
            Timestamp departureTime = new Timestamp(parsedDate.getTime());

            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO tiket_bookings (id_pemesan, nama_pemesan, kereta_api, kelas, tujuan, waktu) VALUES (?,?,?,?,?,?)"
            );
            statement.setInt(1, Integer.parseInt(tiket[0]));
            statement.setString(2, tiket[1]);
            statement.setString(3, tiket[2]);
            statement.setString(4,tiket[3]);
            statement.setString(5, tiket[4]);
            statement.setTimestamp(6,departureTime);
            statement.executeUpdate(); //simpan ke table
        } catch (SQLException e) {
            e.printStackTrace();
        }catch (ParseException e){
            e.printStackTrace();
        }
    }

    public void bookTicket() {
        Scanner input = new Scanner(System.in);

        System.out.println("==============     PEMESANAN TIKET KERETA API     ================");
        System.out.println("============            STASIUN ROGOJAMPI         ================");
        System.out.println("_________________________________________________________________");
        System.out.println("|    KERETA API    |   KELAS   |    TUJUAN   |  HARGA  |  WAKTU  |");
        System.out.println("_________________________________________________________________");
        System.out.println("| Tawang Alun      | Ekonomi   |  Malang     |  62.000 |   08.25 |");
        System.out.println("| Wijaya Kusuma    | Ekonomi   |  Surabaya   |  52.000 |   09.00 |");
        System.out.println("| Mutiara Timur    | Ekonomi   |  Yogyakarta |  94.000 |   06.30 |");
        System.out.println("| Wijaya Kusuma    | Ekonomi   |  Cilacap    |  88.000 |   07.30 |");
        System.out.println("| Probowangi       | Ekonomi   |  Ketapang   |  41.000 |   06.00 |");
        System.out.println("__________________________________________________________________");

        System.out.print("Masukkan Id: ");
        String id = input.nextLine();
        System.out.print("Masukkan nama pemesan: ");
        String pemesan = input.nextLine();
        System.out.print("Masukkan nama kereta: ");
        String kereta = input.nextLine();
        System.out.print("Masukkan kelas tiket (Eksekutif/Bisnis/Ekonomi): ");
        String jenisTiket = input.nextLine();
        System.out.print("Tujuan : ");
        String tujuan = input.nextLine();
        System.out.print("Waktu Keberangkatan(HH:mm:ss) :");
        String waktu = input.nextLine();

        // menambahkan tiket menggunakan enqueue(poll()).
        enqueueTiket(new String[]{id, pemesan, kereta, jenisTiket, tujuan, waktu});

            String[] tiket = dequeueTiket();
            menyimpanTiketKeDatabase(tiket);
            System.out.println("__________________________________________________________________");
            System.out.println("Berikut rincian pemesan:");
            System.out.println("Id: "+tiket[0]);
            System.out.println("Nama: " + tiket[1]);
            System.out.println("Kereta: " + tiket[2]);
            System.out.println("Kelas: " + tiket[3]);
            System.out.println("Tujuan: " + tiket[4]);
            System.out.println("Waktu: "+ tiket[5]);
    }
//    MEMBATALKAN PEMESANAN TIKET
    public void cancelTiket() {
        Scanner input = new Scanner(System.in);
        if (isTiketEmpty()) {
            System.out.println("Tidak ada tiket yang bisa dibatalkan.");
        }else {
            try {
                PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM tiket_bookings WHERE id_pemesan = ?"
                );
                System.out.print("masukkan id yang ingin dibatalkan: ");
                int id = input.nextInt();
                statement.setInt(1, id);
                statement.executeUpdate();
                System.out.println("tiket dicancel pada id nomor: "+id);
            } catch (SQLException e){
                e.printStackTrace();
            }
        }
    }
    // MENAMPILKAN DATA PARA PEMESAN TIKET DENGAN TEKNIK SORTING.
    public void printPemesan(){
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM tiket_bookings"
            );
            ResultSet rs = statement.executeQuery();

            // Mendapatkan metadata kolom
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Menampilkan nama kolom
            for (int i = 1; i <= columnCount; i++) {
                System.out.print(metaData.getColumnName(i) + "\t");
            }
            System.out.println();

            // Menampilkan data baris per baris
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    System.out.print(rs.getString(i) + "\t");
                }
                System.out.println();
            }
            rs.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    // MENAMBAHKAN METHOD SEARCHING UNTUK MENCARI DATA APAKAH MASIH ADA SISA TIKET
    public void cariPemesan(){
        Scanner input = new Scanner(System.in);
        if (isTiketEmpty()){
            System.out.println("tidak ada data pemesan");
        }else {
            try {
                System.out.print("masukkan nama pemesan: ");
                String nama = input.nextLine();

                PreparedStatement statement = connection.prepareStatement(
                        "SELECT * FROM tiket_bookings WHERE Nama_Pemesan LIKE ?"
                );
                statement.setString(1, "%"+nama+"%");
                ResultSet rs = statement.executeQuery();
                boolean dataDitemukan = false;
                while (rs.next()) {
                    String namaPemesan = rs.getString("Nama_Pemesan");
                    if (nama.equals(namaPemesan)) {
                        System.out.println("Nama Pemesan: " + namaPemesan);
                        System.out.println("Tersedia!");
                        System.out.println("-----------------------");
                        dataDitemukan = true;
                    }
                }
                // Menampilkan pesan jika data tidak ditemukan
                if (!dataDitemukan) {
                    System.out.println("Data tidak ditemukan.");
                }
                rs.close();
                statement.close();
            }catch (SQLException e){
                e.printStackTrace();
            }
        }
    }

// MENAMBAHKAN METHOD SORTING UNTUK MENGURUTKAN DATA PEMESANAN
    public static void main(String[] args) {
        TicketBookingSystem bookingSystem = new TicketBookingSystem();
        Scanner scanner = new Scanner(System.in);
        int choice;
        do {
            System.out.println("\n--- Ticket Booking System ---");
            System.out.println("1. Booking Ticket");
            System.out.println("2. Cancel Ticket");
            System.out.println("3. Daftar Pemesan");
            System.out.println("4. Cari pemesan");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");
            choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    bookingSystem.bookTicket();
                    break;
                case 2:
                    bookingSystem.cancelTiket();
                    break;
                case 3:
                    bookingSystem.printPemesan();
                    break;
                case 4:
                    bookingSystem.cariPemesan();
                    break;
                case 5:
                    System.out.println("terima kasih telah menggunakan layanan kami");
                    System.out.println("semoga perjalanan anda menyenangkan 😊");
                    break;
                default:
                    System.out.println("nomor yang anda inputkan tidak benar, mohon ulangi kembali");
            }
        } while (choice != 5);
    }
}