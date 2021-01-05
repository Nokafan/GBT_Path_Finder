import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class PathFinding {

    JFrame frame;
    private int cells = 20;
    private int delay = 30;
    private double dense = .5;
    private double density = (cells * cells) * .5;
    private int startX = -1;
    private int startY = -1;
    private int finishX = -1;
    private int finishY = -1;
    private int tool = 0;
    private int checks = 0;
    private int length = 0;
    private int curSize = 0;
    private final int width = 850;
    private final int height = 650;
    private int mSize = 600;
    private int cSize = mSize / cells;
    private final String[] tools = {"Start", "Finish", "Wall", "Eraser"};
    private final String[] modelsSizes = {"1x1", "2x2", "3x3"};
    private boolean solving = false;

    private Node[][] map;
    private Algorithm algorithm = new Algorithm();
    private Random random = new Random();

    JSlider size = new JSlider(1, 10, 2);
    JSlider speed = new JSlider(0, 500, delay);
    JSlider obstacles = new JSlider(1, 100, 50);

    JLabel toolL = new JLabel("Toolbox");
    JLabel sizeL = new JLabel("Size:");
    JLabel cellsL = new JLabel(cells + "x" + cells);
    JLabel delayL = new JLabel("Delay:");
    JLabel msL = new JLabel(delay + "ms");
    JLabel obstacleL = new JLabel("Dens:");
    JLabel densityL = new JLabel(obstacles.getValue() + "%");
    JLabel checkL = new JLabel("Checks: " + checks);
    JLabel lengthL = new JLabel("Path Length: " + length);

    JButton searchB = new JButton("Start Search");
    JButton resetB = new JButton("Reset");
    JButton genMapB = new JButton("Generate Map");
    JButton clearMapB = new JButton("Clear Map");
    JLabel modelSize = new JLabel("Model size");

    JComboBox toolBox = new JComboBox(tools);
    JComboBox modelSizeBox = new JComboBox(modelsSizes);

    JPanel toolPanel = new JPanel();
    Map canvas;
    Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);

    public static void main(String[] args) {    //MAIN METHOD
        new PathFinding();
    }

    public PathFinding() {
        clearMap();
        initialize();
    }

    private void generateMap() {
        clearMap();
        for (int i = 0; i < density; i++) {
            Node currentNode;
            do {
                int x = random.nextInt(cells);
                int y = random.nextInt(cells);
                currentNode = map[x][y];
            } while (currentNode.getType() == 2);
            currentNode.setType(2);
        }
    }

    private void clearMap() {
        finishX = -1;
        finishY = -1;
        startX = -1;
        startY = -1;
        map = new Node[cells][cells];
        for (int x = 0; x < cells; x++) {
            for (int y = 0; y < cells; y++) {
                map[x][y] = new Node(3, x, y);
            }
        }
        reset();
    }

    public void resetMap() {
        for (int x = 0; x < cells; x++) {
            for (int y = 0; y < cells; y++) {
                Node currentNode = map[x][y];
                if (currentNode.getType() == 4 || currentNode.getType() == 5)
                    map[x][y] = new Node(3, x, y);
            }
        }
        if (startX > -1 && startY > -1) {
            map[startX][startY] = new Node(0, startX, startY);
            map[startX][startY].setHops(0);
        }
        if (finishX > -1 && finishY > -1)
            map[finishX][finishY] = new Node(1, finishX, finishY);
        reset();
    }

    private void initialize() {
        frame = new JFrame();
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setSize(width, height);
        frame.setTitle("Path Finding");
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        toolPanel.setBorder(BorderFactory.createTitledBorder(loweredetched, "Controls"));
        int spacePixels = 25;
        int bufferPixels = 40;

        toolPanel.setLayout(null);
        toolPanel.setBounds(10, 10, 210, 600);

        searchB.setBounds(40, spacePixels, 120, 25);
        toolPanel.add(searchB);
        spacePixels += bufferPixels;

        resetB.setBounds(40, spacePixels, 120, 25);
        toolPanel.add(resetB);
        spacePixels += bufferPixels;

        genMapB.setBounds(40, spacePixels, 120, 25);
        toolPanel.add(genMapB);
        spacePixels += bufferPixels;

        clearMapB.setBounds(40, spacePixels, 120, 25);
        toolPanel.add(clearMapB);
        spacePixels += 40;

        modelSize.setBounds(40, spacePixels, 120, 25);
        toolPanel.add(modelSize);
        spacePixels += 25;

        modelSizeBox.setBounds(40, spacePixels, 120, 25);
        toolPanel.add(modelSizeBox);
        spacePixels += 25;

        toolL.setBounds(40, spacePixels, 120, 25);
        toolPanel.add(toolL);
        spacePixels += 25;

        toolBox.setBounds(40, spacePixels, 120, 25);
        toolPanel.add(toolBox);
        spacePixels += bufferPixels;

        sizeL.setBounds(15, spacePixels, 40, 25);
        toolPanel.add(sizeL);

        size.setMajorTickSpacing(10);
        size.setBounds(50, spacePixels, 100, 25);
        toolPanel.add(size);

        cellsL.setBounds(160, spacePixels, 40, 25);
        toolPanel.add(cellsL);
        spacePixels += bufferPixels;

        delayL.setBounds(15, spacePixels, 50, 25);
        toolPanel.add(delayL);

        speed.setMajorTickSpacing(5);
        speed.setBounds(50, spacePixels, 100, 25);
        toolPanel.add(speed);

        msL.setBounds(160, spacePixels, 40, 25);
        toolPanel.add(msL);
        spacePixels += bufferPixels;

        obstacleL.setBounds(15, spacePixels, 100, 25);
        toolPanel.add(obstacleL);

        obstacles.setMajorTickSpacing(5);
        obstacles.setBounds(50, spacePixels, 100, 25);
        toolPanel.add(obstacles);

        densityL.setBounds(160, spacePixels, 100, 25);
        toolPanel.add(densityL);
        spacePixels += bufferPixels;

        checkL.setBounds(15, spacePixels, 100, 25);
        toolPanel.add(checkL);
        spacePixels += bufferPixels;

        lengthL.setBounds(15, spacePixels, 100, 25);
        toolPanel.add(lengthL);

        frame.getContentPane().add(toolPanel);

        canvas = new Map();
        canvas.setBounds(230, 10, mSize + 1, mSize + 1);
        frame.getContentPane().add(canvas);

        searchB.addActionListener(new ActionListener() {        //ACTION LISTENERS
            @Override
            public void actionPerformed(ActionEvent e) {
                reset();
                if ((startX > -1 && startY > -1) && (finishX > -1 && finishY > -1))
                    solving = true;
            }
        });
        resetB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetMap();
                update();
            }
        });
        genMapB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateMap();
                update();
            }
        });
        clearMapB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearMap();
                update();
            }
        });
        modelSizeBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                curSize = modelSizeBox.getSelectedIndex();
            }
        });
        toolBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                tool = toolBox.getSelectedIndex();
            }
        });
        size.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                cells = size.getValue() * 10;
                clearMap();
                reset();
                update();
            }
        });
        speed.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                delay = speed.getValue();
                update();
            }
        });
        obstacles.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                dense = (double) obstacles.getValue() / 100;
                update();
            }
        });
        startSearch();
    }

    public void startSearch() {
        if (solving) {
            algorithm.AStar();
        }
        pause();
    }

    public void pause() {
        int i = 0;
        while (!solving) {
            i++;
            if (i > 500)
                i = 0;
            try {
                Thread.sleep(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        startSearch();
    }

    public void update() {
        density = (cells * cells) * dense;
        cSize = mSize / cells;
        canvas.repaint();
        cellsL.setText(cells + "x" + cells);
        msL.setText(delay + "ms");
        lengthL.setText("Path Length: " + length);
        densityL.setText(obstacles.getValue() + "%");
        checkL.setText("Checks: " + checks);
    }

    public void reset() {
        solving = false;
        length = 0;
        checks = 0;
    }

    public void delay() {
        try {
            Thread.sleep(delay);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class Map extends JPanel implements MouseListener, MouseMotionListener {

        public Map() {
            addMouseListener(this);
            addMouseMotionListener(this);
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            for (int x = 0; x < cells; x++) {
                for (int y = 0; y < cells; y++) {
                    switch (map[x][y].getType()) {
                        case 0:
                            g.setColor(Color.GREEN);
                            break;
                        case 1:
                            g.setColor(Color.RED);
                            break;
                        case 2:
                            g.setColor(Color.BLACK);
                            break;
                        case 3:
                            g.setColor(Color.WHITE);
                            break;
                        case 4:
                            g.setColor(Color.CYAN);
                            break;
                        case 5:
                            g.setColor(Color.YELLOW);
                            break;
                    }
                    g.fillRect(x * cSize, y * cSize, cSize, cSize);
                    g.setColor(Color.BLACK);
                    g.drawRect(x * cSize, y * cSize, cSize, cSize);
                }
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            try {
                int x = e.getX() / cSize;
                int y = e.getY() / cSize;
                Node current = map[x][y];
                if ((tool == 2 || tool == 3) && (current.getType() != 0 && current.getType() != 1))
                    current.setType(tool);
                PathFinding.this.update();
            } catch (Exception z) {
                z.printStackTrace();
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
        }

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
            resetMap();
            try {
                int x = e.getX() / cSize;
                int y = e.getY() / cSize;
                Node currentNode = map[x][y];
                switch (tool) {
                    case 0: {
                        if (currentNode.getType() != 2) {
                            if (startX > -1 && startY > -1) {
                                map[startX][startY].setType(3);
                                map[startX][startY].setHops(-1);
                            }
                            currentNode.setHops(0);
                            startX = x;
                            startY = y;
                            currentNode.setType(0);
                        }
                        break;
                    }
                    case 1: {
                        if (currentNode.getType() != 2) {
                            if (finishX > -1 && finishY > -1)
                                map[finishX][finishY].setType(3);
                            finishX = x;
                            finishY = y;
                            currentNode.setType(1);
                        }
                        break;
                    }
                    default:
                        if (currentNode.getType() != 0 && currentNode.getType() != 1)
                            currentNode.setType(tool);
                        break;
                }
                PathFinding.this.update();
            } catch (Exception z) {
                z.printStackTrace();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }
    }

    class Algorithm {
        public void AStar() {
            ArrayList<Node> priorityNodesList = new ArrayList<>();
            priorityNodesList.add(map[startX][startY]);
            while (solving) {
                if (priorityNodesList.size() <= 0) {
                    solving = false;
                    break;
                }
                int hops = priorityNodesList.get(0).getHops() + 1;

                ArrayList<Node> exploredNodesList =
                        exploreNeighbors(priorityNodesList.get(0), hops);
                if (exploredNodesList.size() > 0) {
                    priorityNodesList.remove(0);
                    priorityNodesList.addAll(exploredNodesList);
                    update();
                    delay();
                } else {
                    priorityNodesList.remove(0);
                }
                sortQue(priorityNodesList);
            }
        }

        public ArrayList<Node> sortQue(ArrayList<Node> sort) {
            int c = 0;
            while (c < sort.size()) {
                int sm = c;
                for (int i = c + 1; i < sort.size(); i++) {
                    if (sort.get(i).getEuclidDist() + sort.get(i).getHops()
                            < sort.get(sm).getEuclidDist() + sort.get(sm).getHops())
                        sm = i;
                }
                if (c != sm) {
                    Node temp = sort.get(c);
                    sort.set(c, sort.get(sm));
                    sort.set(sm, temp);
                }
                c++;
            }
            return sort;
        }

        public ArrayList<Node> exploreNeighbors(Node current, int hops) {
            ArrayList<Node> explored = new ArrayList<>();
            for (int a = -1; a <= 1; a++) {
                for (int b = -1; b <= 1; b++) {
                    int xbound = current.getX() + a;
                    int ybound = current.getY() + b;
                    if ((xbound > -1 && xbound < cells)
                            && (ybound > -1 && ybound < cells)) {
                        Node neighbor = map[xbound][ybound];
                        if ((neighbor.getHops() == -1 || neighbor.getHops() > hops)
                                && explorePassage(neighbor, hops, current.getX(), current.getY())) {
                            explore(neighbor, current.getX(), current.getY(), hops);
                            explored.add(neighbor);
                        }
                    }
                }
            }
            return explored;
        }

        public boolean explorePassage(Node neighbor, int hops, int parentX, int parentY) {
            for (int i = 0; i <= curSize; i++) {
                for (int j = 0; j <= curSize; j++) {
                    if (neighbor.getX() + i >= cells || neighbor.getY() + j >= cells) {
                        return false;
                    }
                    Node tempNode = map[neighbor.getX() + i][neighbor.getY() + j];
                    if (tempNode.getType() == 2) {
                        return false;
                    }
                    if (tempNode.getType() == 1) {
                        explore(neighbor, parentX, parentY, hops);
                        backtrack(neighbor.getLastX(), neighbor.getLastY(), hops);
                    }
                }
            }
            return true;
        }

        public void explore(Node current, int lastx, int lasty, int hops) {
            if (current.getType() != 0 && current.getType() != 1)
                current.setType(4);
            current.setLastNode(lastx, lasty);
            current.setHops(hops);
            checks++;
            if (current.getType() == 1) {
                backtrack(current.getLastX(), current.getLastY(), hops);
            }
        }

        public void backtrack(int lx, int ly, int hops) {
            length = hops;
            while (hops > 1) {
                Node current = map[lx][ly];
                current.setType(5);
                lx = current.getLastX();
                ly = current.getLastY();
                hops--;
            }
            solving = false;
        }
    }

    class Node {
        // 0 = start, 1 = finish, 2 = wall, 3 = empty, 4 = checked, 5 = finalpath
        private int cellType;
        private int hops;
        private int x;
        private int y;
        private int lastX;
        private int lastY;
        private double dToEnd = 0;

        public Node(int type, int x, int y) {
            cellType = type;
            this.x = x;
            this.y = y;
            hops = -1;
        }

        public double getEuclidDist() {
            int xdif = Math.abs(x - finishX);
            int ydif = Math.abs(y - finishY);
            dToEnd = Math.sqrt((xdif * xdif) + (ydif * ydif));
            return dToEnd;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getLastX() {
            return lastX;
        }

        public int getLastY() {
            return lastY;
        }

        public int getType() {
            return cellType;
        }

        public int getHops() {
            return hops;
        }

        public void setType(int type) {
            cellType = type;
        }

        public void setLastNode(int x, int y) {
            lastX = x;
            lastY = y;
        }

        public void setHops(int hops) {
            this.hops = hops;
        }
    }
}
