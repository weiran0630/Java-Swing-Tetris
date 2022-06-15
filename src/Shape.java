import java.util.Random;

/**
 * Class Shape: Provides info about a Tetris piece.
 */
public class Shape {
    // holds seven Tetris shape names and the empty shape called NoShape.
    protected enum Tetrominoe {
        NoShape, ZShape, SShape, LineShape,
        TShape, SquareShape, LShape, MirroredLShape
    }

    private Tetrominoe pieceShape;
    private int[][] coords;

    public Shape() {
        // holds the actual coordinates of a Tetris piece.
        this.coords = new int[4][2];
        this.setShape(Tetrominoe.NoShape);
    }

    void setShape(Tetrominoe shape) {
        // holds all possible coordinate values of the Tetris pieces.
        // This is a template from which all pieces take their coordinate values.
        int[][][] coordsTable = new int[][][]{
                {{0, 0}, {0, 0}, {0, 0}, {0, 0}}, // NoShape
                {{0, -1}, {0, 0}, {-1, 0}, {-1, 1}}, // ZShape
                {{0, -1}, {0, 0}, {1, 0}, {1, 1}}, // SShape
                {{0, -1}, {0, 0}, {0, 1}, {0, 2}}, // LineShape
                {{-1, 0}, {0, 0}, {1, 0}, {0, 1}}, // TShape
                {{0, 0}, {1, 0}, {0, 1}, {1, 1}}, // SquareShape
                {{-1, -1}, {0, -1}, {0, 0}, {0, 1}}, // LShape
                {{1, -1}, {0, -1}, {0, 0}, {0, 1}} // MirroredLShape
        };

        for (int i = 0; i < 4; i++) {
            // ordinal() method returns the current position of the enum type in the enum object.
            System.arraycopy(coordsTable[shape.ordinal()], 0, coords, 0, 4);
        }

        pieceShape = shape;
    }

    /**
     *  Helper functions to set or get the X/Y coordinate, respectively.
     */
    private void setX(int index, int x) {
        coords[index][0] = x;
    }

    private void setY(int index, int y) {
        coords[index][1] = y;
    }

    int x(int index) {
        return coords[index][0];
    }

    int y(int index) {
        return coords[index][1];
    }

    public int minX() {
        int m = coords[0][0];

        for (int i = 0; i < 4; i++) {
            m = Math.min(m, coords[i][0]);
        }

        return m;
    }

    int minY() {
        int m = coords[0][1];

        for (int i = 0; i < 4; i++) {
            m = Math.min(m, coords[i][1]);
        }

        return m;
    }

    Tetrominoe getShape() {
        return pieceShape;
    }

    void setRandomShape() {
        var r = new Random();
        int x = Math.abs(r.nextInt()) % 7 + 1;

        Tetrominoe[] values = Tetrominoe.values();
        setShape(values[x]);
    }

    /**
     * Rotation
     */
    // Rotates a piece to the left. The square does not have to be rotated, thus return the reference to the current object.
    Shape rotateLeft() {
        if (pieceShape == Tetrominoe.SquareShape) {
            return this;
        }

        var result = new Shape();
        result.pieceShape = pieceShape;

        for (int i = 0; i < 4; i++) {
            result.setX(i, y(i));
            result.setY(i, -x(i));
        }

        return result;
    }

    // Do the same thing rotateLeft() do, but instead rotate a piece to the right.
    Shape rotateRight() {
        if (pieceShape == Tetrominoe.SquareShape) {
            return this;
        }

        var result = new Shape();
        result.pieceShape = pieceShape;

        for (int i = 0; i < 4; i++) {
            result.setX(i, -y(i));
            result.setY(i, x(i));
        }

        return result;
    }
}
