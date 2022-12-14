/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * limitations under the License.
 */

package org.apache.flume.sink.kafka.util;

import kafka.server.KafkaConfig;
import kafka.server.KafkaServer;
import kafka.utils.TestUtils;
import org.apache.kafka.common.utils.Time;

import java.io.IOException;
import java.util.Properties;

/**
 * A local Kafka server for running unit tests.
 * Reference: https://gist.github.com/fjavieralba/7930018/
 */
public class KafkaLocal {

  public KafkaServer kafka;
  public ZooKeeperLocal zookeeper;

  public KafkaLocal(Properties kafkaProperties) throws IOException, InterruptedException {
    KafkaConfig kafkaConfig = KafkaConfig.fromProps(kafkaProperties);

    // start local kafka broker
    kafka = TestUtils.createServer(kafkaConfig, Time.SYSTEM);
  }

  public void start() throws Exception {
    kafka.startup();
  }

  public void stop() {
    kafka.shutdown();
  }

}