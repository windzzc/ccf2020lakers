import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Main {
    final static int idNum = 5000001;//accounts行数
    final static int taskNum = 20;//执行任务数
    final static int threadNum = 4;//线程池线程数量
    final static int relaNum = 3667136;//relations行数

    //打成jar包时使用的相对路径
//    final static String accPath = "."+File.separator+"accounts.txt";
//    final static String relaPath = "."+File.separator+"relations.txt";
   // final static String resPath = "."+File.separator+"result.txt";

    //本地执行main类时使用的相对路径
    final static String accPath = "src\\raw_data\\accounts.txt";
    final static String relaPath = "src\\raw_data\\relations.txt";
    final static String resPath = "src\\prediction_result\\result.txt";


    public static void MultiRead() throws ExecutionException, InterruptedException, IOException {
        //线程读accounts文件到bytes数组中
        byte[] nameMap = new byte[idNum * 16];
        AccountThread fileThread = new AccountThread(accPath, nameMap);
        fileThread.start();

        //线程池读relations文件并进行union操作
        RandomAccessFile[] outArr = new RandomAccessFile[taskNum];
        ExecutorService threadpool = Executors.newFixedThreadPool(threadNum);
        CompletionService<int[]> completionService = new ExecutorCompletionService<>(threadpool);


        try {
            int length = (int) new File(relaPath).length();
            //每个任务存储数组的大小
            int relaBlock = (relaNum * 2) / taskNum + 100;
            int numPerThred = length / taskNum;
            RandomAccessFile rf = new RandomAccessFile(relaPath, "r");
            //存储relations文件切割起始位置
            List<StartToEnd> seList = new ArrayList<>();
            //计算每个切割文件的起始位置
            calculateStartEnd(0, numPerThred, length, seList, rf);
            rf.close();

            for (int i = 0; i < taskNum; i++) {
                //让每个线程分别负责读取文件的不同部分
                outArr[i] = new RandomAccessFile(relaPath, "rw");
                ReadTask mft = new ReadTask(seList.get(i).getStart(), seList.get(i).getEnd(), outArr[i],relaBlock);
                completionService.submit(mft);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        UnionFind union = new UnionFind(idNum);
        //获取读线程的结果，并进行union操作
        for (int j = 0; j < taskNum; j++) {
            int[] ids = completionService.take().get();
            int idsSize = ids.length;
            for (int i = 0; i < idsSize; i = i + 2) {
                if (ids[i] == 0 && ids[i + 1] == 0) {
                    break;
                }
                union.unionElements(ids[i], ids[i + 1]);
            }
        }

        //获取最终的父节点数组
        int[] parents = union.parent;
        for (int i = 0; i < idNum; i++) {
            parents[i] = parents[parents[i]];
        }

        //开一个线程提前转换簇id （int-->char[]）
        int cluNum = idNum - relaNum;
        int[] clusterId = new int[cluNum];//int类型的簇id
        List<char[]> cluster = new ArrayList<>();//转换为char[]类型的簇id
        int[] cidLen = new int[cluNum];//索引之前所有簇id所占byte的大小
        ClusterThread ct = new ClusterThread(parents, cluster, clusterId, cidLen);
        ct.start();

        int[] resNUM = countSortIndex(parents, idNum);//将同簇元素升序排列在一起

        RandomAccessFile wFile = new RandomAccessFile(resPath, "rw");

        List<Future<byte[]>> writeFutureList = new ArrayList<>();


        int wtaskNum = 20;//写拼接任务数
        int blockSize = idNum / wtaskNum;//分块

        ct.join();//在多线程写之前阻塞一下，使ct线程跑完
        fileThread.join();

        //线程池拼接要写到文件中的字符串
        for (int i = 0; i < wtaskNum; i++) {
            int reStart = blockSize * i;
            int resEnd = blockSize * (i + 1) - 1;
            if (i == wtaskNum - 1) {
                resEnd = 5000000;
            }
            WriteTask wf = new WriteTask(parents, resNUM, cluster, nameMap, clusterId, cidLen, reStart, resEnd);
            writeFutureList.add(threadpool.submit(wf));
        }

        int resLength = cidLen[cidLen.length - 1] + 19 * cidLen.length + 17 * (idNum - cidLen.length);//计算result文件大小
        //按任务顺序写入文件
        MappedByteBuffer write = wFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, resLength);
        for (int i = 0; i < wtaskNum; i++) {
            byte[] bytes = writeFutureList.get(i).get();
            write.put(bytes);
        }
        //文件结尾处的\r\n
        write.put((byte) '\r');
        write.put((byte) '\n');

        threadpool.shutdown();
        wFile.close();
    }


    /**
     * @param array parent数组
     * @param k     数组长度
     * @return 以簇id升序 一个个簇    每个簇首元素的判断方法： parent[B[i]] == B[i]
     */
    private static int[] countSortIndex(int[] array, int k) {
        int[] C = new int[k];//索引为簇id, 值为簇中元素个数
        int length = array.length, sum = 0;
        int[] B = new int[length];
        for (int i = 0; i < length; i++) {
            C[array[i]] += 1;
        }
        for (int i = 0; i < k; i++) {
            sum += C[i];
            C[i] = sum;
        }
        for (int i = length - 1; i >= 0; i--) {
            B[C[array[i]] - 1] = i;
            C[array[i]]--;
        }
        return B;
    }

    /*
     * 计算文件分割的始末
     * */
    private static void calculateStartEnd(int start, int size, int fileLength, List<StartToEnd> list, RandomAccessFile rAccessFile) throws IOException {
        if (start > fileLength - 1) {
            return;
        }
        int endPosition = start + size - 1;
        int end;
        if (endPosition >= fileLength - 1) {
            end = fileLength - 1;
            StartToEnd se = new StartToEnd(start, end - 1);
            list.add(se);
            return;
        }

        rAccessFile.seek(endPosition);
        byte tmp = (byte) rAccessFile.read();
        while (tmp != '\n' && tmp != '\r') {
            endPosition++;
            if (endPosition >= fileLength - 1) {
                endPosition = fileLength - 1;
                break;
            }
            rAccessFile.seek(endPosition);
            tmp = (byte) rAccessFile.read();
        }
        end = endPosition;
        StartToEnd se = new StartToEnd(start, end);
        list.add(se);
        calculateStartEnd(endPosition + 2, size, fileLength, list, rAccessFile);
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {
        MultiRead();
    }

}




