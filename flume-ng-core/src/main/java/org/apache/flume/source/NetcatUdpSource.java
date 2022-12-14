/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.flume.source;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import org.apache.flume.ChannelException;
import org.apache.flume.Context;
import org.apache.flume.CounterGroup;
import org.apache.flume.Event;
import org.apache.flume.EventDrivenSource;
import org.apache.flume.conf.Configurable;
import org.apache.flume.conf.Configurables;
import org.apache.flume.event.EventBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;

public class NetcatUdpSource extends AbstractSource implements EventDrivenSource, Configurable {

  private int port;
  private String host = null;
  private Channel nettyChannel;
  private String remoteHostHeader = "REMOTE_ADDRESS";
  private EventLoopGroup group;

  private static final Logger logger = LoggerFactory
      .getLogger(NetcatUdpSource.class);

  private CounterGroup counterGroup = new CounterGroup();

  private static final String REMOTE_ADDRESS_HEADER = "remoteAddress";
  private static final String CONFIG_PORT = "port";
  private static final String CONFIG_HOST = "bind";

  public class NetcatHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    // extract line for building Flume event
    private Event extractEvent(ByteBuf in, SocketAddress remoteAddress) {

      Map<String, String> headers = new HashMap<String,String>();

      headers.put(remoteHostHeader, remoteAddress.toString());

      byte b = 0;
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      Event e = null;
      boolean doneReading = false;

      while (!doneReading && in.isReadable()) {
        b = in.readByte();
        // Entries are separated by '\n'
        if (b == '\n') {
          doneReading = true;
        } else {
          baos.write(b);
        }
      }

      e = EventBuilder.withBody(baos.toByteArray(), headers);

      return e;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
      try {
        Event e = extractEvent(packet.content(), packet.sender());
        if (e == null) {
          return;
        }
        getChannelProcessor().processEvent(e);
        counterGroup.incrementAndGet("events.success");
      } catch (ChannelException ex) {
        counterGroup.incrementAndGet("events.dropped");
        logger.error("Error writing to channel", ex);
      } catch (RuntimeException ex) {
        counterGroup.incrementAndGet("events.dropped");
        logger.error("Error retrieving event from udp stream, event dropped", ex);
      }
    }
  }

  @Override
  public void start() {
    // setup Netty server
    group = new NioEventLoopGroup();
    try {
      Bootstrap b = new Bootstrap();
      b.group(group)
          .channel(NioDatagramChannel.class)
          .option(ChannelOption.SO_BROADCAST, true)
          .handler(new NetcatHandler());
      if (host == null) {
        nettyChannel = b.bind(port).sync().channel();
      } else {
        nettyChannel = b.bind(host, port).sync().channel();
      }
    } catch (InterruptedException ex) {
      logger.warn("netty server startup was interrupted", ex);
    }

    super.start();
  }

  @Override
  public void stop() {
    logger.info("Netcat UDP Source stopping...");
    logger.info("Metrics:{}", counterGroup);
    group.shutdownGracefully();

    super.stop();
  }

  @Override
  public void configure(Context context) {
    Configurables.ensureRequiredNonNull(context, CONFIG_PORT);
    port = context.getInteger(CONFIG_PORT);
    host = context.getString(CONFIG_HOST);
    remoteHostHeader = context.getString(REMOTE_ADDRESS_HEADER);
  }

  @VisibleForTesting
  public int getSourcePort() {
    SocketAddress localAddress = nettyChannel.localAddress();
    if (localAddress instanceof InetSocketAddress) {
      InetSocketAddress addr = (InetSocketAddress) localAddress;
      return addr.getPort();
    }
    return 0;
  }
}
