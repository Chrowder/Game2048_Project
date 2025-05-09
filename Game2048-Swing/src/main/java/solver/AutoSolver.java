package solver;

import java.util.*;

/**
 * Iterative‑Deepening Expectimax with Transposition Table
 * 方向编号：0 左, 1 右, 2 上, 3 下
 */
public class AutoSolver {


    private static final int   BASE_DEPTH   = 4;
    private static final long  TIME_BUDGET  = 40_000;
    private static final int   SIZE         = 4;
    private static final Random RAND        = new Random();


    public int nextMove(int[][] board) {
        long deadline = System.nanoTime() + TIME_BUDGET * 1000;
        int bestDir = 0;
        double bestVal = Double.NEGATIVE_INFINITY;


        for (int depth = 2; depth <= 12; depth++) {
            TT.clear();
            int[] order = {0, 3, 2, 1};
            int localBest = -1;
            double localVal = Double.NEGATIVE_INFINITY;

            for (int dir : order) {
                if (System.nanoTime() > deadline) break;
                int[][] next = cloneBoard(board);
                if (!move(next, dir)) continue;
                double v = expectimax(next, depth - 1, false, deadline);
                if (v > localVal || (v == localVal && RAND.nextBoolean())) {
                    localVal = v;
                    localBest = dir;
                }
            }
            if (System.nanoTime() > deadline) break;
            if (localBest != -1) {
                bestDir = localBest;
                bestVal = localVal;
            }
        }
        return bestDir;
    }


    private static final Map<Long, Double> TT = new HashMap<>();

    private double expectimax(int[][] board, int depth, boolean isPlayer, long deadline) {
        if (depth == 0 || !canMove(board) || System.nanoTime() > deadline)
            return evaluate(board);

        long hash = zobrist(board, isPlayer);
        Double cached = TT.get(hash);
        if (cached != null && depth <= 6) return cached;

        double result;
        if (isPlayer) {
            double best = Double.NEGATIVE_INFINITY;
            for (int dir = 0; dir < 4; dir++) {
                int[][] next = cloneBoard(board);
                if (!move(next, dir)) continue;
                best = Math.max(best, expectimax(next, depth - 1, false, deadline));
            }
            result = best;
        } else {
            List<int[]> empties = emptyTiles(board);
            double sum = 0;
            for (int[] pos : empties) {
                int r = pos[0], c = pos[1];
                int[][] b2 = cloneBoard(board);
                b2[r][c] = 2;
                sum += 0.9 * expectimax(b2, depth - 1, true, deadline);
                b2[r][c] = 4;
                sum += 0.1 * expectimax(b2, depth - 1, true, deadline);
            }
            result = sum / empties.size();
        }
        if (depth >= 4) TT.put(hash, result);
        return result;
    }


    private double evaluate(int[][] b) {
        int empty = 0;
        double mono = 0, smooth = 0, cluster = 0;
        int max = 0;


        double[][] logs = new double[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++) {
                int v = b[r][c];
                if (v == 0) { empty++; continue; }
                logs[r][c] = Math.log(v) / Math.log(2);
                max = Math.max(max, v);
            }


        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++) {
                if (b[r][c] == 0) continue;
                if (c + 1 < SIZE && b[r][c + 1] != 0)
                    smooth -= Math.abs(logs[r][c] - logs[r][c + 1]);
                if (r + 1 < SIZE && b[r + 1][c] != 0)
                    smooth -= Math.abs(logs[r][c] - logs[r + 1][c]);


                for (int dr = -1; dr <= 1; dr++)
                    for (int dc = -1; dc <= 1; dc++) {
                        int nr = r + dr, nc = c + dc;
                        if (nr < 0 || nc < 0 || nr >= SIZE || nc >= SIZE || b[nr][nc] == 0) continue;
                        cluster += Math.abs(logs[r][c] - logs[nr][nc]);
                    }
            }


        mono += monotonicity(b, true) + monotonicity(b, false);


        double maxCorner = (b[0][0]==max||b[0][SIZE-1]==max||b[SIZE-1][0]==max||b[SIZE-1][SIZE-1]==max)?1:0;


        return  3.5 * empty +
                1.5 * smooth +
                1.0 * mono   +
                -0.2 * cluster +
                120.0 * maxCorner;
    }
    private double monotonicity(int[][] b, boolean row) {
        double total = 0;
        for (int i = 0; i < SIZE; i++) {
            double inc = 0, dec = 0, prev = 0;
            for (int j = 0; j < SIZE; j++) {
                int v = row ? b[i][j] : b[j][i];
                double cur = v == 0 ? 0 : Math.log(v) / Math.log(2);
                if (cur > prev) inc += cur - prev; else dec += prev - cur;
                prev = cur;
            }
            total += Math.min(inc, dec);
        }
        return -total;
    }


    private static final long[][][] Z = new long[SIZE][SIZE][16];
    static {
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                for (int k = 0; k < 16; k++)
                    Z[r][c][k] = RAND.nextLong();
    }
    private long zobrist(int[][] b, boolean playerTurn) {
        long h = playerTurn ? 0L : 1L;
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++) {
                int v = b[r][c];
                if (v == 0) continue;
                int log = Integer.numberOfTrailingZeros(v); // 2^n → n
                h ^= Z[r][c][log & 15];
            }
        return h;
    }


    private int[][] cloneBoard(int[][] src){
        int[][] dst = new int[SIZE][SIZE];
        for(int i=0;i<SIZE;i++) System.arraycopy(src[i],0,dst[i],0,SIZE);
        return dst;
    }
    private boolean canMove(int[][] b){
        for(int d=0;d<4;d++){int[][] c=cloneBoard(b);if(move(c,d))return true;}return false;
    }
    private List<int[]> emptyTiles(int[][] b){
        List<int[]> list=new ArrayList<>();
        for(int r=0;r<SIZE;r++)for(int c=0;c<SIZE;c++)if(b[r][c]==0)list.add(new int[]{r,c});
        return list;
    }
    private boolean move(int[][] board,int dir){
        switch(dir){
            case 0: return moveLeft(board);
            case 1: return moveRight(board);
            case 2: return moveUp(board);
            case 3: return moveDown(board);
        }
        return false;
    }


    private boolean moveLeft(int[][] board){
        boolean moved = false;
        for (int r = 0; r < SIZE; ++r) {
            int[] newRow = new int[SIZE];
            int pos = 0, last = 0;
            for (int c = 0; c < SIZE; ++c) {
                int val = board[r][c];
                if (val == 0) continue;
                if (val == last) {
                    newRow[pos - 1] <<= 1;
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
    private void rotateLeft(int[][] b){int[][] n=new int[SIZE][SIZE];
        for(int r=0;r<SIZE;r++)for(int c=0;c<SIZE;c++)n[SIZE-c-1][r]=b[r][c];for(int r=0;r<SIZE;r++)System.arraycopy(n[r],0,b[r],0,SIZE);}
    private void rotateRight(int[][] b){rotateLeft(b);rotateLeft(b);rotateLeft(b);}
    private void rotate180(int[][] b){rotateLeft(b);rotateLeft(b);}
    private boolean moveRight(int[][] b){rotate180(b);boolean m=moveLeft(b);rotate180(b);return m;}
    private boolean moveUp(int[][] b){rotateLeft(b);boolean m=moveLeft(b);rotateRight(b);return m;}
    private boolean moveDown(int[][] b){rotateRight(b);boolean m=moveLeft(b);rotateLeft(b);return m;}
}
