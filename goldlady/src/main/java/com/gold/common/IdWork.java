package com.gold.common;

/**
 * Created by user on 2017/11/6.
 */
public enum IdWork {

    INSTANCE;
    private static java.util.Random w1 =
            new java.util.Random();
    private static java.util.Random w2 =
            new java.util.Random();

    private static DistributedIdCreate distributedIdCreate = new DistributedIdCreate(w1.nextInt(32), w2.nextInt(32));

    public long getId() {
        return distributedIdCreate.nextId();
    }

    public String getId(String name) {
        return name + distributedIdCreate.nextId();
    }

}
