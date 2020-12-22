## 运行说明
#### 本地Main类main方法执行，使用如下的文件路径（在Main类中已加注释说明）
```
    final static String accPath = "src\\raw_data\\accounts.txt";
    final static String relaPath = "src\\raw_data\\relations.txt";
    final static String resPath = "src\\prediction_result\\result.txt";
```
#### 打成jar包运行，使用如下的文件路径（在Main类中已加注释说明）

```
    final static String accPath = "."+File.separator+"accounts.txt";
    final static String relaPath = "."+File.separator+"relations.txt";
    final static String resPath = "."+File.separator+"result.txt";
```
**将accounts.txt与relations.txt两个文件放到src\raw_data文件夹下，输出结果将产生在prediction_result文件夹下**

## 技术路线

项目的技术路线分为四点，分别为数据结构、多线程读取、改进的并查集算法和多线程写拼接。

####  数据结构

​	accounts.txt文件的md5值采用byte[]一维数组存储，每次存储16位。

​    relations.txt文件的id值采用int[]一维数组存储，每次存储一对关系id。

​    每个账户id所属簇(cid)用int[]一维数组存储，数组索引表示账户id，值代表id所属的簇(cid)。

​    簇id用List<char[]>列表按序存储，用于直接写入结果文件。

​    簇id所占长度用int[]一维数组存储，表示索引之前（包含该索引）所有簇id在result.txt中所占字节数。

#### 多线程读取

使用线程池并发读取relations.txt文件的id关系对。首先将relations.txt文件平均划分为多个段，记录段在relation.txt文件中的起始位置，若起始位置不在行的开头和结尾，则进行修正。然后读取文件中的id关系对并记录到int[]一维数组中。按照线程池中任务完成的先后顺序依次取出结果进行下一步并查集计算。

#### 改进的并查集算法

对从realtions.txt中读取的id，成对输入到并查集算法中，查找id所属的祖先节点，在查找过程中不断修正id的祖先节点。然后将二者的祖先节点根据值大小进行合并，以值小的祖先节点为二者共同的祖先节点。

传统并查集算法得到的id对应的簇（cid）数组是按照accounts.txt本身的id顺序进行排列，为了后续高效的写操作，基于计数排序的思想，对该数组根据簇（cid）的大小进行重排序，得到一个簇外相对有序、簇内绝对有序的数组。

#### 多线程写拼接

使用多线程进行拼接，并使用任务队列保证了拼接的有序性。首先对排序后的数组进行划分，平均分成多个子任务。对于每个子任务，首先计算将要写入result.txt中的byte[]大小，然后将簇id、各id的md5值按照官方输出要求进行有序拼接，得到写入result.txt的子数组。