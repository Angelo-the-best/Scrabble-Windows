import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

public class UI {

    Color bg = new Color(249,247,243);
    Color blue1 = new Color(102, 204, 255);
    Color blue2 = new Color(0, 153, 255);
    Color blue3 = new Color(76, 156, 196);
    Color white = new Color(255,255,255);
    Color black = new Color(0,0,0);
    protected static JLabel[][] cells = new JLabel[15][15];
    protected static int your_score = 0;
    protected static int opp_score = 0;
    protected static int letters_left = 100;
    protected static int difficulty;
    public JTable scoreTable;
    public JPanel letters;
    public JLabel lettersLeftBox;
    public JLabel yourTurnBox;
    public JFrame frame3;
    public JButton exitButton;
    private javax.swing.Timer resizeTimer = null;
    protected static int[] Letters_Array = {
        1,1,1,1,1,1,1,1,1,2,2,3,3,4,4,4,4,5,5,5,5,5,5,5,5,5,5,5,5,6,6,
        7,7,7,8,8,9,9,9,9,9,9,9,9,9,10,11,12,12,12,12,13,13,14,14,14,14,14,14,
        15,15,15,15,15,15,15,15,16,16,17,18,18,18,18,18,18,19,19,19,19,
        20,20,20,20,20,20,21,21,21,21,22,22,23,23,24,25,25,26,27,27
    };

    public UI() {
    }

    protected ImageIcon loadIcon(String filename) {
        URL url = getClass().getResource("/images/" + filename);
        if (url != null) return new ImageIcon(url);
        return new ImageIcon("resources/images/" + filename);
    }

    public void window1() {

        JFrame frame1 = new JFrame("Computer Science Internal Assessment");
        frame1.setFont(new Font("Menlo", Font.BOLD, 10));
        frame1.setSize(600, 450);
        frame1.setLocationRelativeTo(null);
        frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final ImageIcon titleRaw = loadIcon("title.png");
        JLabel titleLabel = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                int imgW = titleRaw.getIconWidth();
                int imgH = titleRaw.getIconHeight();
                if (imgW <= 0 || imgH <= 0) return;
                Insets ins = getInsets();
                int availW = getWidth()  - ins.left - ins.right;
                int availH = getHeight() - ins.top  - ins.bottom;
                double sc  = Math.min((double) availW / imgW, (double) availH / imgH);
                int drawW  = (int)(imgW * sc);
                int drawH  = (int)(imgH * sc);
                int drawX  = ins.left + (availW - drawW) / 2;
                int drawY  = ins.top  + (availH - drawH) / 2;
                g2.drawImage(titleRaw.getImage(), drawX, drawY, drawW, drawH, this);
            }
        };
        titleLabel.setBorder(BorderFactory.createEmptyBorder(50, 30, 0, 30));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setPreferredSize(new Dimension(538, 136));

        final JButton playButton = new JButton(" Play ");
        styleButton(playButton, blue2, blue1, true, 20);
        playButton.addActionListener(e -> { frame1.dispose(); window2(); });

        exitButton = new JButton(" Exit ");
        styleButton(exitButton, blue2, blue1, true, 20);
        exitButton.addActionListener(e -> System.exit(0));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setOpaque(false);
        buttonPanel.add(Box.createVerticalGlue());
        playButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.add(playButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonPanel.add(exitButton);
        buttonPanel.add(Box.createVerticalGlue());

        JPanel background = new JPanel(new BorderLayout()) {
            private final Image bgImg = loadIcon("bg1.png").getImage();
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(bgImg, 0, 0, getWidth(), getHeight(), this);
            }
        };
        background.setOpaque(true);
        background.add(titleLabel, BorderLayout.NORTH);
        background.add(buttonPanel, BorderLayout.CENTER);

        frame1.setContentPane(background);
        frame1.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                int fw = frame1.getWidth(), fh = frame1.getHeight();
                double sc = Math.max(0.4, Math.min(fw / 600.0, fh / 450.0));
                int bf = Math.max(10, (int)(20 * sc));
                playButton.setFont(new Font("Menlo", Font.BOLD, bf));
                exitButton.setFont(new Font("Menlo", Font.BOLD, bf));
                titleLabel.setPreferredSize(new Dimension(fw, Math.max(60, (int)(136 * sc))));
                frame1.revalidate();
            }
        });
        frame1.setVisible(true);
    }

    public void window2() {

        JFrame frame2 = new JFrame("Select Difficulty");
        frame2.setFont(new Font("Menlo", Font.BOLD, 10));
        frame2.setSize(300, 315);  // increased for Windows
        frame2.setLocationRelativeTo(null);
        frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel2 = new JPanel();
        panel2.setLayout(new BorderLayout());
        panel2.setBackground(blue1);
        panel2.setOpaque(true);
        frame2.setContentPane(panel2);

        JButton easy = new JButton(" Easy ");
        styleButton(easy, blue2, blue1, true, 17);
        easy.addActionListener(e -> { difficulty = 1; frame2.dispose(); window3(); });

        JButton medium = new JButton(" Medium ");
        styleButton(medium, blue2, blue1, true, 17);
        medium.addActionListener(e -> { difficulty = 2; frame2.dispose(); window3(); });

        JButton hard = new JButton(" Hard ");
        styleButton(hard, blue2, blue1, true, 17);
        hard.addActionListener(e -> { difficulty = 3; frame2.dispose(); window3(); });

        JPanel buttonPanel2 = new JPanel();
        buttonPanel2.setLayout(new BoxLayout(buttonPanel2, BoxLayout.Y_AXIS));
        buttonPanel2.setOpaque(false);
        buttonPanel2.add(Box.createVerticalGlue());
        easy.setAlignmentX(Component.CENTER_ALIGNMENT);
        medium.setAlignmentX(Component.CENTER_ALIGNMENT);
        hard.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel2.add(easy);
        buttonPanel2.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonPanel2.add(medium);
        buttonPanel2.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonPanel2.add(hard);
        buttonPanel2.add(Box.createVerticalGlue());

        frame2.add(buttonPanel2, BorderLayout.CENTER);
        frame2.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                int fw = frame2.getWidth(), fh = frame2.getHeight();
                double sc = Math.max(0.4, Math.min(fw / 300.0, fh / 225.0));
                int bf = Math.max(10, (int)(17 * sc));
                easy.setFont(new Font("Menlo", Font.BOLD, bf));
                medium.setFont(new Font("Menlo", Font.BOLD, bf));
                hard.setFont(new Font("Menlo", Font.BOLD, bf));
                frame2.revalidate();
            }
        });
        frame2.setVisible(true);
    }

    public void window3() {

        frame3 = new JFrame("Game Board");
        frame3.setSize(1300, 928);
        frame3.setLocationRelativeTo(null);
        frame3.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final ImageIcon bg2Raw = loadIcon("bg2.png");
        JPanel bigbackground = new JPanel(new BorderLayout()) {
            private final Image bgImg = bg2Raw.getImage();
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.drawImage(bgImg, 0, 0, getWidth(), getHeight(), this);
            }
        };
        bigbackground.setOpaque(true);
        frame3.setContentPane(bigbackground);

        scoreTable = new JTable(new Object[][]{
            {" Player Score", your_score + " "},
            {" Opponent Score", opp_score + " "}
        }, new String[]{"Player", "Score"});

        scoreTable.setFont(new Font("Menlo", Font.BOLD, 18));
        scoreTable.setForeground(blue2);
        scoreTable.setBackground(Color.WHITE);
        scoreTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        scoreTable.setRowHeight(35);
        scoreTable.getColumnModel().getColumn(0).setPreferredWidth(140);
        scoreTable.getColumnModel().getColumn(1).setPreferredWidth(60);
        scoreTable.getTableHeader().setFont(new Font("Menlo", Font.BOLD, 20));
        scoreTable.getTableHeader().setForeground(blue2);
        scoreTable.getTableHeader().setBackground(blue1);
        scoreTable.setBorder(BorderFactory.createMatteBorder(4, 4, 4, 4, blue2));
        scoreTable.setGridColor(Color.WHITE);
        scoreTable.setEnabled(false);

        scoreTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                ((JLabel) c).setHorizontalAlignment(SwingConstants.RIGHT);
                c.setFont(new Font("Menlo", Font.BOLD, 18));
                c.setForeground(blue2);
                return c;
            }
        });

        lettersLeftBox = new JLabel(" Letters Left: " + letters_left + " ");
        lettersLeftBox.setFont(new Font("Menlo", Font.BOLD, 18));
        lettersLeftBox.setForeground(Color.WHITE);
        lettersLeftBox.setBackground(blue2);
        lettersLeftBox.setOpaque(true);
        lettersLeftBox.setPreferredSize(new Dimension(200, 35));
        lettersLeftBox.setHorizontalAlignment(SwingConstants.CENTER);
        lettersLeftBox.setBorder(BorderFactory.createMatteBorder(4, 4, 4, 4, blue2));

        yourTurnBox = new JLabel(" Your Turn ");
        yourTurnBox.setFont(new Font("Menlo", Font.BOLD, 18));
        yourTurnBox.setForeground(Color.WHITE);
        yourTurnBox.setBackground(blue2);
        yourTurnBox.setOpaque(true);
        yourTurnBox.setHorizontalAlignment(SwingConstants.CENTER);
        yourTurnBox.setBorder(BorderFactory.createMatteBorder(4, 4, 4, 4, blue2));

        JPanel scoreLetterPanel = new JPanel();
        scoreLetterPanel.setLayout(new BoxLayout(scoreLetterPanel, BoxLayout.Y_AXIS));
        scoreLetterPanel.setOpaque(false);
        scoreLetterPanel.add(scoreTable);
        scoreLetterPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        scoreLetterPanel.add(lettersLeftBox);
        scoreLetterPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        scoreLetterPanel.add(yourTurnBox);
        scoreLetterPanel.setBorder(BorderFactory.createEmptyBorder(30, 15, 15, 15));

        JButton submit = new JButton(" Submit ");
        styleButton(submit, blue2, blue1, true, 20);
        submit.addActionListener(e -> submit());

        JButton replace = new JButton(" Replace ");
        styleButton(replace, blue2, blue1, true, 20);
        replace.addActionListener(e -> {
            if (checktiles()) {
                if (letters_left == 0) window5("<html><center>Error: No letters left to replace!</center></html>");
                else replacewindow();
            }
        });

        JButton pass = new JButton(" Pass ");
        styleButton(pass, blue2, blue1, true, 20);
        pass.addActionListener(e -> {
            if (checktiles()) pass();
        });

        JButton exit = new JButton(" Exit ");
        styleButton(exit, blue2, blue1, true, 20);
        exit.addActionListener(e -> {
            try {
                ProcessBuilder pb = new ProcessBuilder("java", "-cp", System.getProperty("java.class.path"), "PlayerLogic");
                pb.inheritIO();
                pb.start();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            System.exit(0);
        });

        JPanel buttonPanel3 = new JPanel();
        buttonPanel3.setLayout(new BoxLayout(buttonPanel3, BoxLayout.Y_AXIS));
        buttonPanel3.setOpaque(false);
        buttonPanel3.add(Box.createVerticalGlue());
        submit.setAlignmentX(Component.CENTER_ALIGNMENT);
        exit.setAlignmentX(Component.CENTER_ALIGNMENT);
        replace.setAlignmentX(Component.CENTER_ALIGNMENT);
        pass.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel3.add(submit);
        buttonPanel3.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonPanel3.add(replace);
        buttonPanel3.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonPanel3.add(pass);
        buttonPanel3.add(Box.createRigidArea(new Dimension(0, 40)));
        buttonPanel3.add(exit);
        buttonPanel3.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 35));
        buttonPanel3.add(Box.createVerticalGlue());

        JPanel boardPanel = new JPanel(new GridLayout(15, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Image boardImg = loadIcon("board.png").getImage();
                g.drawImage(boardImg, 0, 0, getWidth(), getHeight(), this);
            }
        };
        boardPanel.setPreferredSize(new Dimension(750, 750));
        boardPanel.setMinimumSize(new Dimension(750, 750));
        boardPanel.setMaximumSize(new Dimension(750, 750));
        boardPanel.setOpaque(false);

        for (int r = 0; r < 15; r++) {
            for (int c = 0; c < 15; c++) {
                JLabel cell = new JLabel();
                cell.setHorizontalAlignment(SwingConstants.CENTER);
                cell.setVerticalAlignment(SwingConstants.CENTER);
                cell.setOpaque(false);
                cell.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0, 30)));
                cell.setTransferHandler(new CellTransferHandler());

                cell.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (cell.getIcon() != null && cell.getTransferHandler() != null)
                            cell.getTransferHandler().exportAsDrag(cell, e, TransferHandler.MOVE);
                    }
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (cell.getIcon() == null)
                            cell.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 2));
                    }
                    @Override
                    public void mouseExited(MouseEvent e) {
                        if (cell.getClientProperty("isBlank") != null)
                            cell.setBorder(BorderFactory.createLineBorder(new Color(0, 220, 220), 3));
                        else
                            cell.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0, 30)));
                    }
                });

                cells[r][c] = cell;
                boardPanel.add(cell);
            }
        }

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.add(boardPanel, new GridBagConstraints());
        wrapper.setOpaque(false);

        letters = new JPanel();
        letters.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        letters.setOpaque(false);

        for (int i = 0; i < 7; i++) {
            int index = (int)(Math.random() * Letters_Array.length);
            int num = Letters_Array[index];
            while (num == 0) {
                index = (index + 1) % Letters_Array.length;
                num = Letters_Array[index];
            }
            Letters_Array[index] = 0;
            letters_left -= 2;
            lettersLeftBox.setText(" Letters Left: " + letters_left + " ");

            String filename = num + ".png";
            ImageIcon icon = loadIcon(filename);
            Image scaled = icon.getImage().getScaledInstance(45, 45, Image.SCALE_SMOOTH);

            JLabel slot = new JLabel(new ImageIcon(scaled));
            slot.setName(filename);
            slot.setPreferredSize(new Dimension(50, 50));
            slot.setHorizontalAlignment(SwingConstants.CENTER);
            slot.setVerticalAlignment(SwingConstants.CENTER);
            slot.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0, 30)));

            slot.setTransferHandler(new TransferHandler() {
                @Override
                protected Transferable createTransferable(JComponent c) {
                    JLabel label = (JLabel) c;
                    if (label.getIcon() != null) {
                        ImageIcon icon = (ImageIcon) label.getIcon();
                        Image img = icon.getImage();
                        String filename = label.getName();
                        setDragImage(img);
                        setDragImageOffset(new Point(img.getWidth(null) / 2, img.getHeight(null) / 2));
                        return new LetterTransferable(img, filename);
                    }
                    return null;
                }
                @Override
                public int getSourceActions(JComponent c) { return MOVE; }
                @Override
                protected void exportDone(JComponent source, Transferable data, int action) {
                    if (action == MOVE) ((JLabel) source).setIcon(null);
                }
            });

            slot.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (slot.getIcon() != null)
                        slot.getTransferHandler().exportAsDrag(slot, e, TransferHandler.MOVE);
                }
            });

            letters.add(slot);
        }

        JPanel boardWithLetters = new JPanel();
        boardWithLetters.setLayout(new BorderLayout());
        boardWithLetters.setOpaque(false);
        boardWithLetters.add(wrapper, BorderLayout.CENTER);
        boardWithLetters.add(letters, BorderLayout.SOUTH);

        bigbackground.add(boardWithLetters, BorderLayout.CENTER);
        bigbackground.add(buttonPanel3, BorderLayout.EAST);
        bigbackground.add(scoreLetterPanel, BorderLayout.WEST);

        // ── Resize listener with 50ms throttle ────────────────────────────
        frame3.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                if (resizeTimer != null && resizeTimer.isRunning()) resizeTimer.stop();
                resizeTimer = new javax.swing.Timer(50, evt -> {
                    ((javax.swing.Timer) evt.getSource()).stop();

                    int fw = frame3.getWidth(), fh = frame3.getHeight();
                    double sc = Math.max(0.3, Math.min(fw / 1300.0, fh / 900.0));

                    int bs = (Math.round((int)(750 * sc) / 15)) * 15;
                    if (bs < 15) bs = 15;
                    boardPanel.setPreferredSize(new Dimension(bs, bs));
                    boardPanel.setMinimumSize(new Dimension(bs, bs));
                    boardPanel.setMaximumSize(new Dimension(bs, bs));

                    int cellSz = Math.max(10, (int)((bs / 15) * 0.9));
                    for (int r = 0; r < 15; r++) {
                        for (int c2 = 0; c2 < 15; c2++) {
                            JLabel cell = cells[r][c2];
                            String fname = cell.getName();
                            if (cell.getIcon() != null && fname != null && !fname.isEmpty()) {
                                Image img = loadIcon(fname).getImage()
                                    .getScaledInstance(cellSz, cellSz, Image.SCALE_SMOOTH);
                                cell.setIcon(new ImageIcon(img));
                            }
                        }
                    }

                    int tf = Math.max(8, (int)(18 * sc));
                    scoreTable.setFont(new Font("Menlo", Font.BOLD, tf));
                    scoreTable.setRowHeight(Math.max(15, (int)(35 * sc)));
                    scoreTable.getTableHeader().setFont(
                        new Font("Menlo", Font.BOLD, Math.max(8, (int)(20 * sc))));
                    scoreTable.getColumnModel().getColumn(0).setPreferredWidth((int)(140 * sc));
                    scoreTable.getColumnModel().getColumn(1).setPreferredWidth((int)(60  * sc));

                    lettersLeftBox.setFont(new Font("Menlo", Font.BOLD, tf));
                    lettersLeftBox.setPreferredSize(
                        new Dimension((int)(200 * sc), (int)(35 * sc)));
                    yourTurnBox.setFont(new Font("Menlo", Font.BOLD, tf));

                    int tileSz = cellSz;
                    for (Component comp : letters.getComponents()) {
                        if (comp instanceof JLabel) {
                            JLabel lbl = (JLabel) comp;
                            lbl.setPreferredSize(new Dimension(tileSz + 5, tileSz + 5));
                            String fname = lbl.getName();
                            if (fname != null && !fname.isEmpty() && lbl.getIcon() != null) {
                                Image img = loadIcon(fname).getImage()
                                    .getScaledInstance(tileSz, tileSz, Image.SCALE_SMOOTH);
                                lbl.setIcon(new ImageIcon(img));
                            }
                        }
                    }
                    letters.revalidate();

                    int bf = Math.max(10, (int)(20 * sc));
                    submit.setFont(new Font("Menlo", Font.BOLD, bf));
                    replace.setFont(new Font("Menlo", Font.BOLD, bf));
                    pass.setFont(new Font("Menlo", Font.BOLD, bf));
                    exit.setFont(new Font("Menlo", Font.BOLD, bf));

                    frame3.revalidate();
                    frame3.repaint();
                });
                resizeTimer.setRepeats(false);
                resizeTimer.start();
            }
        });

        // ── Trigger initial layout when window first opens ─────────────────
        frame3.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                frame3.dispatchEvent(new java.awt.event.ComponentEvent(
                    frame3, java.awt.event.ComponentEvent.COMPONENT_RESIZED));
            }
        });

        frame3.setVisible(true);
    }

    public void window4() {
        if (your_score > opp_score)      EndAnimations.playWin(frame3);
        else if (your_score < opp_score) EndAnimations.playLose(frame3);
        else                             EndAnimations.playDraw(frame3);
    }

    public void window5(String message) {

        freezeBoard();

        JFrame frame5 = new JFrame("Message Window");
        frame5.setSize(300, 315);  // increased for Windows
        frame5.setLocationRelativeTo(null);
        frame5.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel5 = new JPanel();
        panel5.setLayout(new BorderLayout());
        panel5.setBackground(blue1);
        panel5.setOpaque(true);
        frame5.setContentPane(panel5);

        JLabel errortext = new JLabel("<html><center>Message:<br><br>" + message + "</center></html>");
        errortext.setBorder(BorderFactory.createEmptyBorder(30, 30, 15, 15));
        errortext.setFont(new Font("Menlo", Font.BOLD, 17));
        errortext.setForeground(blue2);
        errortext.setHorizontalAlignment(SwingConstants.CENTER);
        panel5.add(errortext, BorderLayout.NORTH);

        JButton errorButton = new JButton(" Go back ");
        styleButton(errorButton, blue2, blue1, true, 20);
        errorButton.addActionListener(e -> {
            unfreezeBoard();
            frame5.dispose();
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setOpaque(false);
        buttonPanel.add(Box.createVerticalGlue());
        errorButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.add(errorButton);
        buttonPanel.add(Box.createVerticalGlue());
        panel5.add(buttonPanel, BorderLayout.CENTER);

        frame5.setVisible(true);
    }

    public void replacewindow() {

        freezeBoard();

        JFrame frame6 = new JFrame("Replace Window");
        frame6.setSize(350, 308);  // increased for Windows
        frame6.setLocationRelativeTo(null);
        frame6.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel6 = new JPanel();
        panel6.setLayout(new BorderLayout());
        panel6.setBackground(blue1);
        panel6.setOpaque(true);
        frame6.setContentPane(panel6);

        JLabel replacetext = new JLabel("<html><center>How many letters do you want to replace?</center></html>", SwingConstants.CENTER);
        replacetext.setFont(new Font("Menlo", Font.BOLD, 17));
        replacetext.setForeground(blue2);
        replacetext.setBorder(BorderFactory.createEmptyBorder(25, 10, 10, 10));
        panel6.add(replacetext, BorderLayout.NORTH);

        JTextField field = new JTextField();
        field.setHorizontalAlignment(JTextField.CENTER);
        field.setFont(new Font("Menlo", Font.BOLD, 17));
        field.setBackground(white);
        field.setForeground(blue2);
        field.setBorder(BorderFactory.createMatteBorder(4, 4, 4, 4, blue2));
        field.setPreferredSize(new Dimension(80, 50));

        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.add(field);
        panel6.add(centerPanel, BorderLayout.CENTER);

        JButton confirm = new JButton(" Confirm ");
        styleButton(confirm, blue2, blue1, true, 16);
        confirm.addActionListener(e -> {
            String input = field.getText().trim();
            int count;
            try {
                count = Integer.parseInt(input);
            } catch (NumberFormatException ex) {
                replacetext.setText("<html><center>Please enter a number <br> between one and seven!</center></html>");
                field.setText("");
                return;
            }

            int available = 0;
            for (int i = 0; i < 7; i++) {
                JLabel label = (JLabel) letters.getComponent(i);
                if (label.getIcon() != null) available++;
            }

            if (count < 1 || count > available) {
                replacetext.setText("<html><center>Please enter a number <br> between one and seven!</center></html>");
                field.setText("");
                return;
            }

            if (count > letters_left) {
                replacetext.setText("<html><center>Not enough <br> letters left</center></html>");
                field.setText("");
                return;
            }

            frame6.dispose();
            if (count == 7) replace1(new int[]{0, 1, 2, 3, 4, 5, 6});
            else window6(count);
        });

        JButton cancel = new JButton(" Cancel ");
        styleButton(cancel, blue2, blue1, true, 16);
        cancel.addActionListener(e -> {
            unfreezeBoard();
            frame6.dispose();
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.add(confirm);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPanel.add(cancel);
        panel6.add(buttonPanel, BorderLayout.SOUTH);

        frame6.getRootPane().setDefaultButton(confirm);
        frame6.setVisible(true);
    }

    public void window6(int count) {

        JFrame replaceFrame = new JFrame("");
        replaceFrame.setSize(450, 280);  // increased for Windows
        replaceFrame.setLocationRelativeTo(null);
        replaceFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(blue1);
        replaceFrame.setContentPane(panel);

        JLabel instructions = new JLabel(
            "<html><center>Click " + count + " letter(s) to replace:</center></html>",
            SwingConstants.CENTER);
        instructions.setFont(new Font("Menlo", Font.BOLD, 17));
        instructions.setForeground(blue2);
        instructions.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
        panel.add(instructions, BorderLayout.NORTH);

        JPanel rackDisplay = new JPanel();
        rackDisplay.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        rackDisplay.setOpaque(false);

        ArrayList<Integer> selectedIndices = new ArrayList<>();
        ArrayList<JLabel> clickableLabels = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            final int index = i;
            JLabel originalLabel = (JLabel) letters.getComponent(i);

            if (originalLabel.getIcon() != null) {
                JLabel clickableLabel = new JLabel(originalLabel.getIcon());
                clickableLabel.setPreferredSize(new Dimension(50, 50));
                clickableLabel.setBorder(BorderFactory.createLineBorder(blue2, 2));
                clickableLabel.setHorizontalAlignment(SwingConstants.CENTER);
                clickableLabel.setVerticalAlignment(SwingConstants.CENTER);
                clickableLabels.add(clickableLabel);

                clickableLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (!selectedIndices.contains(index)) {
                            clickableLabel.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 3));
                            clickableLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                        }
                    }
                    @Override
                    public void mouseExited(MouseEvent e) {
                        if (!selectedIndices.contains(index)) {
                            clickableLabel.setBorder(BorderFactory.createLineBorder(blue2, 2));
                            clickableLabel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                        }
                    }
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (selectedIndices.contains(index)) {
                            selectedIndices.remove(Integer.valueOf(index));
                            clickableLabel.setBorder(BorderFactory.createLineBorder(blue2, 2));
                            instructions.setText("<html><center>Click " + (count - selectedIndices.size()) + " more letter(s) to replace:</center></html>");
                        } else {
                            if (selectedIndices.size() < count) {
                                selectedIndices.add(index);
                                clickableLabel.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 3));
                                int remaining = count - selectedIndices.size();
                                if (remaining > 0)
                                    instructions.setText("<html><center>Click " + remaining + " more letter(s) to replace:</center></html>");
                                else
                                    instructions.setText("<html><center>Press Confirm to replace!</center></html>");
                            }
                        }
                    }
                });

                rackDisplay.add(clickableLabel);
            }
        }

        panel.add(rackDisplay, BorderLayout.CENTER);

        JButton confirmButton = new JButton(" Confirm ");
        styleButton(confirmButton, blue2, blue1, true, 16);
        confirmButton.addActionListener(e -> {
            if (selectedIndices.size() != count) {
                instructions.setText("<html><center>Please select exactly " + count + " letter(s)!</center></html>");
                return;
            }
            int[] indices = selectedIndices.stream().mapToInt(Integer::intValue).toArray();
            unfreezeBoard();
            replaceFrame.dispose();
            replace1(indices);
        });

        JButton cancelButton = new JButton(" Cancel ");
        styleButton(cancelButton, blue2, blue1, true, 16);
        cancelButton.addActionListener(e -> {
            unfreezeBoard();
            replaceFrame.dispose();
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.add(confirmButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        replaceFrame.setVisible(true);
    }

    public void submit() {}
    public void replace1(int[] rackIndices) {}
    public void pass() {}
    public boolean checktiles() { return true; }

    public char ask() {

        freezeBoard();

        final char[] result = new char[1];

        JDialog dialog = new JDialog(frame3, "Blank Tile", true);
        dialog.setSize(350, 308);  // increased for Windows
        dialog.setLocationRelativeTo(frame3);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(blue1);
        panel.setOpaque(true);
        dialog.setContentPane(panel);

        JLabel text = new JLabel("<html><center>You placed a blank tile!<br>Please specify your letter</center></html>");
        text.setFont(new Font("Menlo", Font.BOLD, 17));
        text.setForeground(blue2);
        text.setHorizontalAlignment(SwingConstants.CENTER);
        text.setBorder(BorderFactory.createEmptyBorder(25, 10, 10, 10));
        panel.add(text, BorderLayout.NORTH);

        JTextField field = new JTextField();
        field.setHorizontalAlignment(JTextField.CENTER);
        field.setFont(new Font("Menlo", Font.BOLD, 17));
        field.setBackground(white);
        field.setForeground(blue2);
        field.setBorder(BorderFactory.createMatteBorder(4, 4, 4, 4, blue2));
        field.setPreferredSize(new Dimension(80, 50));

        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.add(field);
        panel.add(centerPanel, BorderLayout.CENTER);

        JButton confirm = new JButton(" Confirm ");
        styleButton(confirm, blue2, blue1, true, 18);
        confirm.addActionListener(e -> {
            String input = field.getText().trim();
            if (input.length() == 1 && Character.isLetter(input.charAt(0))) {
                result[0] = Character.toUpperCase(input.charAt(0));
                unfreezeBoard();
                dialog.dispose();
            } else {
                text.setText("<html><center>Please enter <br> exactly one letter!</center></html>");
                field.setText("");
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.add(confirm);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.getRootPane().setDefaultButton(confirm);
        dialog.setVisible(true);

        return result[0];
    }

    public void freezeBoard() {
        for (int r = 0; r < 15; r++)
            for (int c = 0; c < 15; c++)
                cells[r][c].setTransferHandler(null);

        for (Component comp : letters.getComponents())
            if (comp instanceof JLabel)
                ((JLabel) comp).setTransferHandler(null);

        disableButtons(frame3.getContentPane(), true);
    }

    public void unfreezeBoard() {
        for (int r = 0; r < 15; r++) {
            for (int c = 0; c < 15; c++) {
                if (cells[r][c].getBackground().equals(new Color(200, 200, 200)))
                    cells[r][c].setTransferHandler(null);
                else
                    cells[r][c].setTransferHandler(new CellTransferHandler());
            }
        }

        for (Component comp : letters.getComponents()) {
            if (comp instanceof JLabel) {
                ((JLabel) comp).setTransferHandler(new TransferHandler() {
                    @Override
                    protected Transferable createTransferable(JComponent c) {
                        JLabel label = (JLabel) c;
                        if (label.getIcon() != null) {
                            ImageIcon icon = (ImageIcon) label.getIcon();
                            Image img = icon.getImage();
                            String filename = label.getName();
                            setDragImage(img);
                            setDragImageOffset(new Point(img.getWidth(null) / 2, img.getHeight(null) / 2));
                            return new LetterTransferable(img, filename);
                        }
                        return null;
                    }
                    @Override
                    public int getSourceActions(JComponent c) { return MOVE; }
                    @Override
                    protected void exportDone(JComponent source, Transferable data, int action) {
                        if (action == MOVE) ((JLabel) source).setIcon(null);
                    }
                });
            }
        }

        disableButtons(frame3.getContentPane(), false);
    }

    protected void disableButtons(Container container, boolean disable) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                if (button.getText().trim().equals("Exit"))
                    button.setEnabled(true);
                else
                    button.setEnabled(!disable);
            } else if (comp instanceof Container) {
                disableButtons((Container) comp, disable);
            }
        }
    }

    private void styleButton(JButton button, Color base, Color border, boolean have_border, int size) {
        button.setFont(new Font("Menlo", Font.BOLD, size));
        button.setBackground(border);
        button.setForeground(base);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(border, 4, true),
            BorderFactory.createMatteBorder(0, 0, 4, 4, new Color(0, 0, 0, 60))
        ));
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setContentAreaFilled(true);
        button.setBorderPainted(have_border);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) { button.setForeground(base.brighter()); }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) { button.setForeground(base); }
        });
    }

    class LetterTile extends JLabel {
        String letter;

        public LetterTile(String letter, ImageIcon icon) {
            super(icon);
            this.letter = letter;

            Image scaled = icon.getImage().getScaledInstance(45, 45, Image.SCALE_SMOOTH);
            setIcon(new ImageIcon(scaled));
            setPreferredSize(new Dimension(50, 50));

            TransferHandler th = new TransferHandler("icon") {
                @Override
                protected Transferable createTransferable(JComponent c) {
                    JLabel label = (JLabel) c;
                    if (label.getIcon() != null) {
                        ImageIcon icon = (ImageIcon) label.getIcon();
                        Image img = icon.getImage();
                        String filename = label.getName();
                        setDragImage(img);
                        setDragImageOffset(new Point(img.getWidth(null) / 2, img.getHeight(null) / 2));
                        return new LetterTransferable(img, filename);
                    }
                    return null;
                }
                @Override
                public int getSourceActions(JComponent c) { return MOVE; }
                @Override
                protected void exportDone(JComponent source, Transferable data, int action) {
                    if (action == MOVE) ((JLabel) source).setIcon(null);
                }
            };

            setTransferHandler(th);
            th.setDragImage(((ImageIcon) getIcon()).getImage());
            th.setDragImageOffset(new Point(25, 25));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (getIcon() != null)
                        getTransferHandler().exportAsDrag(LetterTile.this, e, TransferHandler.MOVE);
                }
            });
        }
    }

    class ImageSelection implements Transferable {
        private ImageIcon icon;
        public ImageSelection(ImageIcon icon) { this.icon = icon; }

        @Override
        public DataFlavor[] getTransferDataFlavors() { return new DataFlavor[]{DataFlavor.imageFlavor}; }
        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) { return flavor.equals(DataFlavor.imageFlavor); }
        @Override
        public Object getTransferData(DataFlavor flavor) { return icon.getImage(); }
    }

    class LetterTransferable implements Transferable {
        private Image image;
        private String filename;

        public LetterTransferable(Image image, String filename) {
            this.image = image;
            this.filename = filename;
        }

        public String getFilename() { return filename; }

        @Override
        public DataFlavor[] getTransferDataFlavors() { return new DataFlavor[]{DataFlavor.imageFlavor}; }
        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) { return flavor.equals(DataFlavor.imageFlavor); }
        @Override
        public Object getTransferData(DataFlavor flavor) { return image; }
    }

    class CellTransferHandler extends TransferHandler {

        @Override
        public boolean canImport(TransferSupport support) {
            if (!support.isDataFlavorSupported(DataFlavor.imageFlavor)) return false;
            JLabel target = (JLabel) support.getComponent();
            return target.getIcon() == null;
        }

        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) return false;
            try {
                Transferable t = support.getTransferable();
                Image img = (Image) t.getTransferData(DataFlavor.imageFlavor);
                JLabel target = (JLabel) support.getComponent();
                target.setIcon(new ImageIcon(img));
                if (t instanceof LetterTransferable)
                    target.setName(((LetterTransferable) t).getFilename());
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            JLabel label = (JLabel) c;
            if (label.getIcon() != null) {
                ImageIcon icon = (ImageIcon) label.getIcon();
                Image img = icon.getImage();
                setDragImage(img);
                setDragImageOffset(new Point(img.getWidth(null) / 2, img.getHeight(null) / 2));
                return new LetterTransferable(img, label.getName());
            }
            return null;
        }

        @Override
        public int getSourceActions(JComponent c) { return MOVE; }

        @Override
        protected void exportDone(JComponent source, Transferable data, int action) {
            if (action == MOVE) ((JLabel) source).setIcon(null);
        }
    }

    public static void main(String[] args) {
        UI ui = new UI();
        ui.window1();
    }
}

// DA END