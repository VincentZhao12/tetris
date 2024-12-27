import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.event.MouseInputListener;

public class Tetris implements ActionListener {
    public static PlayField field;
    public static JFrame frame;
    public static GamePanel panel;
    public static Shape shape;
    public static UI ui;
    public static boolean over;
    public static Timer timer;

    public static void init() {
        field = new PlayField();
        frame = new JFrame("Tetris");
        panel = new GamePanel();
        shape = new Shape();
        ui = new UI();
        timer = new Timer(800,new Tetris());
        over = false;

        frame.addKeyListener(ui);
        frame.add(panel);
        frame.setSize(Constants.PANEL_WIDTH, Constants.PANEL_HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(Constants.RESIZABLE);
        frame.setVisible(true);
    }

    public static void stop() {
        field.clearRows();
        shape = new Shape();
    }

    public static void tick() {
        shape.move(Constants.DOWN);
        panel.repaint();
    }

    public static boolean isGameOver() {
        return over;
    }

    public static void gameOver() {
        over = true;
        timer.stop();
    }

    public static void runIt() {
        init();
        timer.start();
        // while (!isGameOver()) {
        //     tick();
        //     delay(Constants.TICK_LENGTH);
        // }
        panel.repaint();
    }

    public static void delay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        runIt();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        tick();
    }
}

class GamePanel extends JPanel {
    private static final long serialVersionUID = 1L;

    @Override
    public void paintComponent(Graphics g) { 
        for (int i = 0; i < Constants.HEIGHT; i++) {
            for (int j = 0; j < Constants.WIDTH; j++) {
                g.setColor(Tetris.field.getColor(i, j));
                g.fillRect(j * Constants.CELL_WIDTH, i * Constants.CELL_HEIGHT, Constants.CELL_WIDTH,
                        Constants.CELL_HEIGHT);
            }
        }
        g.setColor(Constants.LINE_COLOR);
        for (int i = 0; i < Constants.PANEL_HEIGHT; i += Constants.CELL_HEIGHT)
            g.drawLine(0, i, Constants.PANEL_WIDTH, i);

        for (int i = 0; i < Constants.PANEL_WIDTH; i += Constants.CELL_WIDTH)
            g.drawLine(i, 0, i, Constants.PANEL_HEIGHT);

    }

    /**
     * Repaints the Panel
     */
    public void repaint() {
        super.repaint();
    }
}

class Shape {
    Point[] points;
    int code;

    /**
     * Creates instance of a shape, shape is determined by randomly generated number
     */
    public Shape() {
        int ran = (int) (Math.random() * Constants.SHAPES);

        code = ran + 1;
        points = Constants.getPoints(ran);
    }

    /**
     * Moves the shape in the given direction
     * 
     * @param direction -direction to move in, constants for each direction is in
     *                  Constants class
     */
    public boolean move(char direction) {
        Point[] newPoints = new Point[Constants.SHAPE_POINTS];
        setPoints(Constants.EMPTY_CODE);

        for (int i = 0; i < Constants.SHAPE_POINTS; i++) {
            if (direction == Constants.DOWN)
                newPoints[i] = new Point(points[i].getX() + 1, points[i].getY());
            else if (direction == Constants.LEFT)
                newPoints[i] = new Point(points[i].getX(), points[i].getY() - 1);
            else if (direction == Constants.RIGHT)
                newPoints[i] = new Point(points[i].getX(), points[i].getY() + 1);
            else {
                int y = points[Constants.PIVOT_INDEX].getY() + points[Constants.PIVOT_INDEX].getX() - points[i].getX();
                int x = points[Constants.PIVOT_INDEX].getX() - points[Constants.PIVOT_INDEX].getY() + points[i].getY();

                newPoints[i] = new Point(x, y);
            }
        }

        for (Point point: points)
            if(point.getX() < 0||point.getY() >= Constants.WIDTH)
                return false;

        if (!possible(newPoints)) {
            setPoints(code);
            if (direction == Constants.DOWN){
                Tetris.stop();
                return false; 
            }
        }

        points = newPoints;

        setPoints(code);
        return true;
    }

    /**
     * Makes shape go all the way to the bottom
     */
    public void allTheWayDown() {
        while (move(Constants.DOWN)){

        }
    }

    /**
     * Given a set of point, returns whether or not the original set of points can
     * be set to the new set of points
     * 
     * @param newPoints -the new set of points
     * @return whether the given set of points is possible
     */
    public boolean possible(Point[] newPoints) {
        for (int j = 0; j < Constants.SHAPE_POINTS; j++) {
            System.out.println(newPoints[j].getX() + ", " + newPoints[j].getY());
            if (newPoints[j].getX() >= Constants.HEIGHT - 1 || newPoints[j].getY() >= Constants.WIDTH
                    || newPoints[j].getY() < 0 || newPoints[j].getX() < 0)
                return false;
            if (!Tetris.field.isEmpty(newPoints[j])) {
                System.out.println("HIII");
                return false;
            }
        }

        return true;
    }

    /**
     * Sets all the points to the given color
     * 
     * @param code -what color the points should be changed to
     */
    public void setPoints(int code) {
        for (int i = 0; i < Constants.SHAPE_POINTS; i++)
            Tetris.field.set(points[i], code);
    }
}

class PlayField {
    int[][] board;

    /**
     * Creates instance of PlayField
     */
    public PlayField() {
        board = new int[Constants.HEIGHT][Constants.WIDTH];
    }

    /**
     * Returns whether the cell is empty or not given an x and y;
     * 
     * @param x -i index of board
     * @param y -j index of board
     * 
     * @return whether or not the cell is empty
     */
    public boolean isEmpty(int x, int y) {
        System.out.println(board[x][y] + " is at " + x + ", " + y);
        return board[x][y] == Constants.EMPTY_CODE;
    }

    /**
     * Returns whether the cell is empty or not given a point
     * 
     * @param point -The cell to be checked
     * @return whether or not the cell is empty
     */
    public boolean isEmpty(Point point) {
        return isEmpty(point.getX(), point.getY());
    }

    /**
     * Given a point, changes point to given color
     * 
     * @param point     -Point to change color
     * @param colorCode -color to change to
     */ 
    public void set(Point point, int colorCode) {
        board[point.getX()][point.getY()] = colorCode;
    }

    /**
     * Clears all rows with with full rows
     */
    public void clearRows() {
        for (int i = Constants.HEIGHT - 1; i >= 0; i--) {
            boolean full = true;
            for (int j = 0; j < Constants.WIDTH; j++) {
                if (board[i][j] == Constants.EMPTY_CODE) {
                    full = false;
                }
            }
            if (full) {
                System.out.println("CLEARING!!");
                clear(i);
                i++;
            }
        }
    }

    /**
     * Clears a row and moves rows above it down
     * 
     * @param row -The row that is to be cleared
     */
    public void clear(int row) {
        board[0] = new int[Constants.WIDTH];

        for (int i = row; i > 0; i--)
            board[i] = board[i - 1].clone();
    }

    /**
     * Given an x and y coordinate, returns the color on the board
     * 
     * @param x -X-coordinate
     * @param y -Y-coordinate
     * 
     * @return returns the color at the given point
     */
    public Color getColor(int x, int y) {
        int code = board[x][y];

        switch (code) {
        case Constants.BLACK_CODE:
            return Constants.BLACK;
        case Constants.BLUE_CODE:
            return Constants.BLUE;
        case Constants.LIGHT_BLUE_CODE:
            return Constants.LIGHT_BLUE;
        case Constants.ORANGE_CODE:
            return Constants.ORANGE;
        case Constants.PURPLE_CODE:
            return Constants.PURPLE;
        case Constants.PINK_CODE:
            return Constants.PINK;
        case Constants.YELLOW_CODE:
            return Constants.YELLOW;
        default:
            return Constants.GREEN;
        }
    }
}

class Constants {
    public static final int HEIGHT = 24;
    public static final int WIDTH = 10;
    public static final int PANEL_HEIGHT = 720;
    public static final int PANEL_WIDTH = 300;
    public static final int CELL_HEIGHT = 30;
    public static final int CELL_WIDTH = 30;
    public static final int SHAPE_POINTS = 4;
    public static final int PIVOT_INDEX = 2;
    public static final int SHAPES = 7;
    public static final int TICK_LENGTH = 2000;

    public static final int EMPTY_CODE = 0;
    public static final int BLACK_CODE = 0;
    public static final int BLUE_CODE = 1;
    public static final int LIGHT_BLUE_CODE = 2;
    public static final int ORANGE_CODE = 3;
    public static final int GREEN_CODE = 4;
    public static final int PURPLE_CODE = 5;
    public static final int PINK_CODE = 6;
    public static final int YELLOW_CODE = 7;

    public static final char DOWN = 'd';
    public static final char LEFT = 'l';
    public static final char RIGHT = 'r';
    public static final char ROTATE = 'R';

    public static final boolean RESIZABLE = false;

    public static final Color BLACK = new Color(0, 0, 0);
    public static final Color BLUE = new Color(0, 0, 255);
    public static final Color LIGHT_BLUE = new Color(110, 190, 255);
    public static final Color ORANGE = new Color(255, 145, 0);
    public static final Color GREEN = new Color(0, 255, 0);
    public static final Color PURPLE = new Color(255, 0, 255);
    public static final Color PINK = new Color(235, 52, 177);
    public static final Color YELLOW = new Color(217, 205, 43);
    public static final Color LINE_COLOR = new Color(212, 211, 210);

    /**
     * Generates a shape given a random number
     * 
     * @param ran -Random number to determine which shape to generate
     * 
     * @return Returns the points of the shape that is generated
     */
    public static Point[] getPoints(int ran) {
        Point[] points;
        switch (ran) {
        case 0:
            points = new Point[] { new Point(0, 4), new Point(1, 4), new Point(2, 4), new Point(3, 4) }; // l shape
            break;
        case 1:
            points = new Point[] { new Point(0, 4), new Point(1, 4), new Point(0, 5), new Point(1, 5) }; // square shape
            break;
        case 2:
            points = new Point[] { new Point(0, 4), new Point(1, 4), new Point(1, 5), new Point(2, 5) }; // z shape
            break;
        case 3:
            points = new Point[] { new Point(0, 4), new Point(1, 4), new Point(1, 5), new Point(2, 4) }; // T shape
            break;
        case 4:
            points = new Point[]{  new Point(0, 4), new Point(1, 4), new Point(1, 3), new Point(2, 3) }; // S shape
            break;
        case 5:
            points = new Point[]{  new Point(0, 4), new Point(1, 4), new Point(2, 4), new Point(0, 3) }; // J shape
            break;
        default:
            points = new Point[] { new Point(0, 4), new Point(1, 4), new Point(2, 4), new Point(0, 5) }; // L shape
            break;
        }

        for (int i = 0; i < SHAPE_POINTS; i++)
            if (!Tetris.field.isEmpty(points[i])) {
                System.out.println("GAME OVER");
                Tetris.gameOver();
                break;
            }

        return points;
    }
}

class Point {
    int x;
    int y;

    /**
     * Creates instance of a point
     * 
     * @param x -x coordinate of point
     * @param y -y coordinate of point
     */
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Resets the x and y of the point
     * 
     * @param x -x coordinate of point
     * @param y -y coordinate of point
     */
    public void set(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * @return x value
     */
    public int getX() {
        return x;
    }

    /**
     * @return y value
     */
    public int getY() {
        return y;
    }
}

class UI implements KeyListener, MouseInputListener {
    public UI() {}

    @Override
    public void keyTyped(KeyEvent e) {
        System.out.println("typ");
    }

    @Override
    public void keyPressed(KeyEvent e) {
        System.out.println("PRES");
    }

    @Override
    public void keyReleased(KeyEvent e) {
        System.out.println("reles");
        if (e.getKeyCode() == KeyEvent.VK_DOWN)
            Tetris.shape.move(Constants.DOWN);
        if (e.getKeyCode() == KeyEvent.VK_RIGHT)
            Tetris.shape.move(Constants.RIGHT);
        if (e.getKeyCode() == KeyEvent.VK_LEFT)
            Tetris.shape.move(Constants.LEFT);
        if (e.getKeyCode() == KeyEvent.VK_UP)
            Tetris.shape.move(Constants.ROTATE);
        if (e.getKeyCode() == KeyEvent.VK_SPACE)
            Tetris.shape.allTheWayDown();

        Tetris.panel.repaint();
        Tetris.tick();
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {
        
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseDragged(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {}

}