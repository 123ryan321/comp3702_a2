package solver;

public class Timer {
    // allows for easy reporting of elapsed time
    long oldTime;

    public Timer() {
        oldTime = System.currentTimeMillis();
    }

    public long elapsed() {
        return System.currentTimeMillis() - oldTime;
    }

    public void reset() {
        oldTime = System.currentTimeMillis();
    }

    public String toString() {
        // now resets the timer...
        String ret = elapsed() + " ms elapsed";
        reset();
        return ret;
    }


}
