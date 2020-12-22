public class StartToEnd {
    private int start;
    private int end;

    //存储分割文件的起始值
    public StartToEnd(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    @Override
    public String toString() {
        return "StartToEnd{" +
                "start=" + start +
                ", end=" + end +
                '}';
    }
}

