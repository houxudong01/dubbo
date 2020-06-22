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
package org.apache.dubbo.rpc.cluster;

import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.ExtensionLoader;
import org.apache.dubbo.common.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Configurator. (SPI, Prototype, ThreadSafe)
 */
public interface Configurator extends Comparable<Configurator> {

    /**
     * Get the configurator url.
     *
     * @return configurator url.
     */
    URL getUrl();

    /**
     * Configure the provider url.
     *
     * @param url - old provider url.
     * @return new provider url.
     */
    URL configure(URL url);


    /**
     * 将配置规则 URL 集合，转换成对应的 Configurator 集合
     * <p>
     * Convert override urls to map for use when re-refer. Send all rules every time, the urls will be reassembled and
     * calculated
     * <p>
     * URL contract:
     * <ol>
     * <li>1.override://0.0.0.0/...( or override://ip:port...?anyhost=true)&para1=value1... means global rules
     * (all of the providers take effect)</li>
     * <li>2.override://ip:port...?anyhost=false Special rules (only for a certain provider)</li>
     * <li>3.override:// rule is not supported... ,needs to be calculated by registry itself</li>
     * <li>4.override://0.0.0.0/ without parameters means clearing the override</li>
     * </ol>
     *
     * @param urls URL list to convert
     * @return converted configurator list
     */
    static Optional<List<Configurator>> toConfigurators(List<URL> urls) {
        if (CollectionUtils.isEmpty(urls)) {
            return Optional.empty();
        }

        // 加载到 ConfiguratorFactory 扩展点的自适应扩展类
        ConfiguratorFactory configuratorFactory = ExtensionLoader.getExtensionLoader(ConfiguratorFactory.class)
                .getAdaptiveExtension();

        // 创建 Configurator 集合
        List<Configurator> configurators = new ArrayList<>(urls.size());
        for (URL url : urls) {
            // 若协议为 `empty://` ，意味着清空所有配置规则，因此返回空 Configurator 集合
            if (Constants.EMPTY_PROTOCOL.equals(url.getProtocol())) {
                configurators.clear();
                break;
            }
            // 对应第 4 条契约，不带参数的 override://0.0.0.0/ 表示清除 override
            Map<String, String> override = new HashMap<>(url.getParameters());
            //The anyhost parameter of override may be added automatically, it can't change the judgement of changing url
            // override 上的 anyhost 可能是自动添加的，不能影响改变url判断
            override.remove(Constants.ANYHOST_KEY);
            if (override.size() == 0) {
                configurators.clear();
                continue;
            }
            // 获得 Configurator 对象，并添加到 `configurators` 中
            configurators.add(configuratorFactory.getConfigurator(url));
        }
        // 排序
        Collections.sort(configurators);
        return Optional.of(configurators);
    }

    /**
     * Sort by host, then by priority
     * 1. the url with a specific host ip should have higher priority than 0.0.0.0
     * 2. if two url has the same host, compare by priority value；
     */
    @Override
    default int compareTo(Configurator o) {
        if (o == null) {
            return -1;
        }

        // host 升序
        int ipCompare = getUrl().getHost().compareTo(o.getUrl().getHost());
        // host is the same, sort by priority
        // 如果 host 相同，按照 priority 排序
        if (ipCompare == 0) {
            int i = getUrl().getParameter(Constants.PRIORITY_KEY, 0);
            int j = o.getUrl().getParameter(Constants.PRIORITY_KEY, 0);
            return Integer.compare(i, j);
        } else {
            return ipCompare;
        }
    }
}
