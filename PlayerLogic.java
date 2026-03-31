import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import javax.swing.*;

public class PlayerLogic extends UI {

    private static final HashSet<Integer> DOUBLE_LETTER_POSITIONS = new HashSet<>();
    private static final HashSet<Integer> TRIPLE_LETTER_POSITIONS = new HashSet<>();
    private static final HashSet<Integer> DOUBLE_WORD_POSITIONS = new HashSet<>();
    private static final HashSet<Integer> TRIPLE_WORD_POSITIONS = new HashSet<>();
    private BotLogic bot;
    private String currentDragFilename = null;
    private TrackedCellTransferHandler trackedHandler;
    public boolean first = true;
    public int passes = 0;

    static {
        int[][] dls = {{0,3}, {0,11}, {2,6}, {2,8}, {3,0}, {3,7}, {3,14},
            {6,2}, {6,6}, {6,8}, {6,12}, {7,3}, {7,11},
            {8,2}, {8,6}, {8,8}, {8,12}, {11,0}, {11,7}, {11,14},
            {12,6}, {12,8}, {14,3}, {14,11}};
        for (int[] pos : dls) DOUBLE_LETTER_POSITIONS.add(pos[0] * 15 + pos[1]);

        int[][] tls = {{1,5}, {1,9}, {5,1}, {5,5}, {5,9}, {5,13},
            {9,1}, {9,5}, {9,9}, {9,13}, {13,5}, {13,9}};
        for (int[] pos : tls) TRIPLE_LETTER_POSITIONS.add(pos[0] * 15 + pos[1]);

        int[][] dws = {{1,1}, {2,2}, {3,3}, {4,4}, {7,7}, {1,13}, {2,12},
            {3,11}, {4,10}, {10,4}, {11,3}, {12,2}, {13,1},
            {10,10}, {11,11}, {12,12}, {13,13}};
        for (int[] pos : dws) DOUBLE_WORD_POSITIONS.add(pos[0] * 15 + pos[1]);

        int[][] tws = {{0,0}, {0,7}, {0,14}, {7,0}, {7,14},
            {14,0}, {14,7}, {14,14}};
        for (int[] pos : tws) TRIPLE_WORD_POSITIONS.add(pos[0] * 15 + pos[1]);
    }

    // Constructs a PlayerLogic instance and calls the parent UI constructor.
    public PlayerLogic() {
        super();
    }

    class LetterTransferable implements Transferable {
        private Image image;
        private String filename;

        // Stores the dragged image and its associated filename for transfer.
        public LetterTransferable(Image image, String filename) {
            this.image = image;
            this.filename = filename;
        }

        // Returns the filename of the letter tile being transferred.
        public String getFilename() {
            return filename;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{DataFlavor.imageFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor.equals(DataFlavor.imageFlavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) {
            return image;
        }
    }

    class TrackedCellTransferHandler extends TransferHandler {

        // Returns true only if the target cell is empty and accepts image data.
        @Override
        public boolean canImport(TransferSupport support) {
            if (!support.isDataFlavorSupported(DataFlavor.imageFlavor)) return false;
            JLabel target = (JLabel) support.getComponent();
            return target.getIcon() == null;
        }

        // Drops the dragged image onto the target cell and stores its filename.
        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) return false;
            try {
                Image img = (Image) support.getTransferable().getTransferData(DataFlavor.imageFlavor);
                JLabel target = (JLabel) support.getComponent();
                target.setIcon(new ImageIcon(img));
                target.setName(currentDragFilename);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        // Creates a transferable from the cell's current icon and records its filename.
        @Override
        protected Transferable createTransferable(JComponent c) {
            JLabel label = (JLabel) c;
            if (label.getIcon() != null) {
                ImageIcon icon = (ImageIcon) label.getIcon();
                Image img = icon.getImage();
                currentDragFilename = label.getName();

                setDragImage(img);
                setDragImageOffset(new Point(img.getWidth(null) / 2, img.getHeight(null) / 2));

                return new LetterTransferable(img, currentDragFilename);
            }
            return null;
        }

        @Override
        public int getSourceActions(JComponent c) { return MOVE; }

        // Clears the source cell's icon and filename after a successful move.
        @Override
        protected void exportDone(JComponent source, Transferable data, int action) {
            if (action == MOVE) {
                ((JLabel) source).setIcon(null);
                ((JLabel) source).setName(null);
            }
            currentDragFilename = null;
        }
    }

    // Sets up the game board, creates the bot, and attaches the tracked transfer handler to all cells.
    @Override
    public void window3() {

        super.window3();
        bot = new BotLogic(this);

        trackedHandler = new TrackedCellTransferHandler();

        for (int r = 0; r < 15; r++) {
            for (int c = 0; c < 15; c++) {
                cells[r][c].setTransferHandler(trackedHandler);
            }
        }

        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(cells[0][0]);
        if (frame != null) {
            updateHandlers(frame.getContentPane(), trackedHandler);
        }
    }

    // Recursively assigns the tracked transfer handler to every letter rack label in the container.
    private void updateHandlers(Container container, TrackedCellTransferHandler handler) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                if (label.getParent() == letters) {
                    label.setTransferHandler(trackedHandler);
                }
            } else if (comp instanceof Container) {
                updateHandlers((Container) comp, handler);
            }
        }
    }

    // Returns selected letters to the pool, draws replacements, then triggers the bot's turn.
    @Override
    public void replace1(int[] rackIndices) {

        for (int i : rackIndices) {
            JLabel label = (JLabel) letters.getComponent(i);
            if (label.getIcon() != null && label.getName() != null) {
                int letterNum = Integer.parseInt(label.getName().split("\\.")[0]);
                for (int j = 0; j < Letters_Array.length; j++) {
                    if (Letters_Array[j] == 0) {
                        Letters_Array[j] = letterNum;
                        break;
                    }
                }
            }
        }

        for (int i : rackIndices) {
            JLabel label = (JLabel) letters.getComponent(i);
            int index = (int)(Math.random() * Letters_Array.length);
            int num = Letters_Array[index];
            int attempts = 0;

            while (num == 0) {
                index = (index + 1) % Letters_Array.length;
                num = Letters_Array[index];
                attempts++;
                if (attempts >= Letters_Array.length) break;
            }
            if (num == 0) break;

            Letters_Array[index] = 0;

            String filename = num + ".png";
            ImageIcon icon = loadIcon(filename);
            Image scaled = icon.getImage().getScaledInstance(45, 45, Image.SCALE_SMOOTH);
            label.setIcon(new ImageIcon(scaled));
            label.setName(filename);
            label.setTransferHandler(trackedHandler);
        }

        passes = 0;
        yourTurnBox.setText(" Opponent's Turn ");
        freezeBoard();
        bot.botTurn();
    }

    // Increments the pass counter, freezes the board, and triggers the bot's turn.
    @Override
    public void pass() {
        passes++;
        yourTurnBox.setText(" Opponent's Turn ");
        freezeBoard();
        isEnd();
        bot.botTurn();
    }

    // Validates the player's placed word, scores it, and triggers the bot's turn if valid.
    @Override
    public void submit() {

        String word = "";
        ArrayList<Point> pos = new ArrayList<>();
        boolean found = false;
        int placedLetters = 0;
        int count = 0;

        for (int r = 0; r < 15; r++) {
            for (int c = 0; c < 15; c++) {
                if (cells[r][c].getName() != null && cells[r][c].getTransferHandler() != null) {
                    placedLetters++;
                }
            }
        }

        if (placedLetters == 0) {
            window5("Error: No letters have been placed!");
            return;
        }

        if (first) {
            if (cells[7][7].getName() == null) {
                ReturnLetters();
                window5("<html><center>Error: First word must pass through the center!</center></html>");
                return;
            } else first = false;
        }

        for (int i = 0; i < 15; i++) {
            word = "";
            pos.clear();
            count = 0;

            for (int j = 0; j < 15; j++) {

                boolean bool = true;
                if (hasTransfer(i, j)) bool = !hasAboveBelow(i, j, true);

                if (hasName(i, j) && bool) {

                    pos.add(new Point(i, j));
                    char letter = getLetter(cells[i][j].getName().split("\\.")[0]);
                    word += letter;
                    if (hasTransfer(i, j)) count++;
                    boolean braek = false;
                    int k = j + 1;

                    while ((!braek)) {
                        if (k >= 15) break;

                        boolean boool;
                        if (hasTransfer(i, k)) boool = !hasAboveBelow(i, k, true);
                            else boool = true;

                        if (hasName(i, k) && boool) {
                            pos.add(new Point(i, k));
                            if (hasTransfer(i, k)) count++;
                            letter = getLetter(cells[i][k].getName().split("\\.")[0]);
                            word += letter;
                        } else {
                            braek = true;
                        }
                        k++;
                    }

                    if (count == placedLetters) {
                        found = true;
                        break;
                    } else {
                        word = "";
                        pos.clear();
                        count = 0;
                    }
                }
            }

            if (found) break;
        }

        if (!found) {
            for (int j = 0; j < 15; j++) {

                word = "";
                count = 0;
                pos.clear();

                for (int i = 0; i < 15; i++) {

                    boolean bool = true;
                    if (hasTransfer(i, j)) bool = !hasAboveBelow(i, j, false);

                    if (hasName(i, j) && bool) {

                        pos.add(new Point(i, j));
                        char letter = getLetter(cells[i][j].getName().split("\\.")[0]);
                        word += letter;
                        if (hasTransfer(i, j)) count++;
                        boolean braek = false;
                        int k = i + 1;

                        while ((!braek)) {
                            if (k >= 15) break;

                            boolean boool;
                            if (hasTransfer(k, j)) boool = !hasAboveBelow(k, j, false);
                                else boool = true;

                            if (hasName(k, j) && boool) {
                                pos.add(new Point(k, j));
                                if (hasTransfer(k, j)) count++;
                                letter = getLetter(cells[k][j].getName().split("\\.")[0]);
                                word += letter;
                            } else {
                                braek = true;
                            }
                            k++;
                        }

                        if (count == placedLetters) {
                            found = true;
                            break;
                        } else {
                            word = "";
                            pos.clear();
                            count = 0;
                        }
                    }
                }

                if (found) break;
            }
        }

        if (!found) {
            ReturnLetters();
            window5("<html><center>Error: Tiles are not placed correctly!</center></html>");
            return;
        }

        Trie trie = new Trie();
        try {
            InputStream is = getClass().getResourceAsStream("/Words.txt");
            if (is == null) is = getClass().getResourceAsStream("Words.txt");
            if (is != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line;
                while ((line = br.readLine()) != null) {
                    if (!line.trim().isEmpty()) trie.insert(line.trim());
                }
                br.close();
            } else {
                BufferedReader br = new BufferedReader(new java.io.FileReader("Words.txt"));
                String line;
                while ((line = br.readLine()) != null) {
                    if (!line.trim().isEmpty()) trie.insert(line.trim());
                }
                br.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        int[] blankIndx = {-1, -1};
        char[] blankChoices = {0, 0};
        int blankCount = 0;

        for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) == '[') {
                char c = ask();
                word = word.substring(0, i) + c + word.substring(i + 1);

                if (blankIndx[0] == -1) blankIndx[0] = i;
                else blankIndx[1] = i;

                blankChoices[blankCount++] = c;
            }
        }

        if (trie.search(word)) {

            int b = 0;
            for (int i = 0; i < word.length(); i++) {
                if (blankIndx[0] == i || blankIndx[1] == i) {
                    JLabel label = cells[pos.get(i).x][pos.get(i).y];
                    int letterNum = (int) blankChoices[b++] - 'A' + 1;
                    String filename = letterNum + ".png";
                    ImageIcon icon = loadIcon(filename);
                    Image scaled = icon.getImage().getScaledInstance(45, 45, Image.SCALE_SMOOTH);
                    label.setIcon(new ImageIcon(scaled));
                    label.setName(filename);
                    label.putClientProperty("isBlank", true);
                    label.setBorder(BorderFactory.createLineBorder(new Color(0, 220, 220), 3));
                }
            }

            Point startPoint = pos.get(0);
            Point endPoint = pos.get(pos.size() - 1);
            int start = startPoint.x * 15 + startPoint.y;
            int end = endPoint.x * 15 + endPoint.y;
            int[] positions = getPosition(start, end);

            your_score += score_calc(word, positions, blankIndx,true);
            int dif = letters_left - placedLetters;
            if (dif < 0) letters_left = 0;
            else letters_left = dif;

            for (Point p : pos) {
                cells[p.x][p.y].setTransferHandler(null);
                cells[p.x][p.y].setBackground(new Color(200, 200, 200));
            }

            scoreTable.setValueAt(your_score + " ", 0, 1);

            if (UI.letters_left < 10) lettersLeftBox.setText(" Letters Left: " + UI.letters_left + "  ");
            else lettersLeftBox.setText(" Letters Left: " + UI.letters_left + " ");
            lettersLeftBox.setText(" Letters Left: " + letters_left + " ");

            drawPlayerLetters();
            yourTurnBox.setText(" Opponent's Turn ");
            passes = 0;
            bot.botTurn();

        } else {
            ReturnLetters();
            window5("<html><center>Error: Word does not exist!</center></html>");
        }
    }

    // Entry point: sets the UI scale and launches the game.
    public static void main(String[] args) {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            System.setProperty("sun.java2d.uiScale", "1");
            System.setProperty("sun.java2d.dpiaware", "true");
        } else {
            System.setProperty("sun.java2d.uiScale", "2");
        }
        PlayerLogic game = new PlayerLogic();
        game.window1();
    }

    // Returns true if the specified board cell has a name (i.e. holds a tile).
    public boolean hasName(int row, int col) {
        if (cells[row][col].getName() != null) return true;
        return false;
    }

    // Returns true if the specified board cell has an active transfer handler.
    public boolean hasTransfer(int row, int col) {
        if (cells[row][col].getTransferHandler() != null) return true;
        return false;
    }

    // Returns true if the cell has a neighbouring tile in the direction perpendicular to play.
    public boolean hasAboveBelow(int row, int col, boolean isRow) {
        if (isRow) {
            if (row == 14) {
                if (cells[row - 1][col].getName() != null) return true;
                return false;
            } else if (row == 0) {
                if (cells[row + 1][col].getName() != null) return true;
                return false;
            } else {
                if (cells[row - 1][col].getName() != null || cells[row + 1][col].getName() != null) return true;
            }
            return false;
        } else {
            if (col == 14) {
                if (cells[row][col - 1].getName() != null) return true;
                return false;
            } else if (col == 0) {
                if (cells[row][col + 1].getName() != null) return true;
                return false;
            } else {
                if (cells[row][col - 1].getName() != null || cells[row][col + 1].getName() != null) return true;
            }
            return false;
        }
    }

    // Calculates and returns the score for a word, applying all board multipliers.
    public int score_calc(String word, int[] positions, int[] blankIndx, boolean commit) {
        HashMap<Character, Integer> points = new HashMap<>();
        points.put('A', 1); points.put('E', 1); points.put('I', 1); points.put('O', 1);
        points.put('U', 1); points.put('L', 1); points.put('N', 1); points.put('R', 1);
        points.put('S', 1); points.put('T', 1); points.put('D', 2); points.put('G', 2);
        points.put('B', 3); points.put('C', 3); points.put('M', 3); points.put('P', 3);
        points.put('F', 4); points.put('H', 4); points.put('V', 4); points.put('W', 4);
        points.put('Y', 4); points.put('K', 5); points.put('J', 8); points.put('X', 8);
        points.put('Q', 10); points.put('Z', 10);

        int total = 0;
        int wordMultiplier = 1;

        for (int i = 0; i < word.length(); i++) {
            char c = word.toUpperCase().charAt(i);
            int letterScore = points.getOrDefault(c, 0);

            int pos = positions[i];
            int row = pos / 15;
            int col = pos % 15;

            if (i == blankIndx[0] || i == blankIndx[1] || cells[row][col].getClientProperty("isBlank") != null) letterScore = 0;

            int letterMultiplier = 1;

            if (DOUBLE_LETTER_POSITIONS.contains(pos)) {
                letterMultiplier = 2;
                if (commit) DOUBLE_LETTER_POSITIONS.remove(pos);
            } 
            else if (TRIPLE_LETTER_POSITIONS.contains(pos)) {
                letterMultiplier = 3;
                if (commit) TRIPLE_LETTER_POSITIONS.remove(pos);
            }

            if (DOUBLE_WORD_POSITIONS.contains(pos)) {
                wordMultiplier *= 2;
                if (commit) DOUBLE_WORD_POSITIONS.remove(pos);
            } 
            else if (TRIPLE_WORD_POSITIONS.contains(pos)) {
                wordMultiplier *= 3;
                if (commit) TRIPLE_WORD_POSITIONS.remove(pos);
            }

            total += letterScore * letterMultiplier;
        }

        total *= wordMultiplier;

        if (word.length() == 8) total += 50;

        return total;
    }

    // Returns an array of board positions for every tile in a word given its start and end indices.
    public int[] getPosition(int start, int end) {
        int dif = end - start;
        int[] position = new int[dif + 1];

        if (dif < 14) {
            for (int i = 0; i < dif + 1; i++) {
                position[i] = start + i;
            }
        } else {
            for (int i = 0; i < dif / 15 + 1; i++) {
                position[i] = start + (i * 15);
            }
        }

        return position;
    }

    // Converts a numeric string representing a letter position to its corresponding character.
    public static char getLetter(String s) {
        int position = Integer.parseInt(s);
        return (char) ('A' + position - 1);
    }

    // Checks that no player tiles remain on the board before allowing a replace or pass action.
    @Override
    public boolean checktiles() {
        for (int r = 0; r < 15; r++) {
            for (int c = 0; c < 15; c++) {
                JLabel cell = cells[r][c];
                if (cell.getIcon() != null &&
                    cell.getName() != null &&
                    cell.getTransferHandler() != null) {
                    ReturnLetters();
                    window5("<html><center>Error: Not all tiles are on the rack!</center></html>");
                    return false;
                }
            }
        }
        return true;
    }

    // Triggers the end-game screen if both players have passed consecutively.
    public void isEnd() {
        if (passes == 2) window4();
    }

    // Re-enables drag-and-drop on board cells and rack letters after the bot's turn ends.
    @Override
    public void unfreezeBoard() {
        for (int r = 0; r < 15; r++) {
            for (int c = 0; c < 15; c++) {
                if (cells[r][c].getBackground().equals(new Color(200, 200, 200))) {
                    cells[r][c].setTransferHandler(null);
                } else {
                    cells[r][c].setTransferHandler(trackedHandler);
                }
            }
        }

        for (Component comp : letters.getComponents()) {
            if (comp instanceof JLabel) {
                ((JLabel) comp).setTransferHandler(trackedHandler);
            }
        }

        disableButtons(frame3.getContentPane(), false);
    }

    // Draws new tiles from the pool to fill any empty slots in the player's rack.
    public void drawPlayerLetters() {
        if (UI.letters_left > 0) {

            int temp;
            if (UI.letters_left < 7) temp = UI.letters_left;
            else temp = 7;

            for (int i = 0; i < temp; i++) {
                JLabel label = (JLabel) letters.getComponent(i);
                if (label.getIcon() == null) {
                    int index = (int)(Math.random() * Letters_Array.length);
                    int num = Letters_Array[index];
                    int attempts = 0;

                    while (num == 0) {
                        index = (index + 1) % Letters_Array.length;
                        num = Letters_Array[index];
                        attempts++;
                        if (attempts >= Letters_Array.length) break;
                    }
                    if (num == 0) break;

                    Letters_Array[index] = 0;
                    String filename = num + ".png";
                    ImageIcon icon = loadIcon(filename);
                    Image scaled = icon.getImage().getScaledInstance(45, 45, Image.SCALE_SMOOTH);
                    label.setIcon(new ImageIcon(scaled));
                    label.setName(filename);
                    label.setTransferHandler(trackedHandler);
                }
            }
        }
    }

    // Moves all tiles the player placed back onto the rack, clearing them from the board.
    private void ReturnLetters() {
        for (int r = 0; r < 15; r++) {
            for (int c = 0; c < 15; c++) {
                if (cells[r][c].getName() != null && cells[r][c].getTransferHandler() != null) {
                    for (int i = 0; i < 7; i++) {
                        JLabel label = (JLabel) letters.getComponent(i);
                        if (label.getIcon() == null) {
                            label.setIcon(cells[r][c].getIcon());
                            label.setName(cells[r][c].getName());
                            label.setTransferHandler(trackedHandler);
                            break;
                        }
                    }
                    cells[r][c].setIcon(null);
                    cells[r][c].setName(null);
                }
            }
        }
    }
}