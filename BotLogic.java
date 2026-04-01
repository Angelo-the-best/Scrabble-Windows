import java.awt.Color;
import java.awt.Image;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.Timer;

public class BotLogic {

    private PlayerLogic player;
    int[] botLetters = new int[7];
    PriorityQueue<WordScore> top5 = new PriorityQueue<>(
        (a, b) -> Integer.compare(a.score, b.score)
    );
    private Trie trie;

    // Initialises the bot with a reference to the player and loads the trie.
    public BotLogic(PlayerLogic player) {
        this.player = player;
        this.trie = loadTrie();
    }

    // Loads the word list from file into a Trie and returns it.
    private Trie loadTrie() {
        Trie t = new Trie();
        try {
            InputStream is = getClass().getResourceAsStream("/Words.txt");
            if (is == null) is = getClass().getResourceAsStream("Words.txt");
            if (is != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line;
                while ((line = br.readLine()) != null) {
                    if (!line.trim().isEmpty()) t.insert(line.trim());
                }
                br.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return t;
    }

    // Controls the bot's full turn sequence from drawing letters to placing a word.
    public void botTurn() {

        drawBotLetters(botLetters);

        if (player.cells[7][7].getName() == null) {
            
            playFirstWord();

            int placedLetters_first = 0;
            for (int i = 0; i < 7; i++) {
                if (botLetters[i] == 0) placedLetters_first++;
            }
            UI.letters_left -= placedLetters_first;

            player.scoreTable.setValueAt(UI.opp_score, 1, 1);
            player.passes = 0;
            waitASecond(placedLetters_first);
            updateLetters();

            return;
        }

        top5.clear();
        findrow();
        findcol();

        if (top5.isEmpty()) {
            botSwapOrPass();
            player.isEnd();
            return;
        }

        WordScore ChosenOne = choose();

        int nonZeroBefore = 0;
        for (int i = 0; i < 7; i++) {
            if (botLetters[i] != 0) nonZeroBefore++;
        }

        place(ChosenOne.word, player.getPosition(ChosenOne.start, ChosenOne.end));
        UI.opp_score += player.score_calc(ChosenOne.word, player.getPosition(ChosenOne.start, ChosenOne.end), new int[]{-1,-1}, true);

        int nonZeroAfter = 0;
        for (int i = 0; i < 7; i++) {
            if (botLetters[i] != 0) nonZeroAfter++;
        }
        int placedLetters = nonZeroBefore - nonZeroAfter;
        
        player.scoreTable.setValueAt(UI.opp_score, 1, 1);

        int dif = UI.letters_left - placedLetters;
        if (dif < 0) UI.letters_left = 0;
        else UI.letters_left = dif;

        player.passes = 0;
        waitASecond(placedLetters);
        updateLetters();
    }

    // Scans every row and triggers a full row check when a placed tile is found.
    public void findrow() {
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                if ((player.cells[i][j].getName() != null)) {
                    checkrow(i);
                    break;
                }
            }
        }
    }

    // Scans every column and triggers a full column check when a placed tile is found.
    public void findcol() {
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                if ((player.cells[j][i].getName() != null)) {
                    checkcol(i);
                    break;
                }
            }
        }
    }

    // Builds a template array for the given row and passes it to findcomb.
    public void checkrow(int row) {
        int[] halfword = new int[16];
        int count = 0;
        int start = 0, end = 0;

        for (int j = 0; j < 15; j++) {
            if (player.cells[row][j].getName() != null) {
                halfword[j] = Integer.parseInt(player.cells[row][j].getName().split("\\.")[0]);
            } else {
                boolean around;
                if (row == 0) around = player.cells[row + 1][j].getName() == null;
                else if (row == 14) around = player.cells[row - 1][j].getName() == null;
                else around = (player.cells[row - 1][j].getName() == null) && (player.cells[row + 1][j].getName() == null);

                if (around) {
                    halfword[j] = -1;
                } else {
                    halfword[j] = -2;
                }
            }
        }

        for (int k = 0; k < 16; k++) {
            if (halfword[k] == -2 || halfword[k] == 0) {
                if (k == 0) {
                    start = 1;
                    end = 3;
                    continue;
                }
                if (k == 14) break;
                if (halfword[start] == -2) {
                    start++;
                    continue;
                }

                int[] temp = Arrays.copyOfRange(halfword, start, end);
                findcomb(temp, count, row, start, end - 1, true);
                start = k + 1;
                count = 0;

            } else {
                end = k + 1;
                if (halfword[k] == -1) count++;
            }
        }
    }

    // Builds a template array for the given column and passes it to findcomb.
    public void checkcol(int col) {
        int[] halfword = new int[16];
        int count = 0;
        int start = 0, end = 0;

        for (int j = 0; j < 15; j++) {
            if (player.cells[j][col].getName() != null) {
                halfword[j] = Integer.parseInt(player.cells[j][col].getName().split("\\.")[0]);
            } else {
                boolean around;
                if (col == 0) around = player.cells[j][col + 1].getName() == null;
                else if (col == 14) around = player.cells[j][col - 1].getName() == null;
                else around = (player.cells[j][col - 1].getName() == null) && (player.cells[j][col + 1].getName() == null);

                if (around) {
                    halfword[j] = -1;
                } else {
                    halfword[j] = -2;
                }
            }
        }

        for (int k = 0; k < 16; k++) {
            if (halfword[k] == -2 || halfword[k] == 0) {
                if (k == 0) {
                    start = 1;
                    continue;
                }
                if (k == 14) break;
                if (halfword[start] == -2) {
                    start++;
                    continue;
                }

                int[] temp = Arrays.copyOfRange(halfword, start, end);
                findcomb(temp, count, col, start, end - 1, false);
                start = k + 1;
                count = 0;

            } else {
                end = k + 1;
                if (halfword[k] == -1) count++;
            }
        }
    }

    // Iterates over all valid sub-segments of a template and searches for playable words.
    public void findcomb(int[] wrd, int count, int rowcol, int start, int end, boolean isRow) {

        int startIdx;
        int endIdx;

        int botLettersAvailable = 0;
        for (int b : botLetters) {
            if (b != 0) botLettersAvailable++;
        }

        for (int size = 2; size < wrd.length; size++) {
            startIdx = 0;
            endIdx = startIdx + size;

            for (int k = 0; k < wrd.length - size; k++) {
                int[] subarray = Arrays.copyOfRange(wrd, startIdx, endIdx);

                int subarrayEmpties = 0;
                for (int v : subarray) {
                    if (v == -1) subarrayEmpties++;
                }

                if (subarrayEmpties > botLettersAvailable) {
                    startIdx++;
                    endIdx++;
                    continue;
                }

                if (HasLetters(subarray) && HasSpaces(subarray)) {
                    int boardStart, boardEnd;
                    if (isRow) {
                        boardStart = rowcol * 15 + start + startIdx;
                        boardEnd = rowcol * 15 + start + endIdx - 1;
                    } else {
                        boardStart = (start + startIdx) * 15 + rowcol;
                        boardEnd = (start + endIdx - 1) * 15 + rowcol;
                    }

                    boolean valid = true;
                    if (isRow) {
                        int row = boardStart / 15;
                        int startCol = boardStart % 15;
                        int endCol = boardEnd % 15;
                        boolean leftClear = (startCol == 0) || (player.cells[row][startCol - 1].getName() == null);
                        boolean rightClear = (endCol == 14) || (player.cells[row][endCol + 1].getName() == null);
                        if (!leftClear || !rightClear) valid = false;
                    } else {
                        int col = boardStart % 15;
                        int startRow = boardStart / 15;
                        int endRow = boardEnd / 15;
                        boolean topClear = (startRow == 0) || (player.cells[startRow - 1][col].getName() == null);
                        boolean bottomClear = (endRow == 14) || (player.cells[endRow + 1][col].getName() == null);
                        if (!topClear || !bottomClear) valid = false;
                    }

                    if (valid) {
                        WordScore ws = findword(subarray, botLetters, trie, boardStart, boardEnd);
                        if (ws != null) {
                            addWord(top5, ws.word, ws.score, boardStart, boardEnd);
                        }
                    }
                }

                startIdx++;
                endIdx++;
            }
        }
    }

    // Finds the highest-scoring valid word that fits a given template using the bot's letters.
    public WordScore findword(int[] template, int[] botletters, Trie trie, int start, int end) {
        List<Integer> emptyPositions = new ArrayList<>();
        for (int i = 0; i < template.length; i++) {
            if (template[i] == -1) emptyPositions.add(i);
        }

        String[] bestWord = new String[]{null};
        int[] bestScore = new int[]{-1};

        findwordHelper(template, botletters, emptyPositions, 0,
                new boolean[botletters.length],
                new boolean[emptyPositions.size()],
                trie, bestScore, bestWord, start, end);

        if (bestWord[0] == null || bestWord[0].isEmpty()) return null;
        return new WordScore(bestWord[0], bestScore[0], start, end);
    }

    // Recursively fills empty template slots with bot letters to find the best valid word.
    private void findwordHelper(int[] template, int[] botletters,
                                List<Integer> emptyPositions, int posIdx,
                                boolean[] used, boolean[] slotIsBlank,
                                Trie trie, int[] bestScore, String[] bestWord, int start, int end) {
        if (posIdx == emptyPositions.size()) {
            StringBuilder word = new StringBuilder();
            for (int letterCode : template) {
                if (letterCode == -1) return;
                if (letterCode > 0) word.append(numToChar(letterCode));
            }
            String wordStr = word.toString();
            if (trie.search(wordStr)) {
                int[] blankIndx = {-1, -1};
                int blankCount = 0;
                for (int i = 0; i < emptyPositions.size() && blankCount < 2; i++) {
                    if (slotIsBlank[i]) blankIndx[blankCount++] = emptyPositions.get(i);
                }
                int score = player.score_calc(wordStr, player.getPosition(start, end), blankIndx,false);
                if (score > bestScore[0]) {
                    bestScore[0] = score;
                    bestWord[0] = wordStr;
                }
            }
            return;
        }

        int currentPos = emptyPositions.get(posIdx);

        for (int i = 0; i < botletters.length; i++) {
            if (!used[i] && botletters[i] != 0) {
                used[i] = true;
                if (botletters[i] == 27) {
                    for (int letter = 1; letter <= 26; letter++) {
                        template[currentPos] = letter;
                        slotIsBlank[posIdx] = true;
                        findwordHelper(template, botletters, emptyPositions,
                                posIdx + 1, used, slotIsBlank, trie, bestScore, bestWord, start, end);
                    }
                } else {
                    template[currentPos] = botletters[i];
                    slotIsBlank[posIdx] = false;
                    findwordHelper(template, botletters, emptyPositions,
                            posIdx + 1, used, slotIsBlank, trie, bestScore, bestWord, start, end);
                }
                template[currentPos] = -1;
                used[i] = false;
            }
        }
    }

    // Places a word on the board by consuming bot rack tiles and triggering animations.
    public void place(String word, int[] positions) {
        if (word == null || word.isEmpty()) return;

        boolean[] used = new boolean[botLetters.length];
        int[] assignedSlot = new int[word.length()];
        boolean[] isBlankSlot = new boolean[word.length()];
        Arrays.fill(assignedSlot, -1);

        for (int k = 0; k < word.length(); k++) {
            int letterNum = word.charAt(k) - 'A' + 1;

            int pos = positions[k];
            int row = pos / 15;
            int col = pos % 15;

            if (player.cells[row][col].getName() != null) continue;

            boolean found = false;

            for (int j = 0; j < botLetters.length; j++) {
                if (!used[j] && botLetters[j] == letterNum) {
                    used[j] = true;
                    assignedSlot[k] = j;
                    found = true;
                    break;
                }
            }

            if (!found) {
                for (int j = 0; j < botLetters.length; j++) {
                    if (!used[j] && botLetters[j] == 27) {
                        used[j] = true;
                        botLetters[j] = -letterNum;
                        assignedSlot[k] = j;
                        isBlankSlot[k] = true;
                        break;
                    }
                }
            }
        }

        for (int k = 0; k < word.length(); k++) {
            int letterNum = word.charAt(k) - 'A' + 1;

            int pos = positions[k];
            int row = pos / 15;
            int col = pos % 15;

            JLabel cell = player.cells[row][col];

            if (assignedSlot[k] == -1) continue;

            int j = assignedSlot[k];

            if (isBlankSlot[k]) {
                botLetters[j] = 0;
                cell.setTransferHandler(null);
                animateLetterPlacement(cell, letterNum, k * 200, true);
            } else {
                botLetters[j] = 0;
                cell.setTransferHandler(null);
                animateLetterPlacement(cell, letterNum, k * 200, false);
            }
        }
    }

    // Animates a letter tile flying from the edge of the screen onto the target board cell.
    private void animateLetterPlacement(JLabel targetCell, int letterNum, int delay, boolean isBlank) {

        String filename = letterNum + ".png";
        ImageIcon icon = loadIcon(filename);
        final int TILE = Math.max(10, (int)(targetCell.getWidth() * 0.9));
        Image scaled = icon.getImage().getScaledInstance(TILE, TILE, Image.SCALE_SMOOTH);
        final ImageIcon scaledIcon = new ImageIcon(scaled);

        JLayeredPane layeredPane = player.frame3.getLayeredPane();

        Timer startTimer = new Timer(delay, null);
        startTimer.setRepeats(false);
        startTimer.addActionListener(startEvt -> {

            Point cellOrigin = javax.swing.SwingUtilities.convertPoint(
                    targetCell, new Point(0, 0), layeredPane);
            final float targetX = cellOrigin.x + (targetCell.getWidth()  - TILE) / 2f;
            final float targetY = cellOrigin.y + (targetCell.getHeight() - TILE) / 2f;

            int paneW = layeredPane.getWidth();
            int paneH = layeredPane.getHeight();
            int edge  = (int)(Math.random() * 4);
            final float startX, startY;
            switch (edge) {
                case 0:  startX = targetX;       startY = -TILE - 20;      break;
                case 1:  startX = paneW + 20;    startY = targetY;          break;
                case 2:  startX = targetX;       startY = paneH + 20;       break;
                default: startX = -TILE - 20;    startY = targetY;          break;
            }

            JLabel animLabel = new JLabel(scaledIcon);
            animLabel.setSize(TILE, TILE);
            animLabel.setLocation(Math.round(startX), Math.round(startY));
            animLabel.setOpaque(false);

            layeredPane.add(animLabel, JLayeredPane.DRAG_LAYER);
            layeredPane.repaint();

            final int STEPS    = 30;
            final int FRAME_MS = 15;
            final int[] step   = {0};

            Timer animTimer = new Timer(FRAME_MS, null);
            animTimer.addActionListener(animEvt -> {
                if (step[0] < STEPS) {
                    float t     = (float) step[0] / STEPS;
                    float eased = 1f - (1f - t) * (1f - t);
                    int   newX  = Math.round(startX + (targetX - startX) * eased);
                    int   newY  = Math.round(startY + (targetY - startY) * eased);
                    animLabel.setLocation(newX, newY);
                    layeredPane.repaint(
                        Math.min(newX, Math.round(startX)) - 2,
                        Math.min(newY, Math.round(startY)) - 2,
                        TILE + Math.abs(Math.round(targetX - startX)) + 4,
                        TILE + Math.abs(Math.round(targetY - startY)) + 4
                    );
                    step[0]++;
                } else {
                    ((Timer) animEvt.getSource()).stop();
                    layeredPane.remove(animLabel);
                    layeredPane.repaint();

                    targetCell.setIcon(scaledIcon);
                    targetCell.setName(letterNum + ".png");
                    targetCell.setTransferHandler(null);
                    targetCell.setBackground(new Color(200, 200, 200));

                    if (isBlank) {
                        targetCell.putClientProperty("isBlank", true);
                        targetCell.setBorder(
                            BorderFactory.createLineBorder(new Color(0, 220, 220), 3));
                    }
                    targetCell.repaint();
                }
            });
            animTimer.start();
        });
        startTimer.start();
    }

    // Adds a word to the priority queue, keeping only the top 5 highest-scoring entries.
    public static void addWord(PriorityQueue<WordScore> pq, String word, int score, int start, int end) {
        if (word == null || word.isEmpty() || score == 0) {
            return;
        }

        for (WordScore ws : pq) {
            if (ws.word.equals(word) && ws.score == score) {
                return;
            }
        }

        pq.offer(new WordScore(word, score, start, end));

        if (pq.size() > 5) {
            pq.poll();
        }
    }

    // Converts a letter number (1–26) to its corresponding uppercase character.
    public char numToChar(int n) {
        return (char) ('A' + n - 1);
    }

    // Selects a word from the top-5 list based on the current difficulty setting.
    public WordScore choose() {
        if (top5.isEmpty()) return null;

        List<WordScore> all = new ArrayList<>(top5);
        all.sort((a, b) -> Integer.compare(b.score, a.score));

        int num = (int)(Math.random() * 2) + 1;

        WordScore chosen;
        if (player.difficulty == 3) {
            chosen = all.get(0);
        } else if (player.difficulty == 2) {
            if (num == 1) chosen = all.size() > 1 ? all.get(1) : all.get(0);
            else chosen = all.size() > 2 ? all.get(2) : (all.size() > 1 ? all.get(1) : all.get(0));
        } else {
            if (num == 1) chosen = all.size() > 3 ? all.get(3) : (all.size() > 2 ? all.get(2) : ((all.size() > 1 ? all.get(1) : all.get(0))));
            else chosen = all.size() > 4 ? all.get(4) : (all.size() > 3 ? all.get(3) : (all.size() > 2 ? all.get(2) : ((all.size() > 1 ? all.get(1) : all.get(0)))));
        }

        top5.remove(chosen);
        return chosen;
    }

    // Returns true if the subarray contains at least one existing board tile.
    public boolean HasLetters(int[] subarray) {
        for (int j = 0; j < subarray.length; j++) {
            if (subarray[j] != -1) {
                return true;
            }
        }
        return false;
    }

    // Returns true if the subarray contains at least one empty slot for a new tile.
    public boolean HasSpaces(int[] subarray) {
        for (int j = 0; j < subarray.length; j++) {
            if (subarray[j] == -1) {
                return true;
            }
        }
        return false;
    }

    // Finds and places the highest-scoring opening word centred on the middle row.
    private void playFirstWord() {
        String bestWord = null;
        int bestScore = -1;
        int bestStart = -1;
        int bestEnd = -1;

        for (int startCol = 0; startCol < 15; startCol++) {
            for (int length = 2; length <= Math.min(7, 15 - startCol); length++) {

                if (startCol > 7 || startCol + length - 1 < 7) continue;

                int[] template = new int[length];
                Arrays.fill(template, -1);

                int boardStart = 7 * 15 + startCol;
                int boardEnd   = 7 * 15 + (startCol + length - 1);

                WordScore ws = findword(template, botLetters, trie, boardStart, boardEnd);

                if (ws != null && ws.score > bestScore) {
                    bestScore = ws.score;
                    bestWord  = ws.word;
                    bestStart = boardStart;
                    bestEnd   = boardEnd;
                }
            }
        }

        if (bestWord != null) {
            place(bestWord, player.getPosition(bestStart, bestEnd));
            UI.opp_score += player.score_calc(bestWord, player.getPosition(bestStart, bestEnd), new int[]{-1,-1}, true);
            player.first = false;
        }
    }

    // Draws new letters from the pool to refill any empty slots in the bot's rack.
    public void drawBotLetters(int[] botLetters) {
        if (UI.letters_left > 0) {
            int temp;
            if (UI.letters_left < 7) temp = UI.letters_left;
            else temp = 7;

            for (int i = 0; i < temp; i++) {
                if (botLetters[i] == 0) {
                    int index = (int)(Math.random() * player.Letters_Array.length);
                    int num = player.Letters_Array[index];

                    while (num == 0) {
                        index = (index + 1) % player.Letters_Array.length;
                        num = player.Letters_Array[index];
                    }
                    player.Letters_Array[index] = 0;
                    botLetters[i] = num;
                }
            }
        }
    }

    // Decides whether the bot should swap its letters or pass when no word can be played.
    public void botSwapOrPass() {
        if (UI.letters_left == 0) {
            if (player.passes == 0) player.window5("The bot has chosen to pass");
            player.passes++;
        } else {
            player.window5("The bot has chosen to replace all letters");
            player.passes = 0;
            replaceBotLetters(botLetters);
        }
        player.yourTurnBox.setText(" Your Turn ");
    }

    // Replaces all bot rack letters with fresh tiles drawn from the pool.
    public void replaceBotLetters(int[] botLetters) {
        for (int i = 0; i < botLetters.length; i++) {
            if (botLetters[i] != 0) {
                for (int j = 0; j < player.Letters_Array.length; j++) {
                    if (player.Letters_Array[j] == 0) {
                        player.Letters_Array[j] = botLetters[i];
                        botLetters[i] = 0;
                        break;
                    }
                }
            }
        }

        int toReplace = Math.min(7, UI.letters_left);
        for (int i = 0; i < toReplace; i++) {
            int index = (int)(Math.random() * player.Letters_Array.length);
            int num = player.Letters_Array[index];
            int attempts = 0;

            while (num == 0) {
                index = (index + 1) % player.Letters_Array.length;
                num = player.Letters_Array[index];
                attempts++;
                if (attempts >= player.Letters_Array.length) break;
            }
            if (num == 0) break;

            player.Letters_Array[index] = 0;
            botLetters[i] = num;
        }
    }

    // Waits for all tile animations to finish, then hands control back to the player.
    public void waitASecond(int placedLetters) {
        int totalAnimationTime = placedLetters * 200 + 1000;
        Timer unfreezeTimer = new Timer(totalAnimationTime, e -> {
            player.yourTurnBox.setText(" Your Turn ");
            player.unfreezeBoard();
            player.isEnd();
        });
        unfreezeTimer.setRepeats(false);
        unfreezeTimer.start();
    }

    // Refreshes the letters-left label in the UI.
    public void updateLetters() {
        if (UI.letters_left < 10) player.lettersLeftBox.setText(" Letters Left: " + UI.letters_left + "  ");
        else player.lettersLeftBox.setText(" Letters Left: " + UI.letters_left + " ");
    }

    // Loads an image icon for a tile from the images resource folder.
    protected ImageIcon loadIcon(String filename) {
        java.net.URL url = getClass().getResource("/images/" + filename);
        if (url != null) return new ImageIcon(url);
        return new ImageIcon("images/" + filename);
    }
}