import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Fantasy Survival: Emoji Edition");
        GamePanel panel = new GamePanel();
        
        frame.add(panel);
        frame.setSize(1024, 768); // Rezolutie mai mare
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}