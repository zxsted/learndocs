通过设置输入分片大小控制 task的个数


####yarn下map数的控制
[传送]:http://www.cnblogs.com/chengxin1982/p/3844900.html
FileInputFormat 函数的部分源代码：
```java
public List<InputSplit> getSplits(JobContext job) throws IOException {
        long minSize = Math.max(getFormatMinSplitSize(), getMinSplitSize(job));
        long maxSize = getMaxSplitSize(job);

        List splits = new ArrayList();
        List files = listStatus(job);
        for (FileStatus file : files) {
            Path path = file.getPath();
            long length = file.getLen();
            if (length != 0L) {
                FileSystem fs = path.getFileSystem(job.getConfiguration());
                BlockLocation[] blkLocations = fs.getFileBlockLocations(file,
                        0L, length);
                if (isSplitable(job, path)) {
                    long blockSize = file.getBlockSize();
                    long splitSize = computeSplitSize(blockSize, minSize,
                            maxSize);

                    long bytesRemaining = length;
                    while (bytesRemaining / splitSize > 1.1D) {
                        int blkIndex = getBlockIndex(blkLocations, length
                                - bytesRemaining);
                        splits.add(makeSplit(path, length - bytesRemaining,
                                splitSize, blkLocations[blkIndex].getHosts()));

                        bytesRemaining -= splitSize;
                    }

                    if (bytesRemaining != 0L) {
                        int blkIndex = getBlockIndex(blkLocations, length
                                - bytesRemaining);
                        splits.add(makeSplit(path, length - bytesRemaining,
                                bytesRemaining,
                                blkLocations[blkIndex].getHosts()));
                    }
                } else {
                    splits.add(makeSplit(path, 0L, length,
                            blkLocations[0].getHosts()));
                }
            } else {
                splits.add(makeSplit(path, 0L, length, new String[0]));
            }
        }

        job.getConfiguration().setLong(
                "mapreduce.input.fileinputformat.numinputfiles", files.size());
        LOG.debug("Total # of splits: " + splits.size());
        return splits;
    }
```

关键代码是：
```java
long minSize = Math.max(getFormatMinSplitSize(), getMinSplitSize(job));
long maxSize = getMaxSplitSize(job);
```
以及
```jva
  long splitSize = computeSplitSize(blockSize, minSize,
                            maxSize);
```
可以在同文件中找到上面三个函数的定义

```java
 public static long getMinSplitSize(BSPJob job) {
    return job.getConfiguration().getLong("bsp.min.split.size", 1L);
  }

.....

 public static void setMaxInputSplitSize(Job job, long size) {
    job.getConfiguration().setLong("bsp.max.split.size", size);
  }

.....

 public static long getMaxSplitSize(BSPJob context) {
    return context.getConfiguration().getLong("bsp.max.split.size",
        Long.MAX_VALUE);
  }

......
 protected long computeSplitSize(long goalSize, long minSize, long blockSize) {
    if (goalSize > blockSize) {
      return Math.max(minSize, Math.max(goalSize, blockSize));
    } else {
      return Math.max(minSize, Math.min(goalSize, blockSize));
    }
  }
```

 Yarn 下好像没了1*下的由用户设置预期的Map数
 
```java
核心代码
 
long minSize = Math.max(getFormatMinSplitSize(), getMinSplitSize(job));
 
getFormatMinSplitSize 默认返回1，getMinSplitSize 为用户设置的最小分片数, 如果用户设置的大于1，则为用户设置的最小分片数
long maxSize = getMaxSplitSize(job);
 
getMaxSplitSize为用户设置的最大分片数，默认最大为9223372036854775807L
 
long splitSize = computeSplitSize(blockSize, minSize,
                            maxSize);
 
protected long computeSplitSize(long blockSize, long minSize, long maxSize) {
        return Math.max(minSize, Math.min(maxSize, blockSize));
    }

```

测试 文件大小 297M(311349250)

块大小128M

测试代码

测试1
```java
   FileInputFormat.setMinInputSplitSize(job, 301349250);
   FileInputFormat.setMaxInputSplitSize(job, 10000);
```
测试后Map个数为1，由上面分片公式算出分片大小为301349250, 比 311349250小， 理论应该为两个map,  再看分片函数
```java
while (bytesRemaining / splitSize > 1.1D) {
                        int blkIndex = getBlockIndex(blkLocations, length
                                - bytesRemaining);
                        splits.add(makeSplit(path, length - bytesRemaining,
                                splitSize, blkLocations[blkIndex].getHosts()));

                        bytesRemaining -= splitSize;
                    }
```
只要剩余的文件大小不超过分片大小的1.1倍， 则会分到一个分片中，避免开两个MAP， 其中一个运行数据太小，浪费资源。

测试2
```java
FileInputFormat.setMinInputSplitSize(job, 150*1024*1024);
FileInputFormat.setMaxInputSplitSize(job, 10000);
```

MAP 数为2

测试3

在原有的输入目录下，添加一个很小的文件,几K，测试是否会合并
```java
FileInputFormat.setMinInputSplitSize(job, 150*1024*1024);
FileInputFormat.setMaxInputSplitSize(job, 10000);
```
Map数变为了3
看源代码
```java
for (FileStatus file : files) {

..

}
```
原来输入是按照文件名来分片的，这个按照常理也能知道， 不同的文件内容格式不同

总结，分片过程大概为，先遍历目标文件，过滤部分不符合要求的文件， 然后添加到列表，然后按照文件名来切分分片 （大小为前面计算分片大小的公式, 最后有个文件尾可能合并，其实常写网络程序的都知道）， 然后添加到分片列表，然后每个分片读取自身对应的部分给MAP处理

#####FileInputFormat(新接口org.apache.hadoop.mapreduce.lib.input)

[hadoop各种输入方法(InputFormat)汇总]http://www.blogjava.net/shenh062326/archive/2012/07/03/hadoop.html
Hadoop 0.20
开始定义了一套新的mapreduce编程接口, 使用新的FileInputFormat, 它与旧接口下的FileInputFormat主要区别在于, 它不再使用mapred.map.tasks, 而使用mapred.max.split.size参数代替goalSize, 通过Math.max(minSize, Math.min(maxSize, blockSize))决定map输入长度, 一个map的输入要大于minSize,小于

Math.min(maxSize, blockSize).

    若需增加map数,可以把mapred.min.split.size调小,把mapred.max.split.size调大. 若需减少map数, 可以把mapred.min.split.size调大, 并把mapred.max.split.size调小.


对于hama bsp 
它重新写了FileInputFormat 类 核心代码如下：
```java
/**
   * Splits files returned by {@link #listStatus(BSPJob)} when they're too big. <br/>
   * numSplits will be ignored by the framework.
   */
  @Override
  public InputSplit[] getSplits(BSPJob job, int numSplits) throws IOException {
    long minSize = Math.max(getFormatMinSplitSize(), getMinSplitSize(job));
    long maxSize = getMaxSplitSize(job);
    
    // generate splits
    List<InputSplit> splits = new ArrayList<InputSplit>();
    FileStatus[] files = listStatus(job);
    for (FileStatus file : files) {
      Path path = file.getPath();
      FileSystem fs = path.getFileSystem(job.getConfiguration());
      long length = file.getLen();
      BlockLocation[] blkLocations = fs.getFileBlockLocations(file, 0, length);
      if ((length != 0) && isSplitable(job, path)) {
        long blockSize = file.getBlockSize();
        long splitSize = computeSplitSize(blockSize, minSize, maxSize);

        long bytesRemaining = length;
        while (((double) bytesRemaining) / splitSize > SPLIT_SLOP) {
          int blkIndex = getBlockIndex(blkLocations, length - bytesRemaining);
          splits.add(new FileSplit(path, length - bytesRemaining, splitSize,
              blkLocations[blkIndex].getHosts()));
          bytesRemaining -= splitSize;
        }

        if (bytesRemaining != 0) {
          splits
              .add(new FileSplit(path, length - bytesRemaining, bytesRemaining,
                  blkLocations[blkLocations.length - 1].getHosts()));
        }
      } else if (length != 0) {
        splits.add(new FileSplit(path, 0, length, blkLocations[0].getHosts()));
      } else {
        // Create empty hosts array for zero length files
        splits.add(new FileSplit(path, 0, length, new String[0]));
      }
    }

    // Save the number of input files in the job-conf
    job.getConfiguration().setLong("bsp.input.files", files.length);

    LOG.debug("Total # of splits: " + splits.size());
    return splits.toArray(new InputSplit[splits.size()]);
  }

```
可以在同文件中找到上面三个函数的定义

```java
 public static long getMinSplitSize(BSPJob job) {
    return job.getConfiguration().getLong("bsp.min.split.size", 1L);
  }

.....

 public static void setMaxInputSplitSize(Job job, long size) {
    job.getConfiguration().setLong("bsp.max.split.size", size);
  }

.....

 public static long getMaxSplitSize(BSPJob context) {
    return context.getConfiguration().getLong("bsp.max.split.size",
        Long.MAX_VALUE);
  }

......
 protected long computeSplitSize(long goalSize, long minSize, long blockSize) {
    if (goalSize > blockSize) {
      return Math.max(minSize, Math.max(goalSize, blockSize));
    } else {
      return Math.max(minSize, Math.min(goalSize, blockSize));
    }
  }
```

通过观察上面函数的定义，可以知道 能够以下面的方式控制每个peer任务的处理的分片的大小，同时也就控制了peer的task的个数，这样可以将计算密集型的工作划分到多个peer中进行：
```java
conf.setInt("dfs.blocksize", 10*1024*1024);     //设置文件划分为10M
conf.setLong("bsp.max.split.size", 10000);      //设置bsp的最大分片长度
conf.setLong("bsp.min.split.size",10*1024*1024); //设置bsp的最小分片长度
```
