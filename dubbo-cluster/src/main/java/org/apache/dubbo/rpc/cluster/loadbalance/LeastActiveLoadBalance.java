/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dubbo.rpc.cluster.loadbalance;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcStatus;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * LeastActiveLoadBalance
 * <p>
 * Filter the number of invokers with the least number of active calls and count the weights and quantities of these invokers.
 * If there is only one invoker, use the invoker directly;
 * if there are multiple invokers and the weights are not the same, then random according to the total weight;
 * if there are multiple invokers and the same weight, then randomly called.
 */
public class LeastActiveLoadBalance extends AbstractLoadBalance {

    public static final String NAME = "leastactive";

    @Override
    protected <T> Invoker<T> doSelect(List<Invoker<T>> invokers, URL url, Invocation invocation) {
        // Number of invokers
        int length = invokers.size();
        // The least active value of all invokers
        // 最小的活跃数
        int leastActive = -1;
        // The number of invokers having the same least active value (leastActive)
        // 具有相同“最小活跃数”的服务者提供者（以下用 Invoker 代称）数量
        int leastCount = 0;
        // The index of invokers having the same least active value (leastActive)
        // leastIndexs 用于记录具有相同“最小活跃数”的 Invoker 在 invokers 列表中的下标信息
        int[] leastIndexes = new int[length];
        // the weight of every invokers
        // 每个 invokers 的权重
        int[] weights = new int[length];
        // The sum of the warmup weights of all the least active invokes
        int totalWeight = 0;
        // The weight of the first least active invoke
        int firstWeight = 0;
        // Every least active invoker has the same weight value?
        boolean sameWeight = true;


        // Filter out all the least active invokers
        // 过滤出所有的调用次数（活跃数）等于最小调用次数（活跃数）的服务提供者invoker
        for (int i = 0; i < length; i++) {
            Invoker<T> invoker = invokers.get(i);
            // Get the active number of the invoke
            // 当前服务提供者invoker对应的活跃数
            int active = RpcStatus.getStatus(invoker.getUrl(), invocation.getMethodName()).getActive();
            // Get the weight of the invoke configuration. The default value is 100.
            // 计算当前服务提供者的权重
            int afterWarmup = getWeight(invoker, invocation);
            // save for later use
            // 保存当前服务提供者的权重
            weights[i] = afterWarmup;
            // If it is the first invoker or the active number of the invoker is less than the current least active number
            // 发现更小的活跃数，重新开始
            if (leastActive == -1 || active < leastActive) {
                // Reset the active number of the current invoker to the least active number
                leastActive = active;
                // Reset the number of least active invokers
                leastCount = 1;
                // Put the first least active invoker first in leastIndexs
                leastIndexes[0] = i;
                // Reset totalWeight
                totalWeight = afterWarmup;
                // Record the weight the first least active invoker
                firstWeight = afterWarmup;
                // Each invoke has the same weight (only one invoker here)
                sameWeight = true;
                // If current invoker's active value equals with leaseActive, then accumulating.
                // 当前 Invoker 的活跃数 active 与最小活跃数 leastActive 相同
            } else if (active == leastActive) {
                // Record the index of the least active invoker in leastIndexs order
                // 在 leastIndexs 中记录下当前 Invoker 在 invokers 集合中的下标
                leastIndexes[leastCount++] = i;
                // Accumulate the total weight of the least active invoker
                // 累加权重
                totalWeight += afterWarmup;
                // If every invoker has the same weight?
                // 检测当前 Invoker 的权重与 firstWeight 是否相等，
                // 不相等则将 sameWeight 置为 false
                if (sameWeight && i > 0
                        && afterWarmup != firstWeight) {
                    sameWeight = false;
                }
            }
        }
        // Choose an invoker from all the least active invokers
        // 如果只有一个最小活跃数 invoker 则直接返回
        if (leastCount == 1) {
            // If we got exactly one invoker having the least active value, return this invoker directly.
            return invokers.get(leastIndexes[0]);
        }

        // // 有多个 Invoker 具有相同的最小活跃数，但它们之间的权重不同
        if (!sameWeight && totalWeight > 0) {
            // If (not every invoker has the same weight & at least one invoker's weight>0), select randomly based on 
            // totalWeight.
            // 随机生成一个 [0, totalWeight) 之间的数字
            int offsetWeight = ThreadLocalRandom.current().nextInt(totalWeight);
            // Return a invoker based on the random value.
            // 循环让随机数减去具有最小活跃数的 Invoker 的权重值，
            // 当 offset 小于等于0时，返回相应的 Invoker
            for (int i = 0; i < leastCount; i++) {
                int leastIndex = leastIndexes[i];
                offsetWeight -= weights[leastIndex];
                if (offsetWeight < 0) {
                    return invokers.get(leastIndex);
                }
            }
        }
        // If all invokers have the same weight value or totalWeight=0, return evenly.
        // // 如果权重相同或权重为0时，随机返回一个 Invoker
        return invokers.get(leastIndexes[ThreadLocalRandom.current().nextInt(leastCount)]);
    }
}