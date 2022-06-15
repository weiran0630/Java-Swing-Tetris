import javax.swing.*;
import java.awt.*;

/**
 * Set up the game; Create a board which player play the game
 * Originally created by: https://zetcode.com
 * Modified by: 07360931 朱瑋然 (Date: 2022061
 */
public class Tetris extends JFrame {
    private JLabel statusbar;

    public Tetris() {
        this.init();
    }

    private void init() {
        this.statusbar = new JLabel(" Score: 0"); // Create a statusbar.
        this.add(this.statusbar, BorderLayout.SOUTH);

        // Create and add the board to the container.
        var board = new Board(this);
        this.add(board);
        board.start(); // Starts the game.

        this.setTitle("Tetris");
        this.setSize(200, 400);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
    }

    /**
     * Getter of the statusbar, used in Board Class.
     * @return statusbar JLabel
     */
    JLabel getStatusBar() {
        return this.statusbar;
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new Tetris().setVisible(true));
    }
}