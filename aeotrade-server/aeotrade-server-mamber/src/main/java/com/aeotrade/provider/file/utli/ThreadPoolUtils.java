package com.aeotrade.provider.file.utli;

/**线程池
 * @Author: yewei
 * @Date: 2020/4/2 21:00
 */


import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
@Slf4j
public class ThreadPoolUtils {
    //使用volatile关键字保其可见性
    volatile private static ThreadPoolExecutor threadPool = null;

    /**
     * 无返回值直接执行
     *
     * @param runnable
     */
    public static void execute(Runnable runnable) {
        getThreadPool().execute(runnable);
    }

    /**
     * 返回值直接执行
     *
     * @param callable
     */
    public static <T> Future<T> submit(Callable<T> callable) {
        return getThreadPool().submit(callable);
    }

    private static ThreadPoolExecutor getThreadPool() {
        try {
            if (threadPool != null) {
//                return threadPool;
            } else {
                synchronized (ThreadPoolUtils.class) {
                    if (threadPool == null) {//二次检查
                        //创建实例之前可能会有一些准备性的耗时工作
                        Thread.sleep(300);
                        int cpuNum = Runtime.getRuntime().availableProcessors();// 获取处理器数量
                        int threadNum = cpuNum * 2 + 1;// 根据cpu数量,计算出合理的线程并发数
                        threadPool = new ThreadPoolExecutor(
                                threadNum - 1,// 核心线程数
                                threadNum, // 最大线程数
                                Integer.MAX_VALUE, // 闲置线程存活时间
                                TimeUnit.MILLISECONDS,// 时间单位
                                new LinkedBlockingDeque<Runnable>(Integer.MAX_VALUE),// 线程队列
                                Executors.defaultThreadFactory(),// 线程工厂
                                new ThreadPoolExecutor.AbortPolicy() {// 队列已满,而且当前线程数已经超过最大线程数时的异常处理策略
                                    @Override
                                    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
                                        super.rejectedExecution(r, e);
                                    }
                                }
                        );
                    }
                }
            }
        } catch (InterruptedException e) {
            log.warn(e.getMessage());
        }
        return threadPool;
    }


}
