package stringgame;

public class Game
{
    static final boolean printGame = false;

    Player x;
    Player o;

    int state[][];      ///< 0 = free,1 = x, 2 = o

    GameStats stats;

    class WinState
    {
        boolean done;
        int winner;
    }
    
    class GameStats
    {
        public GameStats() {
            draw = xWin = oWin = 0;
        }
        int draw;
        int xWin;
        int oWin;
    }

    public Game(Player x, Player o) {
        state = new int[3][3];
        this.x = x;
        this.o = o;
        stats = new GameStats();
    }

    public void play() {
        init();

        boolean done = false;
        int turn = 0;
        int winner = -1;
        while (!done) {
            if (printGame)
                print();

            Player p = null;
            if (turn == 0)
                p = x;
            else if (turn == 1)
                p = o;
            String action = p.sense(constructState(turn), 0.0);
            applyAction(action, turn);

            WinState ws = checkDone();
            if (ws.done)
                done = true;
            winner = ws.winner;

            turn = 1 - turn;
        }

        if (printGame)
            print();

        if (winner == 0) {
            x.terminate(constructState(0), 0.0, true);
            o.terminate(constructState(1), 0.0, false);
            if (printGame)
                System.out.println("Draw");
            stats.draw++;
        } else if (winner == 1) {
            x.terminate(constructState(0), 1.0, true);
            o.terminate(constructState(1), -1.0, false);
            if (printGame)
                System.out.println("X wins.");
            stats.xWin++;
        } else if (winner == 2) {
            x.terminate(constructState(0), -1.0, true);
            o.terminate(constructState(1), 1.0, false);
            if (printGame)
                System.out.println("O wins.");
            stats.oWin++;
        }
    }

    protected void init() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                state[i][j] = 0;
            }
        }
    }

    String constructState(int turn) {
        String ret = "";
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                switch (state[i][j]) {
                case 1: // X
                    if (turn == 0) { // switch around, as player sees always X
                        // as himself
                        ret += "x";
                    } else {
                        ret += "o";
                    }
                    break;
                case 2: // O
                    if (turn == 0) {
                        ret += "o";
                    } else {
                        ret += "x";
                    }
                    break;
                case 0:
                default:
                    ret += "_";
                    break;
                }
            }
        }
        return ret;
    }

    // / should be like 1,2 for "place mark at 1st row, 2nd column"
    void applyAction(String action, int turn) {
        char row = action.charAt(0);
        char col = action.charAt(2);
        int i = 0, j = 0;
        switch (row) {
        case '1':
            i = 0;
            break;
        case '2':
            i = 1;
            break;
        case '3':
            i = 2;
            break;
        default:
            return;
        }
        switch (col) {
        case '1':
            j = 0;
            break;
        case '2':
            j = 1;
            break;
        case '3':
            j = 2;
            break;
        default:
            return;
        }
        if (state[i][j] != 0) {
            return;
        }
        state[i][j] = turn + 1;
    }

    WinState checkDone() {
        WinState ws = new WinState();
        ws.done = false;
        ws.winner = 0;

        boolean xwin = false;
        boolean owin = false;
        // row
        for (int i = 0; i < 3; i++) {
            int cx = 0;
            int co = 0;
            for (int j = 0; j < 3; j++) {
                if (state[i][j] == 1)
                    cx++;
                else if (state[i][j] == 2)
                    co++;
            }
            if (cx == 3)
                xwin = true;
            if (co == 3)
                owin = true;
        }
        // col
        for (int j = 0; j < 3; j++) {
            int cx = 0;
            int co = 0;
            for (int i = 0; i < 3; i++) {
                if (state[i][j] == 1)
                    cx++;
                else if (state[i][j] == 2)
                    co++;
            }
            if (cx == 3)
                xwin = true;
            if (co == 3)
                owin = true;
        }

        // diag
        int cx = 0;
        int co = 0;
        for (int ii = 0; ii < 3; ii++) {
            if (state[ii][ii] == 1)
                cx++;
            else if (state[ii][ii] == 2)
                co++;
        }
        if (cx == 3)
            xwin = true;
        if (co == 3)
            owin = true;

        cx = 0;
        co = 0;
        for (int ii = 0; ii < 3; ii++) {
            if (state[ii][2 - ii] == 1)
                cx++;
            else if (state[ii][2 - ii] == 2)
                co++;
        }
        if (cx == 3)
            xwin = true;
        if (co == 3)
            owin = true;

        if (xwin && !owin) {
            ws.winner = 1;
        } else if (owin && !xwin) {
            ws.winner = 2;
        }

        int used = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (state[i][j] != 0)
                    used++;
            }
        }

        ws.done = (xwin || owin) || (used == 9);
        return ws;
    }

    void print() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                switch (state[i][j]) {
                case 0:
                    System.out.print("_ ");
                    break;
                case 1:
                    System.out.print("X ");
                    break;
                case 2:
                    System.out.print("O ");
                    break;
                }
            }
            System.out.print("\n");
        }
        System.out.print("\n");
    }
}
