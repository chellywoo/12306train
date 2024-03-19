//package com.lxq.train.batch.job;
//
//import org.springframework.scheduling.annotation.EnableScheduling;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
///***
// * - 只适合单体应用，集群时就不适合了，如果有多个节点同时跑批，这种情况只能增加分布式锁来解决，确保同一时刻只有一个节点进行跑批；
// * - 没法实时更改定时任务状态和策略
// */
//@Component
//@EnableScheduling
//public class SpringbootBatchJob {
//    @Scheduled(cron = "0/5 * * * * ?")
//    private void test(){
//        // 分布式锁来解决，确保同一时刻只有一个节点进行跑批
//        System.out.println("SpringbootBatchJob TEST");
//    }
//}
