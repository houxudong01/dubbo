/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.dubbo.demo.consumer;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.apache.dubbo.demo.DemoService;
import org.apache.dubbo.demo.consumer.comp.DemoServiceComponent;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

public class Application {
    /**
     * In order to make sure multicast registry works, need to specify '-Djava.net.preferIPv4Stack=true' before
     * launch the application
     */
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ConsumerConfiguration.class);
        context.start();
        DemoService service = context.getBean("demoServiceComponent", DemoServiceComponent.class);
        String hello = service.sayHello("world");
        System.out.println("result :" + hello);

        /**
         *  sayHello(String) rpc调用链路：
         * 1.消费方：
         * proxy0#sayHello(String)
         *   —> InvokerInvocationHandler#invoke(Object, Method, Object[])
         *     —> MockClusterInvoker#invoke(Invocation)
         *       —> AbstractClusterInvoker#invoke(Invocation)
         *         —> FailoverClusterInvoker#doInvoke(Invocation, List<Invoker<T>>, LoadBalance)
         *           —> Filter#invoke(Invoker, Invocation)  // 包含多个 Filter 调用
         *             —> ListenerInvokerWrapper#invoke(Invocation)
         *               —> AbstractInvoker#invoke(Invocation)
         *                 —> DubboInvoker#doInvoke(Invocation)
         *                   —> ReferenceCountExchangeClient#request(Object, int)
         *                     —> HeaderExchangeClient#request(Object, int)
         *                       —> HeaderExchangeChannel#request(Object, int)
         *                         —> AbstractPeer#send(Object)
         *                           —> AbstractClient#send(Object, boolean)
         *                             —> NettyChannel#send(Object, boolean)
         *                               —> NioClientSocketChannel#write(Object)
         * 2.服务提供方：
         * NettyHandler#messageReceived(ChannelHandlerContext, MessageEvent)
         *   —> AbstractPeer#received(Channel, Object)
         *     —> MultiMessageHandler#received(Channel, Object)
         *       —> HeartbeatHandler#received(Channel, Object)
         *         —> AllChannelHandler#received(Channel, Object)
         *           —> ExecutorService#execute(Runnable)    // 由线程池执行后续的调用逻辑
         *             -> ChannelEventRunnable#run()
         *               —> DecodeHandler#received(Channel, Object)
         *                 —> HeaderExchangeHandler#received(Channel, Object)
         *                   —> HeaderExchangeHandler#handleRequest(ExchangeChannel, Request)
         *                     —> DubboProtocol.requestHandler#reply(ExchangeChannel, Object)
         *                       —> Filter#invoke(Invoker, Invocation)
         *                         —> AbstractProxyInvoker#invoke(Invocation)
         *                           —> Wrapper0#invokeMethod(Object, String, Class[], Object[])
         *                             —> DemoServiceImpl#sayHello(String)
         */
    }

    @Configuration
    @EnableDubbo(scanBasePackages = "org.apache.dubbo.demo.consumer.comp")
    @PropertySource("classpath:/spring/dubbo-consumer.properties")
    @ComponentScan(value = {"org.apache.dubbo.demo.consumer.comp"})
    static class ConsumerConfiguration {

    }
}
