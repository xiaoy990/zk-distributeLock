package com.join.zookeeper2.util;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class ZKUtil {

    private static ZooKeeper zooKeeper;

    private static final CountDownLatch c = new CountDownLatch(1);

    private static final String path = "192.168.27.66:2181,192.168.27.67:2181,192.168.27.68:2181/testLock";

    //获取zk对象
    public static ZooKeeper getZK(){
        try {
             zooKeeper = new ZooKeeper(path,1000,new DefaultWatch());
             c.await();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return zooKeeper;
    }

    //关闭连接
    public static void release(){
        try {
            zooKeeper.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //zk连接时的watcher
    static class DefaultWatch implements Watcher{
        @Override
        public void process(WatchedEvent event) {
            switch (event.getState()) {
                case Unknown:
                    break;
                case Disconnected:
                    break;
                case NoSyncConnected:
                    break;
                case SyncConnected:
                    c.countDown();
                    break;
                case AuthFailed:
                    break;
                case ConnectedReadOnly:
                    break;
                case SaslAuthenticated:
                    break;
                case Expired:
                    break;
                case Closed:
                    break;
            }
        }
    }
}

