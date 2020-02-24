package com.join.zookeeper2.util;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.LockSupport;

public class DistributeLock implements Watcher, AsyncCallback.StringCallback, AsyncCallback.Children2Callback ,AsyncCallback.StatCallback{

    ZooKeeper zooKeeper = ZKUtil.getZK();

    String threadName;

    String pathName;

    Thread thread;

    Integer reentrant = 0;

    /**
     * 加锁方法
     */
    public void lock(){
        threadName = Thread.currentThread().getName();
        thread = Thread.currentThread();
        //实现可重入锁
        byte[] data = new byte[0];
        try {
            data = zooKeeper.getData("/", false, new Stat());
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(threadName +" <==> "+new String(data));
        if (data.length != 0 && threadName.equals(new String(data))){
            System.out.println(this.thread.getName()+" 获得了可重入锁 ");
            this.reentrant++;
            return;
        }
        zooKeeper.create("/lock", threadName.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL,this,"lock");
        System.out.println(threadName +" waiting...");
        LockSupport.park();
    }

    /**
     * 解锁方法
     */
    public void unlock(){
        if (reentrant != 0){
            reentrant--;
            return;
        }
        try {
            zooKeeper.delete(pathName,-1);
        } catch (InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void processResult(int rc, String path, Object ctx, String name) {
        //记录一下自己创建的节点，也就是自己拿到的序号
        this.pathName = name;
        zooKeeper.getChildren("/",false,this,"getChildren");
    }

    @Override
    public void processResult(int rc, String path, Object ctx, List<String> children, Stat stat) {
        //排序并判断自己是不是序号最小的那个
        Collections.sort(children);
        int i = children.indexOf(pathName.substring(1));
        if (i == 0){
            //是的话允许执行同步代码块
            try {
                zooKeeper.setData("/", threadName.getBytes(),-1);
            } catch (KeeperException | InterruptedException e) {
                e.printStackTrace();
            }
            LockSupport.unpark(this.thread);
        }else {
            //否则watch住前一个节点
            zooKeeper.exists("/"+children.get(i-1),this,this,"watchLast");
        }
    }

    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        //如果自己没有watch到，说明上一个节点突然挂掉了。那就需要重新判断自己的位置
        if (KeeperException.create(KeeperException.Code.NONODE).code().intValue() == rc){
            System.err.println(threadName +" 出现问题");
            zooKeeper.getChildren("/",false,this,"getChildren");
        }
    }

    @Override
    public void process(WatchedEvent event) {
        switch (event.getType()) {
            case None:
                break;
            case NodeCreated:
                break;
            case NodeDeleted:
                //重新判断自己在队列中的位置
                zooKeeper.getChildren("/",false,this,"getChildren");
                break;
            case NodeDataChanged:
                break;
            case NodeChildrenChanged:
                break;
            case DataWatchRemoved:
                break;
            case ChildWatchRemoved:
                break;
        }
    }
}
