import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class TxtFileUtils {

    /*读取文件的MD5值，为了节省存储空间，存储到byte数组中，每一行的id都是顺序排列的，针对此特性，根据id所占字节数
    即可偏移到指定的md5值的首部位置，顺序取16位存储到数组中即可
    */
    public static void getAcctontsMmb3(String path,byte[] list){
        RandomAccessFile memoryMappedFile = null;
        MappedByteBuffer out = null;
        int size = 0;
        try {
            memoryMappedFile = new RandomAccessFile(path, "r");
            size = (int) memoryMappedFile.length();
            out = memoryMappedFile.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, size);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int k =0;
        //个位数的位数为1，有10个，加上空格与md5值长度，每次偏移起始值为2+20*i；
        for(int i = 0;i<10;i++){
            int s = 2+20*i;
            for(int start = s;start<s+16;start++){
                list[k++] = out.get(start);
            }
        }

        //两位数的位数为2，有90个，加上空格与md5值长度，每次偏移起始值为203+21*i；
        for(int i = 0;i<90;i++){
            //203为上一次的偏移终止数值203=2+10*20+1
            int s = 203+21*i;
            for(int start = s;start<s+16;start++){
                list[k++] = out.get(start);
            }
        }

        //三位数的位数为3，有900个，加上空格与md5值长度，每次偏移起始值为2094+22*i；
        for(int i = 0;i<900;i++){
            //2094为上一次的偏移终止数值2094=203+21*90+1
            int s = 2094+22*i;
            for(int start = s;start<s+16;start++){
                list[k++] = out.get(start);
            }
        }

        //同上注释
        for(int i = 0;i<9000;i++){
            int s = 21895+23*i;
            for(int start = s;start<s+16;start++){
                list[k++] = out.get(start);
            }
        }

        //同上注释
        for(int i = 0;i<90000;i++){
            int s = 228896+24*i;
            for(int start = s;start<s+16;start++){
                list[k++] = out.get(start);
            }
        }

        //同上注释
        for(int i = 0;i<900000;i++){
            int s = 2388897+25*i;
            for(int start = s;start<s+16;start++){
                list[k++] = out.get(start);
            }
        }

        //同上注释
        for(int i = 0;i<4000001;i++){
            int s = 24888898+26*i;
            for(int start = s;start<s+16;start++){
                list[k++] = out.get(start);
            }
        }

        try {
            memoryMappedFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
