import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.Callable;


public class ReadTask implements Callable {


    //定义读取的起始点
    private int start;
    //定义读取的结束点
    private int end;
    //将读取到的字节输出到raf中
    private RandomAccessFile raf;
    private int superLength;

    public ReadTask(int start, int end, RandomAccessFile raf, int length) {
        this.start = start;
        this.end = end;
        this.raf = raf;
        this.superLength = length;
    }

    //将relations中的id对转化为int值存入到关系数组中
    @Override
    public int[] call() throws Exception {
        int[] list = new int[superLength];
        try {
            int contentLen = end - start;
            MappedByteBuffer out = raf.getChannel().map(FileChannel.MapMode.READ_ONLY,start,contentLen);
            int count = 0;
            int index = 0;
            int res = 0;
            while(index<contentLen) {
                byte b = out.get(index);
                if (b == 32) {
                    if ((count & 1) == 0) {
                        index += 18;
                    } else {
                        index += 19;
                    }
                    list[count] = res;
                    count++;
                    res = 0;
                } else {
                    res = res*10 + b - 48;
                    index++;
                }
            }
            raf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }


}
