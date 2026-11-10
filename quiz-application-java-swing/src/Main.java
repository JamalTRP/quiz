/**
 * Main.java
 *
 * The official entry point of the Quiz Application.
 * This class applies the modern System / Nimbus Look and Feel to ensure
 * crisp, native-looking Swing components and launches the LoginFrame safely
 * onto the Swing Event Dispatch Thread (EDT).
 *
 * @author Senior Java Mentor
 * @version 1.0 (Java 17)
 */
import ui.LoginFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        // Run GUI initialization on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                // Set Look and Feel: Try Nimbus first, fallback to System L&F
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception e) {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {
                    // Fall back to default Metal Look and Feel
                }
            }

            // Display the login window
            new LoginFrame().setVisible(true);
        });
    }
}
