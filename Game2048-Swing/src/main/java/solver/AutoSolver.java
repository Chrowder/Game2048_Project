package solver;

import java.util.*;

/**
 * Expectimax AutoSolver for 2048
 *
 * 方向编号与 Game2048 一致：
 *   0 = 左, 1 = 右, 2 = 上, 3 = 下
 */
public class AutoSolver {

    private static final int SIZE = 4;

    /** 公开接口：给定棋盘返回最佳方向 */
    public int nextMove(int[][] board) {
        int bestDir = 0;
        double bestScore = Double.NEGATIVE_INFINITY;

        // 尝试 4 个方向
        for (int dir = 0; dir < 4; dir++) {
            int[][] next = cloneBoard(board);
            if (!move(next, dir)) continue;               // 无效移动
            double v = expectimax(next, 1, false);
            if (v > bestScore) {
                bestScore = v;
                bestDir = dir;
            }
        }
        return bestDir;
    }

    /* ------------------ Expectimax 搜索 ------------------ */

    private static final int MAX_DEPTH = 4;               // 基础深度
    private double expectimax(int[][] state, int depth, boolean isPlayerTurn) {
        if (depth == 0 || !canMove(state)) {
            return evaluate(state);
        }

        if (isPlayerTurn) {                               // 玩家层：max
            double best = Double.NEGATIVE_INFINITY;
            for (int dir = 0; dir < 4; dir++) {
                int[][] next = cloneBoard(state);
                if (!move(next, dir)) continue;
                best = Math.max(best, expectimax(next, depth - 1, false));
            }
            return best;
        } else {                                          // 环境层：期望
            List<int[]> empties = emptyTiles(state);
            double sum = 0;
            for (int[] pos : empties) {
                int r = pos[0], c = pos[1];
                // 90% 放 2
                int[][] s2 = cloneBoard(state);
                s2[r][c] = 2;
                sum += 0.9 * expectimax(s2, depth - 1, true);
                // 10% 放 4
                int[][] s4 = cloneBoard(state);
                s4[r][c] = 4;
                sum += 0.1 * expectimax(s4, depth - 1, true);
            }
            return sum / empties.size();
        }
    }

    /* -------------------- 评估函数 -------------------- */
    private double evaluate(int[][] b) {
        int empty = 0;
        double smooth = 0;
        double monoRow = 0, monoCol = 0;
        int max = 0;

        // 平滑度 & 空格 & 最大值
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++) {
                int v = b[r][c];
                if (v == 0) { empty++; continue; }
                if (v > max) max = v;

                // 相邻差距
                if (c + 1 < SIZE && b[r][c + 1] != 0)
                    smooth -= Math.abs(Math.log(v) / Math.log(2) - Math.log(b[r][c + 1]) / Math.log(2));
                if (r + 1 < SIZE && b[r + 1][c] != 0)
                    smooth -= Math.abs(Math.log(v) / Math.log(2) - Math.log(b[r + 1][c]) / Math.log(2));
            }

        // 行单调
        for (int r = 0; r < SIZE; r++) {
            int current = 0, next = 1;
            while (next < SIZE) {
                while (next < SIZE && b[r][next] == 0) next++;
                if (next >= SIZE) next--;
                int currVal = b[r][current] != 0 ? (int)Math.log(b[r][current]) : 0;
                int nextVal = b[r][next]  != 0 ? (int)Math.log(b[r][next])  : 0;
                if (currVal > nextVal) monoRow += nextVal - currVal;
                current = next;
                next++;
            }
        }
        // 列单调
        for (int c = 0; c < SIZE; c++) {
            int current = 0, next = 1;
            while (next < SIZE) {
                while (next < SIZE && b[next][c] == 0) next++;
                if (next >= SIZE) next--;
                int currVal = b[current][c] != 0 ? (int)Math.log(b[current][c]) : 0;
                int nextVal = b[next][c]   != 0 ? (int)Math.log(b[next][c])   : 0;
                if (currVal > nextVal) monoCol += nextVal - currVal;
                current = next;
                next++;
            }
        }

        // 最大块在角落奖励
        double maxCorner = (b[0][0]==max || b[0][SIZE-1]==max || b[SIZE-1][0]==max || b[SIZE-1][SIZE-1]==max) ? 1 : 0;

        // 权重可调
        return  2.7 * empty +
                1.0 * smooth +
                1.0 * monoRow +
                1.0 * monoCol +
                100.0 * maxCorner;
    }

    /* ------------------ 工具函数 ------------------ */

    private boolean canMove(int[][] s) {
        for (int dir = 0; dir < 4; dir++) {
            int[][] copy = cloneBoard(s);
            if (move(copy, dir)) return true;
        }
        return false;
    }

    private List<int[]> emptyTiles(int[][] s) {
        List<int[]> list = new ArrayList<>();
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                if (s[r][c] == 0) list.add(new int[]{r, c});
        return list;
    }

    private int[][] cloneBoard(int[][] src) {
        int[][] dst = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) System.arraycopy(src[i], 0, dst[i], 0, SIZE);
        return dst;
    }

    /* ---------- 与 Game2048 完全一致的移动实现 ---------- */

    private boolean move(int[][] board, int dir) {
        switch (dir) {
            case 0:  return moveLeft(board);
            case 1:  return moveRight(board);
            case 2:  return moveUp(board);
            case 3:  return moveDown(board);
            default: return false;
        }
    }

    private boolean moveLeft(int[][] board) {
        boolean moved = false;
        for (int r = 0; r < SIZE; ++r) {
            int[] newRow = new int[SIZE];
            int pos = 0, last = 0;
            for (int c = 0; c < SIZE; ++c) {
                int val = board[r][c];
                if (val == 0) continue;
                if (val == last) {
                    newRow[pos - 1] *= 2;
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

    private void rotateLeft(int[][] b) {
        int[][] n = new int[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                n[SIZE - c - 1][r] = b[r][c];
        for (int r = 0; r < SIZE; r++)
            System.arraycopy(n[r], 0, b[r], 0, SIZE);
    }
    private void rotateRight(int[][] b) { rotateLeft(b); rotateLeft(b); rotateLeft(b); }
    private void rotate180(int[][] b)   { rotateLeft(b); rotateLeft(b); }

    private boolean moveRight(int[][] b){ rotate180(b); boolean m=moveLeft(b); rotate180(b); return m; }
    private boolean moveUp(int[][] b)   { rotateLeft(b); boolean m=moveLeft(b); rotateRight(b);return m; }
    private boolean moveDown(int[][] b) { rotateRight(b);boolean m=moveLeft(b); rotateLeft(b); return m; }
}
