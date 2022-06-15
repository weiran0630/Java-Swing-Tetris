import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Board Class: where the game logic is located
 */
public class Board extends JPanel {
    /**
     * CONSTANTS
     */
    // Define the size of the board.
    private final int BOARD_WIDTH = 10;
    private final int BOARD_HEIGHT = 22;

    private final int PERIOD_INTERVAL = 300; // Define the speed of the game.
    private final String urlToBGM = "kirby_bgm.wav"; // Url to the background music.
    /**
     * -------------------------------------------------------------
     */


    private Timer timer;
    private Sound bgm;

    // Determine if the Tetris shape has finished falling and then need to create a new shape.
    private boolean isFallingFinished = false;

    private boolean isPaused = false; // Check if the game is paused.
    private int numLinesRemoved = 0; // Count the lines that player removed.

    // The actual position of the falling Tetris shape.
    private int curX = 0;
    private int curY = 0;

    private JLabel statusbar;
    private Shape curPiece;
    private Shape.Tetrominoe[] board;

    public Board(Tetris parent) {
        this.initBoard(parent);
    }

    private void initBoard(Tetris parent) {
        this.setFocusable(true);
        this.statusbar = parent.getStatusBar();
        this.addKeyListener(new TAdapter());
    }


    /**
     * Determine the width and height of a single Tetrominoe square.
     */
    private int squareWidth() {
        return (int) this.getSize().getWidth() / BOARD_WIDTH;
    }

    private int squareHeight() {
        return (int) this.getSize().getHeight() / BOARD_HEIGHT;
    }
    /**
     *  -------------------------------------------------------------
     */

    /**
     * Determine the shape at the given coordinates. The shape are stored in the board array.
     */
    private Shape.Tetrominoe shapeAt(int x, int y) {
        return this.board[(y * BOARD_WIDTH) + x];
    }

    void start() {
        // Create a new current shape and a new board.
        this.curPiece = new Shape();
        this.board = new Shape.Tetrominoe[BOARD_WIDTH * BOARD_HEIGHT];

        // Clear the board and initialize the new falling piece.
        this.clearBoard();
        this.newPiece();

        // Create a timer, which is executed at PERIOD_INTERVAL intervals, create a game cycle.
        this.timer = new Timer(PERIOD_INTERVAL, new GameCycle());
        this.timer.start();

        this.bgm = new Sound(urlToBGM);
        this.bgm.play();
    }

    /**
     * Pause or resume the game.
     */
    private void pause() {
        this.isPaused = !this.isPaused;

        // If the game is paused, pause the bgm and display the paused message in the statusbar.
        if (this.isPaused) {
            this.bgm.pause();
            this.statusbar.setText(" ---Paused---");
        } else {
            this.bgm.play();
            this.statusbar.setText(String.valueOf(" Score: " + this.numLinesRemoved));
        }

        this.repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        this.doDrawing(g);
    }

    /**
     * Draw all objects on the board
     */
    private void doDrawing(Graphics g) {
        var size = this.getSize();
        int boardTop = (int) size.getHeight() - this.BOARD_HEIGHT * this.squareHeight();

        // Paint all the shapes or remains of the shapes that have been dropped to the bottom of the board.
        for (int i = 0; i < this.BOARD_HEIGHT; i++) {
            for (int j = 0; j < this.BOARD_WIDTH; j++) {
                // All the squares are remembered in the board array. We access it using the shapeAt() method.
                Shape.Tetrominoe shape = this.shapeAt(j, this.BOARD_HEIGHT - i - 1);

                if (shape != Shape.Tetrominoe.NoShape) {
                    this.drawSquare(g, j * this.squareWidth(),
                            boardTop + i * this.squareHeight(), shape);
                }
            }
        }

        // Paint the actual falling piece.
        if (this.curPiece.getShape() != Shape.Tetrominoe.NoShape) {
            for (int i = 0; i < 4; i++) {
                int x = this.curX + this.curPiece.x(i);
                int y = this.curY - this.curPiece.y(i);

                this.drawSquare(g, x * this.squareWidth(),
                        boardTop + (this.BOARD_HEIGHT - y - 1) * this.squareHeight(),
                        this.curPiece.getShape());
            }
        }
    }

    /**
     * Drop the piece
     */
    private void dropDown() {
        if (isPaused) {
            return;
        }

        int newY = this.curY;

        // Try to drop the piece one line down until it reaches the bottom or the top of another fallen Tetris piece.
        while (newY > 0) {
            if (!this.tryMove(this.curPiece, this.curX, newY - 1)) {
                break;
            }

            newY--;
        }

        // When the Tetris piece finishes falling, the pieceDropped() is called.
        this.pieceDropped();
    }

    /**
     * Try to drop the piece one line down.
     */
    private void oneLineDown() {
        if (!this.tryMove(this.curPiece, this.curX, this.curY - 1)) {
            this.pieceDropped();
        }
    }

    /**
     * Fills the board with empty Tetrominoe.NoShape. This is later used at collision detection.
     */
    private void clearBoard() {
        for (int i = 0; i < this.BOARD_HEIGHT * this.BOARD_WIDTH; i++) {
            this.board[i] = Shape.Tetrominoe.NoShape;
        }
    }

    /**
     * Puts the falling piece into the board array;
     * The board array holds all the squares of the pieces and remains of the pieces that has finished falling.
     */
    private void pieceDropped() {
        for (int i = 0; i < 4; i++) {
            int x = this.curX + this.curPiece.x(i);
            int y = this.curY - this.curPiece.y(i);
            this.board[(y * this.BOARD_WIDTH) + x] = this.curPiece.getShape();
        }

        this.removeFullLines(); // Check if we can remove some lines off the board.

        // Try to create a new piece.
        if (!this.isFallingFinished) {
            this.newPiece();
        }
    }

    /**
     * Creates a new Tetris piece.
     */
    private void newPiece() {
        this.curPiece.setRandomShape(); // Get a new random shape.

        // Compute the initial curX and curY values.
        this.curX = this.BOARD_WIDTH / 2 + 1;
        this.curY = this.BOARD_HEIGHT - 1 + this.curPiece.minY();

        // If we cannot move to the initial positions, the game is over (top-out).
        if (!tryMove(curPiece, curX, curY)) {
            this.curPiece.setShape(Shape.Tetrominoe.NoShape);
            this.timer.stop(); // Stop the timer

            this.bgm.stop();

            var msg = String.format(" Game over! Score: %d", numLinesRemoved);
            this.statusbar.setText(msg); // Display game over message and the score on the statusbar.
        }
    }

    /**
     * Try to move the Tetris piece.
     * @param newPiece Shape
     * @param newX int
     * @param newY int
     * @return false if it has reached the board boundaries or it is adjacent to the already fallen Tetris pieces.
     */
    private boolean tryMove(Shape newPiece, int newX, int newY) {
        for (int i = 0; i < 4; i++) {
            int x = newX + newPiece.x(i);
            int y = newY - newPiece.y(i);

            // Out of border.
            if (x < 0 || x >= this.BOARD_WIDTH || y < 0 || y >= this.BOARD_HEIGHT) {
                return false;
            }

            // Adjacent to the already fallen Tetris pieces.
            if (this.shapeAt(x, y) != Shape.Tetrominoe.NoShape) {
                return false;
            }
        }

        this.curPiece = newPiece;
        this.curX = newX;
        this.curY = newY;

        this.repaint();

        return true; // Move successfully.
    }

    /**
     * Check if there is any full row among all rows in the board.
     *
     * If there is at least one full line, remove it. Increase the counter (score).
     */
    private void removeFullLines() {
        int numFullLines = 0;

        // Iterate though rows.
        for (int i = this.BOARD_HEIGHT - 1; i >= 0; i--) {
            boolean lineIsFull = true;

            // Iterate though columns.
            for (int j = 0; j < this.BOARD_WIDTH; j++) {
                // If the column contains any Tetrominoe.NoShape, this row isn't full.
                if (this.shapeAt(j, i) == Shape.Tetrominoe.NoShape) {
                    lineIsFull = false;
                    break;
                }
            }

            if (lineIsFull) {
                numFullLines++;

                // Move all the lines above the full row one line down. (remove the full row)
                for (int k = i; k < this.BOARD_HEIGHT - 1; k++) {
                    for (int j = 0; j < this.BOARD_WIDTH; j++) {
                        this.board[(k * this.BOARD_WIDTH) + j] = this.shapeAt(j, k + 1);
                    }
                }
            }
        }

        if (numFullLines > 0) {
            this.numLinesRemoved += numFullLines; // Increment the score.
            this.statusbar.setText(String.valueOf(" Score: "+ this.numLinesRemoved));
            this.isFallingFinished = true;
            this.curPiece.setShape(Shape.Tetrominoe.NoShape);
        }
    }

    /**
     * Every Tetris piece has four squares. (Defined at Shape.coords)
     * and each of the squares is drawn with this method.
     * @param g Graphics
     * @param x int
     * @param y int
     * @param shape Tetrominoe
     */
    private void drawSquare(Graphics g, int x, int y, Shape.Tetrominoe shape) {
        // Colors corresponding to the Shape.Tetrominoe enum.
        Color[] colors = {new Color(0, 0, 0), new Color(204, 102, 102),
                new Color(102, 204, 102), new Color(102, 102, 204),
                new Color(204, 204, 102), new Color(204, 102, 204),
                new Color(102, 204, 204), new Color(218, 170, 0)
        };

        // Again, ordinal() method returns the current position of the enum type in the enum object.
        var color = colors[shape.ordinal()];

        g.setColor(color);
        g.fillRect(x + 1, y + 1, squareWidth() - 2, squareHeight() - 2);

        // The left and top sides of a square are drawn with a brighter color
        g.setColor(color.brighter());
        g.drawLine(x, y + squareHeight() - 1, x, y);
        g.drawLine(x, y, x + squareWidth() - 1, y);

        // The bottom and right sides are drawn with darker colours.
        g.setColor(color.darker());
        g.drawLine(x + 1, y + squareHeight() - 1,
                x + squareWidth() - 1, y + squareHeight() - 1);
        g.drawLine(x + squareWidth() - 1, y + squareHeight() - 1,
                x + squareWidth() - 1, y + 1);
    }

    /**
     * Call the doGameCycle method and creating a game cycle.
     */
    private class GameCycle implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Board.this.doGameCycle(); // access to the outer class private method.
        }
    }


    private void doGameCycle() {
        this.update();
        this.repaint();
    }

    /**
     * Update represents one step of the game.
     * The falling piece goes one line down or a new piece is created if the previous one has finished falling.
     */
    private void update() {
        if (isPaused) {
            return;
        }

        if (isFallingFinished) {
            isFallingFinished = false;
            newPiece();
        } else {
            oneLineDown();
        }
    }

    /**
     * Checks for key events
     */
    private class TAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (curPiece.getShape() == Shape.Tetrominoe.NoShape) {
                return;
            }

            int keycode = e.getKeyCode();

            // Java 12 switch expressions
            switch (keycode) {
                /**
                 * Bind key events to methods
                 */
                case KeyEvent.VK_P -> pause();
                case KeyEvent.VK_LEFT -> tryMove(curPiece, curX - 1, curY);
                case KeyEvent.VK_RIGHT -> tryMove(curPiece, curX + 1, curY);
                case KeyEvent.VK_DOWN -> tryMove(curPiece.rotateRight(), curX, curY);
                case KeyEvent.VK_UP -> tryMove(curPiece.rotateLeft(), curX, curY);
                case KeyEvent.VK_SPACE -> dropDown();
                case KeyEvent.VK_D -> oneLineDown();
                /**
                 *  -------------------------------------------------------------
                 */
            }
        }
    }
}
