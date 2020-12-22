# 运行说明
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
    final static String resPath = "."+File.separator+"result"+System.currentTimeMillis()+".txt";
```
**将accounts.txt与relations.txt两个文件放到src\raw_data文件夹下，输出结果将产生在prediction_result文件夹下**