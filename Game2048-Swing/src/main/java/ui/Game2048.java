package ui;

import net.NetUtil;
import net.dto.LeaderboardEntry;
import solver.AutoSolver;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Game2048 extends JFrame implements KeyListener {
    private static final int SIZE = 4;
    private static final int TILE_SIZE = 100;
    private static final int GAP = 10;
    private static final int FONT_SIZE = 36;
    private static final int SCORE_HEIGHT = 60;
    private static final int HINT_HEIGHT  = 30;

    private int[][] board = new int[SIZE][SIZE];
    private int score = 0;
    private final Random rand = new Random();
    private final BoardPanel panel = new BoardPanel();
    private volatile boolean busy = false;
    private volatile boolean aiRunning = false;
    private Thread aiThread;
    private String playerName = "";



    public Game2048() {
        setTitle("2048");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        add(panel);
        addKeyListener(this);

        int boardPixels = SIZE * TILE_SIZE + (SIZE + 1) * GAP;
        setSize(boardPixels, boardPixels + SCORE_HEIGHT + HINT_HEIGHT + 2*GAP);
        setLocationRelativeTo(null);

        resetGame();
    }

    // ---------- game logic ----------
    private void resetGame() {
        for (int i = 0; i < SIZE; ++i) Arrays.fill(board[i], 0);
        score = 0;
        addRandomTile();
        addRandomTile();
        panel.repaint();
    }

    private void addRandomTile() {
        List<Point> empty = new ArrayList<>();
        for (int r = 0; r < SIZE; ++r)
            for (int c = 0; c < SIZE; ++c)
                if (board[r][c] == 0) empty.add(new Point(r, c));
        if (empty.isEmpty()) return;
        Point p = empty.get(rand.nextInt(empty.size()));
        board[p.x][p.y] = rand.nextDouble() < 0.9 ? 2 : 4;
    }

    // ---------- movement ----------
    private boolean moveLeft() {
        boolean moved = false;
        for (int r = 0; r < SIZE; ++r) {
            int[] newRow = new int[SIZE];
            int pos = 0, last = 0;
            for (int c = 0; c < SIZE; ++c) {
                int val = board[r][c];
                if (val == 0) continue;
                if (val == last) {
                    newRow[pos - 1] *= 2;
                    score += newRow[pos - 1];
                    last = 0;
                    moved = true;
                } else {
                    newRow[pos] = val;
                    moved |= (c != pos);
                    last = val;
                    pos++;
                }
            }
            board[r] = newRow;
        }
        return moved;
    }
    private void rotateLeft()  { int[][] n = new int[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++) for (int c = 0; c < SIZE; c++) n[SIZE - c - 1][r] = board[r][c]; board = n; }
    private void rotateRight() { rotateLeft(); rotateLeft(); rotateLeft(); }
    private void rotate180()   { rotateLeft(); rotateLeft(); }
    private boolean moveRight(){ rotate180(); boolean m=moveLeft(); rotate180(); return m; }
    private boolean moveUp()   { rotateLeft(); boolean m=moveLeft(); rotateRight();return m; }
    private boolean moveDown() { rotateRight();boolean m=moveLeft(); rotateLeft(); return m; }

    private boolean canMove() {
        for (int r = 0; r < SIZE; ++r)
            for (int c = 0; c < SIZE; ++c) {
                if (board[r][c] == 0) return true;
                if (r < SIZE - 1 && board[r][c] == board[r + 1][c]) return true;
                if (c < SIZE - 1 && board[r][c] == board[r][c + 1]) return true;
            }
        return false;
    }

    // ---------- AI ----------
    private void startAI() {
        aiRunning = true;
        aiThread = new Thread(() -> {
            AutoSolver solver = new AutoSolver();
            while (aiRunning && canMove()) {
                int dir = solver.nextMove(board);
                if (!busy) doMoveByDir(dir);
                try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            }
        }, "AIThread");
        aiThread.start();
    }
    private void stopAI() { aiRunning = false; }

    /* ---------------- keyboard ---------------- */
    @Override public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        switch (code) {
            case KeyEvent.VK_R:
                stopAI();
                resetGame();
                break;
            case KeyEvent.VK_A:
                if (aiRunning) stopAI(); else startAI();
                break;
            default:
                doMoveByDir(mapKey(code));
        }
    }
    private int mapKey(int code) {
        switch (code) {
            case KeyEvent.VK_LEFT:  return 0;
            case KeyEvent.VK_RIGHT: return 1;
            case KeyEvent.VK_UP:    return 2;
            case KeyEvent.VK_DOWN:  return 3;
            default:                return -1;
        }
    }
    private void doMoveByDir(int dir) {
        if (dir == -1 || busy) return;
        busy = true;
        new Thread(() -> {
            boolean moved = false;
            switch (dir) {
                case 0: moved = moveLeft();  break;
                case 1: moved = moveRight(); break;
                case 2: moved = moveUp();    break;
                case 3: moved = moveDown();  break;
            }
            if (moved) addRandomTile();
            SwingUtilities.invokeLater(() -> {
                panel.repaint();
                if (!canMove()) { stopAI(); showGameOver(); }
                busy = false;
            });
        }, "MoveThread").start();
    }

    /* ---------------- Game Over + enter the name ---------------- */
    private void showGameOver() {
        if (playerName.isEmpty()) playerName = System.getProperty("user.name");
        playerName = JOptionPane.showInputDialog(
                this, "Enter your name:", playerName);
        if (playerName == null || playerName.trim().isEmpty()) return;

        int opt = JOptionPane.showConfirmDialog(
                this,
                "Game over! Score: " + score +
                        "\nUpload & show leaderboard as \"" + playerName + "\" ?",
                "2048",
                JOptionPane.YES_NO_OPTION
        );
        if (opt == JOptionPane.YES_OPTION)
            new LeaderboardWorker(playerName.trim(), score).execute();
    }

    /* ---------------- SwingWorker ---------------- */
    private class LeaderboardWorker extends SwingWorker<List<LeaderboardEntry>,Void>{
        private final String name; private final int sc;
        LeaderboardWorker(String n, int s){ name=n; sc=s; }
        @Override protected List<LeaderboardEntry> doInBackground() throws Exception {
            NetUtil.postScore(name, sc);
            return NetUtil.fetchTopScores();
        }
        @Override protected void done() {
            try {
                List<LeaderboardEntry> list = get();
                StringBuilder sb = new StringBuilder("===== Leaderboard =====\n");
                int rank = 1;
                for (LeaderboardEntry e : list)
                    sb.append(rank++).append(". ").append(e.getName())
                            .append(" — ").append(e.getScore()).append("\n");


                Object[] options = {"Open Web Leaderboard", "OK"};
                int choice = JOptionPane.showOptionDialog(
                        Game2048.this, sb.toString(), "Leaderboard",
                        JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE,
                        null, options, options[1]);

                if (choice == 0) {
                    try {
                        String url = NetUtil.getBase() + "/scores";
                        Desktop.getDesktop().browse(new URI(url));
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(Game2048.this,
                                "Cannot open browser: " + ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
                resetGame();
            } catch (InterruptedException | ExecutionException ex) {
                JOptionPane.showMessageDialog(Game2048.this,
                        "Network Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    // ---------- drawing ----------
    private class BoardPanel extends JPanel {
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int boardPixels = SIZE * TILE_SIZE + (SIZE + 1) * GAP;
            int offsetY = SCORE_HEIGHT + HINT_HEIGHT;

            /* background */
            g2.setColor(new Color(0xbbada0));
            g2.fillRoundRect(0, offsetY, boardPixels, boardPixels, 15, 15);

            /* scores */
            g2.setColor(new Color(0x776e65));
            g2.setFont(getFont().deriveFont(Font.BOLD, 24f));
            String scoreStr = "Score: " + score;
            FontMetrics fm = g2.getFontMetrics();
            int sx = (boardPixels - fm.stringWidth(scoreStr)) / 2;
            int sy = (SCORE_HEIGHT + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(scoreStr, sx, sy);

            /* key instructions */
            g2.setFont(getFont().deriveFont(Font.PLAIN, 14f));
            String hint = "← ↑ → ↓ : Move    A : Auto/Manual    R : Restart";
            FontMetrics hfm = g2.getFontMetrics();
            int hx = (boardPixels - hfm.stringWidth(hint)) / 2;
            int hy = SCORE_HEIGHT + hfm.getAscent() + 4;
            g2.drawString(hint, hx, hy);

            /* draw */
            for (int r = 0; r < SIZE; r++)
                for (int c = 0; c < SIZE; c++) {
                    int x = GAP + c * (TILE_SIZE + GAP);
                    int y = offsetY + GAP + r * (TILE_SIZE + GAP);
                    drawTile(g2, board[r][c], x, y);
                }
        }

        private void drawTile(Graphics2D g, int v, int x, int y) {
            g.setColor(getBG(v));
            g.fillRoundRect(x, y, TILE_SIZE, TILE_SIZE, 15, 15);
            if (v != 0) {
                g.setColor(v < 8 ? new Color(0x776e65) : Color.WHITE);
                g.setFont(getFont().deriveFont(Font.BOLD, (float) FONT_SIZE));
                String s = String.valueOf(v);
                FontMetrics fm = g.getFontMetrics();
                int tx = x + (TILE_SIZE - fm.stringWidth(s)) / 2;
                int ty = y + (TILE_SIZE + fm.getAscent() - fm.getDescent()) / 2;
                g.drawString(s, tx, ty);
            }
        }
        private Color getBG(int v) {
            switch (v) {
                case 0:    return new Color(0xcdc1b4);
                case 2:    return new Color(0xeee4da);
                case 4:    return new Color(0xede0c8);
                case 8:    return new Color(0xf2b179);
                case 16:   return new Color(0xf59563);
                case 32:   return new Color(0xf67c5f);
                case 64:   return new Color(0xf65e3b);
                case 128:  return new Color(0xedcf72);
                case 256:  return new Color(0xedcc61);
                case 512:  return new Color(0xedc850);
                case 1024: return new Color(0xedc53f);
                case 2048: return new Color(0xedc22e);
                default:   return new Color(0x3c3a32);
            }
        }
        @Override public Dimension getPreferredSize() {
            int boardPixels = SIZE * TILE_SIZE + (SIZE + 1) * GAP;
            return new Dimension(boardPixels, boardPixels + SCORE_HEIGHT + HINT_HEIGHT + GAP);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Game2048().setVisible(true));
    }
}
