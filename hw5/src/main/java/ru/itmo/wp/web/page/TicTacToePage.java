
package ru.itmo.wp.web.page;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public class TicTacToePage {
    private static final String STATE_KEY = "ticTacToeState";

    private static final String RUNNING = "RUNNING";
    private static final String WON_X = "WON_X";
    private static final String WON_O = "WON_O";
    private static final String DRAW = "DRAW";

    private static final String X = "X";
    private static final String O = "O";

    private static final String CELL_PREFIX = "cell_";
    private static final String VIEW_STATE = "state";

    private static final int SIZE = 3;

    public static class State {
        private final String[][] cells = new String[SIZE][SIZE];
        private boolean crossesMove = true;
        private String phase = RUNNING;

        public int getSize() {
            return SIZE;
        }

        public String[][] getCells() {
            return cells;
        }

        public boolean isCrossesMove() {
            return crossesMove;
        }

        public String getPhase() {
            return phase;
        }
    }

    private void action(HttpServletRequest request,
                        Map<String, Object> view) {
        State state = getState(request);
        view.put(VIEW_STATE, state);
    }

    private void onMove(HttpServletRequest request,
                        Map<String, Object> view) {
        State state = getState(request);

        if (RUNNING.equals(state.phase)) {
            for (int row = 0; row < SIZE; row++) {
                for (int col = 0; col < SIZE; col++) {
                    String cell = CELL_PREFIX + row + col;

                    if (request.getParameter(cell) != null
                            && state.cells[row][col] == null) {

                        state.cells[row][col] =
                                state.crossesMove ? X : O;

                        checkGame(state);

                        if (RUNNING.equals(state.phase)) {
                            state.crossesMove = !state.crossesMove;
                        }

                        view.put(VIEW_STATE, state);
                        return;
                    }
                }
            }
        }

        view.put(VIEW_STATE, state);
    }

    private void newGame(HttpServletRequest request,
                         Map<String, Object> view) {
        State state = new State();
        request.getSession().setAttribute(STATE_KEY, state);
        view.put(VIEW_STATE, state);
    }

    private State getState(HttpServletRequest request) {
        State state = (State) request.getSession()
                .getAttribute(STATE_KEY);

        if (state == null) {
            state = new State();
            request.getSession().setAttribute(STATE_KEY, state);
        }

        return state;
    }

    private void checkGame(State state) {
        String[][] cells = state.cells;

        for (int i = 0; i < SIZE; i++) {
            if (same(cells[i][0], cells[i][1], cells[i][2])) {
                setWinner(state, cells[i][0]);
                return;
            }

            if (same(cells[0][i], cells[1][i], cells[2][i])) {
                setWinner(state, cells[0][i]);
                return;
            }
        }

        if (same(cells[0][0], cells[1][1], cells[2][2])) {
            setWinner(state, cells[0][0]);
            return;
        }

        if (same(cells[0][2], cells[1][1], cells[2][0])) {
            setWinner(state, cells[0][2]);
            return;
        }

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (cells[row][col] == null) {
                    return;
                }
            }
        }

        state.phase = DRAW;
    }

    private boolean same(String a, String b, String c) {
        return a != null && a.equals(b) && b.equals(c);
    }

    private void setWinner(State state, String winner) {
        if (X.equals(winner)) {
            state.phase = WON_X;
        } else {
            state.phase = WON_O;
        }
    }
}
