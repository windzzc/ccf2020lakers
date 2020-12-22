import java.util.List;
import java.util.concurrent.Callable;

/***
 * 按照resnumID划分，计算对应文件的byte[]大小，将拼接的字符串先写到byte数组中，返回byte[]数组
 * 参数， parents resNum, nameMap, cluster, cid(存储之前的簇id长度和),    reStart, resEnd
 * 返回值， 拼接的byte[]数组
 */
public class WriteTask implements Callable {
    public int[] parents;
    public int[] resNum;
    public List<char[]> cluster;
    public byte[] nameMap;
    public int[] clusterId;
    public int[] cidLen;
    public int reStart;
    public int resEnd;

    //中间结果
    public int cidFirstIndex;
    public int cidLastIndex;

    public WriteTask(int[] parents, int[] resNum, List<char[]> cluster, byte[] nameMap, int[] clusterId, int[] cidLen, int reStart, int resEnd) {
        this.parents = parents;
        this.resNum = resNum;
        this.cluster = cluster;
        this.nameMap = nameMap;
        this.clusterId = clusterId;
        this.cidLen = cidLen;
        this.reStart = reStart;
        this.resEnd = resEnd;//均是闭区间
    }

    /***
     * 计算byte[]数组大小
     * @return
     */
    public int calLength() {
        int cidFirst = reStart, cidLast = resEnd;
        //计算分块第一个cid
        //一个簇最多11个成员，因此至多循环11次跳出
        for (int i = reStart; i <= resEnd; i++) {
            int no = resNum[i];
            int pno = parents[no];
            //判断出第一个cid
            if (no == pno) {
                cidFirst = i;
                break;
            }
        }
        // 计算分块的最后一个cid
        for (int i = resEnd; i >= reStart; i--) {
            int no = resNum[i];
            int pno = parents[no];
            //判断出第一个cid
            if (no == pno) {
                cidLast = i;
                break;
            }
        }

        //二分查找，找到cidFirst在cluster数组中对应的索引
        cidFirstIndex = binarySearch(clusterId, resNum[cidFirst]);
        //二分查找，找到cidLast在cluster数组中对应的索引
        cidLastIndex = binarySearch(clusterId, resNum[cidLast]);

        int cidCount = cidLastIndex - cidFirstIndex + 1;//簇id在分块中的个数

        //此处 ，此处如果cidFirstIndex = 0,res会少1
        int cidLength = cidLen[cidLastIndex] - cidLen[cidFirstIndex == 0 ? 0 : cidFirstIndex - 1];//簇id所占byte长度
        int res = cidLength + cidCount * 19 + ((resEnd - reStart + 1) - cidCount) * 17;//分块的byte[]大小

        //特殊处理 cid=0的情况 去掉\r\n,但是要加上之前多减的cidLen[0] = 1
        if (reStart == 0) {
            res -= 2;
            res += 1;
        }
        return res;
    }

    int binarySearch(int[] nums, int target) {
        int left = 0;
        int right = nums.length - 1;

        while (left <= right) {
            int mid = (right + left) / 2;
            if (nums[mid] == target) {
                return mid;
            } else if (nums[mid] < target) {
                left = mid + 1;
            } else if (nums[mid] > target) {
                right = mid - 1;
            }
        }
        return -1;
    }

    @Override
    public byte[] call() throws InterruptedException {

        int bLength = calLength();
        byte[] temp = new byte[bLength];

        int cluIndex = cidFirstIndex; //cluster数组的索引
        int byteIndex = 0;//文件byte数组索引
        if (reStart == 0) {
            temp[byteIndex++] = '0';
            temp[byteIndex++] = ' ';
            for (int i = 0; i < 16; i++) {
                temp[byteIndex++] = nameMap[i];
            }
            reStart++;
            cluIndex++;
        }

        int i;
        for (i = reStart; i <= resEnd; i++) {
            int no = resNum[i];
            int pno = parents[no];

            if (no == pno) {
                temp[byteIndex++] = '\r';
                temp[byteIndex++] = '\n';
                char[] chars = cluster.get(cluIndex++);

                for (int k = 0; k < chars.length; k++) {
                    temp[byteIndex++] = (byte) chars[k];
                }
                temp[byteIndex++] = ' ';

                for (int j = 16 * no; j < 16 * no + 16; j++) {
                    temp[byteIndex++] = nameMap[j];
                }

            } else {
                temp[byteIndex++] = ',';
                for (int j = 16 * no; j < 16 * no + 16; j++) {
                    temp[byteIndex++] = nameMap[j];
                }
            }
        }

        return temp;

    }


}

