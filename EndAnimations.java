import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.*;

public class EndAnimations {

    // Triggers the win confetti overlay on the given frame.
    public static void playWin(JFrame parent)  { showConfetti(parent, "win");  }

    // Triggers the draw confetti overlay on the given frame.
    public static void playDraw(JFrame parent) { showConfetti(parent, "draw"); }

    // Triggers the lose robot-dance overlay on the given frame.
    public static void playLose(JFrame parent) { showRobot(parent);            }

    // Restarts the application by launching a new process and disposing the current frame.
    private static void relaunch(JFrame parent) {
        try {
            ProcessBuilder pb = new ProcessBuilder("java", "-cp",
                    System.getProperty("java.class.path"), "PlayerLogic");
            pb.inheritIO();
            pb.start();
        } catch (IOException ex) { ex.printStackTrace(); }
        parent.dispose();
        System.exit(0);
    }

    // Returns a UI scale factor derived from the current overlay dimensions.
    private static double overlayScale(int pw, int ph) {
        return Math.max(0.3, Math.min(pw / 1300.0, ph / 900.0));
    }

    // Displays an animated confetti overlay with a win or draw message and a restart prompt.
    private static void showConfetti(JFrame parent, String mode) {

        boolean isWin   = mode.equals("win");
        String  msg     = isWin ? "YOU WIN!" : "IT'S A DRAW!";
        Color   fillCol = isWin ? new Color(255, 220, 50)  : new Color(210, 210, 210);
        Color   rimCol  = isWin ? new Color(255, 120,  0)  : new Color( 80,  80,  80);

        int    dif     = UI.your_score - UI.opp_score;
        String comment = !isWin ? "Close indeed"
                       : dif > 50 ? "You would probably survive when AI takes over"
                                  : "Now that's impressive";

        Random rand = new Random();
        List<ConfettiParticle> particles = new ArrayList<>();
        int n = isWin ? 180 : 120;
        int initW = parent.getWidth(), initH = parent.getHeight();
        for (int i = 0; i < n; i++) {
            Color c;
            if (isWin) {
                Color[] wc = { new Color(255,50,50), new Color(50,200,50), new Color(50,100,255),
                               new Color(255,200,0), new Color(255,0,200), new Color(0,220,220),
                               new Color(255,140,0) };
                c = wc[rand.nextInt(wc.length)];
            } else {
                Color[] dc = { new Color(160,160,160), new Color(190,190,190), new Color(130,130,140),
                               new Color(200,200,200), new Color(110,110,110) };
                c = dc[rand.nextInt(dc.length)];
            }
            particles.add(new ConfettiParticle(rand.nextInt(initW), -rand.nextInt(400), c, rand));
        }

        final int[]   screenW   = { initW };
        final int[]   screenH   = { initH };
        final float[] drawScale = { 1.0f  };

        JLabel msgLabel = new JLabel(msg, SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(msg)) / 2;
                int ty = fm.getAscent();
                int o  = 4;
                g2.setColor(Color.BLACK);
                for (int dx = -o; dx <= o; dx += 2)
                    for (int dy = -o; dy <= o; dy += 2)
                        if (dx != 0 || dy != 0) g2.drawString(msg, tx+dx, ty+dy);
                g2.setColor(rimCol);  g2.drawString(msg, tx-2, ty-2); g2.drawString(msg, tx+2, ty+2);
                g2.setColor(fillCol); g2.drawString(msg, tx, ty);
            }
        };
        JLabel commentLabel = new JLabel(comment, SwingConstants.CENTER);
        commentLabel.setForeground(new Color(230, 230, 230));
        JLabel hint = new JLabel("Click anywhere to restart", SwingConstants.CENTER);
        hint.setForeground(new Color(180, 180, 180));

        JPanel overlay = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(new Color(0, 0, 0, 100));
                g.fillRect(0, 0, getWidth(), getHeight());
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                for (ConfettiParticle p : particles) p.draw(g2, drawScale[0]);
            }
        };
        overlay.setOpaque(false);
        overlay.add(msgLabel);
        overlay.add(commentLabel);
        overlay.add(hint);

        Runnable layoutLabels = () -> {
            int pw = overlay.getWidth(), ph = overlay.getHeight();
            if (pw == 0 || ph == 0) return;
            double sc = overlayScale(pw, ph);
            int msgFs = Math.min(Math.max(20, (int)(60 * sc)), ph / 5);
            int comFs = Math.max(10, (int)(20 * sc));
            int hntFs = Math.max(8,  (int)(16 * sc));
            msgLabel.setFont(new Font("Arial Black", Font.BOLD, msgFs));
            commentLabel.setFont(new Font("Arial", Font.BOLD, comFs));
            hint.setFont(new Font("Arial", Font.PLAIN, hntFs));
            int msgH  = msgFs + 16;
            int comH  = comFs + 10;
            int hntH  = hntFs + 10;
            int gap   = Math.max(6, (int)(14 * sc));
            int total = msgH + gap + comH + gap + hntH;
            int y0    = ph / 2 - total / 2;
            msgLabel.setBounds(0, y0, pw, msgH);
            commentLabel.setBounds(0, y0 + msgH + gap, pw, comH);
            hint.setBounds(0, y0 + msgH + gap + comH + gap, pw, hntH);
        };

        overlay.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                int pw = overlay.getWidth(), ph = overlay.getHeight();
                double sc = overlayScale(pw, ph);
                drawScale[0] = (float) sc;
                float sx = (float) pw / screenW[0];
                float sy = (float) ph / screenH[0];
                for (ConfettiParticle p : particles) {
                    p.x *= sx;
                    p.y *= sy;
                }
                screenW[0] = pw;
                screenH[0] = ph;
                layoutLabels.run();
                overlay.repaint();
            }
        });

        parent.setGlassPane(overlay);
        overlay.setVisible(true);

        SwingUtilities.invokeLater(layoutLabels::run);

        final long[] lastTick = { System.currentTimeMillis() };
        Timer t = new Timer(16, null);
        t.addActionListener(e -> {
            long now   = System.currentTimeMillis();
            long delta = now - lastTick[0];
            lastTick[0] = now;
            if (delta >= 5 && delta < 200) {
                for (ConfettiParticle p : particles) p.update(screenW[0], screenH[0]);
            }
            overlay.repaint();
        });
        t.start();

        overlay.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                t.stop(); relaunch(parent);
            }
        });
    }

    // Displays an animated robot-dance overlay with a lose message and a restart prompt.
    private static void showRobot(JFrame parent) {

        int dif = UI.your_score - UI.opp_score;

        String loseMsg = "YOU LOSE!";
        JLabel msgLabel = new JLabel(loseMsg, SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(loseMsg)) / 2;
                int ty = fm.getAscent();
                int o  = 4;
                g2.setColor(Color.BLACK);
                for (int dx = -o; dx <= o; dx += 2)
                    for (int dy = -o; dy <= o; dy += 2)
                        if (dx != 0 || dy != 0) g2.drawString(loseMsg, tx+dx, ty+dy);
                g2.setColor(new Color(120, 0, 0)); g2.drawString(loseMsg, tx-2, ty-2); g2.drawString(loseMsg, tx+2, ty+2);
                g2.setColor(new Color(220, 40, 40)); g2.drawString(loseMsg, tx, ty);
            }
        };
        JLabel commentLabel = new JLabel(
            dif < -50 ? "Go read some dictionary" : "You kinda suck at Scrabble",
            SwingConstants.CENTER);
        commentLabel.setForeground(new Color(255, 180, 180));
        JLabel hint = new JLabel("Click anywhere to restart", SwingConstants.CENTER);
        hint.setForeground(new Color(200, 150, 150));

        String[] robotFrames = { "/images/frame1.png", "/images/frame2.png", "/images/frame3.png" };
        ImageIcon[] robotIconsRaw = new ImageIcon[3];
        for (int i = 0; i < 3; i++) {
            java.net.URL imgURL = EndAnimations.class.getResource(robotFrames[i]);
            if (imgURL == null) { System.err.println("Missing: " + robotFrames[i]); return; }
            robotIconsRaw[i] = new ImageIcon(imgURL);
        }
        ImageIcon[] robotIcons = new ImageIcon[3];
        final int[] frame  = {0};
        final int[] bobY   = {0};
        final int[] bobDir = {1};

        JLabel robot = new JLabel();

        JPanel overlay = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(new Color(180, 0, 0, 120));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        overlay.setOpaque(false);
        overlay.add(msgLabel);
        overlay.add(commentLabel);
        overlay.add(hint);
        overlay.add(robot);

        Runnable layoutAll = () -> {
            int pw = overlay.getWidth(), ph = overlay.getHeight();
            if (pw == 0 || ph == 0) return;
            double sc = overlayScale(pw, ph);
            int msgFs = Math.min(Math.max(20, (int)(60 * sc)), ph / 5);
            int comFs = Math.max(10, (int)(20 * sc));
            int hntFs = Math.max(8,  (int)(16 * sc));
            msgLabel.setFont(new Font("Arial Black", Font.BOLD, msgFs));
            commentLabel.setFont(new Font("Arial", Font.BOLD, comFs));
            hint.setFont(new Font("Arial", Font.PLAIN, hntFs));
            int msgH  = msgFs + 16;
            int comH  = comFs + 10;
            int hntH  = hntFs + 10;
            int gap   = Math.max(6, (int)(14 * sc));
            int nW = Math.max(50, (int)(200 * pw / 1300.0));
            int nH = Math.max(73, (int)(292 * ph / 900.0));
            for (int i = 0; i < 3; i++) {
                robotIcons[i] = new ImageIcon(
                    robotIconsRaw[i].getImage().getScaledInstance(nW, nH, Image.SCALE_SMOOTH));
            }
            robot.setIcon(robotIcons[frame[0]]);
            robot.setSize(nW, nH);
            int robotGap = Math.max(10, (int)(20 * sc));
            int total = msgH + gap + comH + gap + hntH + robotGap + nH;
            int y0    = ph / 2 - total / 2;
            msgLabel.setBounds(0, y0, pw, msgH);
            commentLabel.setBounds(0, y0 + msgH + gap, pw, comH);
            hint.setBounds(0, y0 + msgH + gap + comH + gap, pw, hntH);
            int robotY = y0 + msgH + gap + comH + gap + hntH + robotGap;
            robot.setLocation(pw / 2 - nW / 2, robotY + bobY[0]);
        };

        overlay.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                layoutAll.run();
                overlay.repaint();
            }
        });

        parent.setGlassPane(overlay);
        overlay.setVisible(true);
        SwingUtilities.invokeLater(layoutAll::run);

        Timer t = new Timer(120, null);
        t.addActionListener(e -> {
            frame[0] = (frame[0] + 1) % 3;
            bobY[0] += bobDir[0] * 2;
            if (bobY[0] >= 6 || bobY[0] <= -6) bobDir[0] *= -1;
            robot.setIcon(robotIcons[frame[0]]);
            int pw = overlay.getWidth(), ph = overlay.getHeight();
            double sc = overlayScale(pw, ph);
            int msgFs = Math.min(Math.max(20, (int)(60 * sc)), ph / 5);
            int comFs = Math.max(10, (int)(20 * sc));
            int hntFs = Math.max(8,  (int)(16 * sc));
            int msgH  = msgFs + 16;
            int comH  = comFs + 10;
            int hntH  = hntFs + 10;
            int gap   = Math.max(6,  (int)(14 * sc));
            int nW    = Math.max(50, (int)(200 * pw / 1300.0));
            int nH    = Math.max(73, (int)(292 * ph / 900.0));
            int robotGap = Math.max(10, (int)(20 * sc));
            int total = msgH + gap + comH + gap + hntH + robotGap + nH;
            int robotY = ph / 2 - total / 2 + msgH + gap + comH + gap + hntH + robotGap;
            robot.setLocation(pw / 2 - nW / 2, robotY + bobY[0]);
        });
        t.start();

        overlay.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                t.stop(); relaunch(parent);
            }
        });
    }

    static class ConfettiParticle {
        float x, y, vx, vy, rotation, rotSpeed, wobble, wobbleSpeed, wobbleAmt;
        int   width, height, shape;
        Color color;

        // Initialises a confetti particle with random velocity, size, shape, and wobble.
        ConfettiParticle(int sx, int sy, Color color, Random rand) {
            x=sx; y=sy; this.color=color;
            vx = (rand.nextFloat()-0.5f)*4f;
            vy = 2f + rand.nextFloat()*4f;
            rotation    = rand.nextFloat()*360f;
            rotSpeed    = (rand.nextFloat()-0.5f)*8f;
            width       = 8  + rand.nextInt(10);
            height      = 4  + rand.nextInt(6);
            shape       = rand.nextInt(3);
            wobble      = rand.nextFloat()*360f;
            wobbleSpeed = 2f + rand.nextFloat()*3f;
            wobbleAmt   = 1f + rand.nextFloat()*2f;
        }

        // Advances the particle's position, rotation, and wobble for one frame.
        void update(int screenW, int screenH) {
            wobble += wobbleSpeed;
            x += vx + (float)Math.sin(Math.toRadians(wobble))*wobbleAmt;
            float speedScale = screenH / 900f;
            y += vy * speedScale;
            rotation += rotSpeed;
            if (y > screenH + 20) { y = -20; x = (float)(Math.random() * screenW); }
        }

        // Draws the particle at its current position using the default scale of 1.
        void draw(Graphics2D g2) { draw(g2, 1.0f); }

        // Draws the particle at its current position scaled by the given factor.
        void draw(Graphics2D g2, float sc) {
            int w = Math.max(2, (int)(width  * sc));
            int h = Math.max(1, (int)(height * sc));
            Graphics2D g = (Graphics2D) g2.create();
            g.translate(x, y);
            g.rotate(Math.toRadians(rotation));
            g.setColor(color);
            switch (shape) {
                case 0: g.fillRect(-w/2, -h/2, w, h); break;
                case 1: g.fillOval(-h/2, -h/2, h, h); break;
                case 2: g.fillPolygon(new int[]{0,-w/2,w/2}, new int[]{-h/2,h/2,h/2}, 3); break;
            }
            g.dispose();
        }
    }
}