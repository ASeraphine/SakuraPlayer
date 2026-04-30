import javax.swing.*;

public class App {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run(){
                MusicPlayerGUI gui = new MusicPlayerGUI();
                gui.setAlwaysOnTop(true);
                gui.setVisible(true);
            }
        });
    }
}