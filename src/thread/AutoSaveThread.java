package thread;

public class AutoSaveThread extends Thread {
    @Override
    public void run() {
        while (true) {
            try {
                System.out.println("Auto saving data...");
                // Simulasi autosave (ke file/db)
                Thread.sleep(60000); // tiap 60 detik
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
