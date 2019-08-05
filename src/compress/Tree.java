package compress;

public class Tree implements Comparable<Tree> {

    Tree left;
    Tree right;
    int frequency;
    byte data;

    @Override
    public int compareTo(Tree T) {
        if (this.frequency < T.frequency) {
            return -1;
        }
        if (this.frequency > T.frequency) {
            return 1;
        }
        return 0;
    }
}
