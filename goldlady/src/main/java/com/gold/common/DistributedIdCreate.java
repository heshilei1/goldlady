package com.gold.common;

/**
 * Created by user on 2017/11/6.
 */


/**
 * Twitter_Snowflake<br>
 * SnowFlake的结构如下(每部分用-分开):<br>
 * 0 - 0000000000 0000000000 0000000000 0000000000 0 - 00000 - 00000 - 000000000000 <br>
 * 1位标识，由于long基本类型在Java中是带符号的，最高位是符号位，正数是0，负数是1，
 * 所以id一般是正数，最高位是0<br>
 * 41位时间截(毫秒级)，注意，41位时间截不是存储当前时间的时间截，
 * 而是存储时间截的差值（当前时间截 - 开始时间截)
 * 得到的值），这里的的开始时间截，一般是我们的id生成器开始使用的时间，
 * 由我们程序来指定的（如下下面程序IdWorker类的startTime属性）。
 * 41位的时间截，可以使用69年，年T = (1L << 41) / (1000L * 60 * 60 * 24 * 365) = 69<br>
 * <p>
 * 10位的数据机器位，可以部署在1024个节点，包括5位dataCenterId和5位workerId<br>
 * <p>
 * 12位序列，毫秒内的计数，12位的计数顺序号支持每个节点每毫秒(同一机器，同一时间截)产生4096个ID序号<br>
 * 加起来刚好64位，为一个Long型。<br>
 * SnowFlake的优点是，整体上按照时间自增排序，
 * 并且整个分布式系统内不会产生ID碰撞(由数据中心ID和机器ID作区分)，
 * 并且效率较高，经测试，SnowFlake每秒能够产生26万ID左右。
 */
public class DistributedIdCreate {


    //计算和2017-7-31 08:00:00 的毫秒差
    private final static long twepoch = 1501459200000L;
    private long sequence = 0L;
    /**
     * 机器id所占的位数
     */
    private final static long workerIdBits = 5L;
    /**
     * 序列在id中占的位数
     */
    private final static long sequenceBits = 10L;
    /**
     * 数据标识id所占的位数
     */
    private final static long dataCenterIdBits = 5L;

    /**
     * 支持的最大机器id，结果是31
     */
    public final static long maxWorkerId = -1L ^ -1L << workerIdBits;
    /**
     * 支持的最大数据标识id，结果是31
     */
    private final static long maxdataCenterId = -1L ^ (-1L << dataCenterIdBits);

    /**
     * 机器ID向左移sequenceBits
     */
    private final static long workerIdShift = sequenceBits;

    /**
     * 数据标识id向左移sequenceBits + workerIdBits位
     */
    private final static long dataCenterIdShift = sequenceBits + workerIdBits;

    /**
     * 时间截向左移sequenceBits + workerIdBits + dataCenterIdBits位
     */
    private final static long timestampLeftShift = sequenceBits + workerIdBits + dataCenterIdBits;

    /**
     * 生成序列的掩码，这里为4095 (0b111111111111=0xfff=4095)
     */
    private final long sequenceMask = -1L ^ (-1L << sequenceBits);

    private long lastTimestamp = -1L;
    /**
     * 工作机器ID(0~31)
     */
    private long workerId;

    /**
     * 数据中心ID(0~31)
     */
    private long dataCenterId;

    public DistributedIdCreate(final long workerId) {
        if (workerId > this.maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format(
                    "worker Id can't be greater than %d or less than 0",
                    this.maxWorkerId));
        }
        this.workerId = workerId;
        this.dataCenterId = 0;
    }

    public DistributedIdCreate(long workerId, long dataCenterId) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(
                    String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        if (dataCenterId > maxdataCenterId || dataCenterId < 0) {
            throw new IllegalArgumentException(
                    String.format("dataCenter Id can't be greater than %d or less than 0", maxdataCenterId));
        }
        this.workerId = workerId;
        this.dataCenterId = dataCenterId;
    }

    public DistributedIdCreate() {

        this(0,1);
    }

    public synchronized long nextId() {
        long timestamp = this.timeGen();
        if (this.lastTimestamp == timestamp) {
            this.sequence = (this.sequence + 1) & this.sequenceMask;
            if (this.sequence == 0) {
                timestamp = this.tilNextMillis(this.lastTimestamp);
            }
        } else {
            this.sequence = 0L;
        }

        if (timestamp < this.lastTimestamp) {
            timestamp = twepoch;
        }

        this.lastTimestamp = timestamp;
        return ((timestamp - twepoch) << timestampLeftShift)
                | (dataCenterId << dataCenterIdShift)
                | (this.workerId << this.workerIdShift)
                | (this.sequence);

    }


    private long tilNextMillis(final long lastTimestamp) {
        long timestamp = this.timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = this.timeGen();
        }
        return timestamp;
    }

    private long timeGen() {
        return System.currentTimeMillis();
    }

    public static void main(String[] args) {
        java.util.Random w1 =
                new java.util.Random();
        java.util.Random w2 =
                new java.util.Random();
        DistributedIdCreate idWorker = new DistributedIdCreate(w1.nextInt(32), w2.nextInt(32));
        for (int i = 0; i < 1000; i++) {
            long id = idWorker.nextId();
            System.out.println(id);
        }
    }

}
