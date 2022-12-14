/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.flume.conf.channel;

import org.apache.flume.conf.ComponentWithClassName;

/**
 * Enumeration of built in channel selector types available in the system.
 */
public enum ChannelSelectorType implements ComponentWithClassName {

  /**
   * Place holder for custom channel selectors not part of this enumeration.
   */
  OTHER(null),

  /**
   * Replicating channel selector.
   */
  REPLICATING("org.apache.flume.channel.ReplicatingChannelSelector"),

  /**
   * Load balancing channel selector.
   */
  LOAD_BALANCING("org.apache.flume.channel.LoadBalancingChannelSelector"),

  /**
   * Multiplexing channel selector.
   */
  MULTIPLEXING("org.apache.flume.channel.MultiplexingChannelSelector");

  private final String channelSelectorClassName;

  private ChannelSelectorType(String channelSelectorClassName) {
    this.channelSelectorClassName = channelSelectorClassName;
  }

  @Deprecated
  public String getChannelSelectorClassName() {
    return channelSelectorClassName;
  }

  @Override
  public String getClassName() {
    return channelSelectorClassName;
  }
}
