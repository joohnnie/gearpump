/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.gearpump.cluster.main

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import com.typesafe.config.ConfigValueFactory
import org.apache.gearpump.cluster.{MasterClient, MasterProxy}
import org.apache.gearpump.util.Constants._
import org.apache.gearpump.util.{LogUtil, Configs, Util}
import org.slf4j.{Logger, LoggerFactory}

object Kill extends App with ArgumentsParser {

  private val LOG: Logger = LogUtil.getLogger(getClass)

  override val options: Array[(String, CLIOption[Any])] = Array(
    "master"-> CLIOption("<host1:port1,host2:port2,host3:port3>", required = true),
    "appid" -> CLIOption("<application id>", required = true))

  def start = {
    val config = parse(args)

    val masters = config.getString("master")
    LOG.info("Master URL: {}", masters)

    implicit val timeout = Timeout(5, TimeUnit.SECONDS)
    val system = ActorSystem("client", Configs.loadApplicationConfig()
      .withValue("akka.loglevel", ConfigValueFactory.fromAnyRef("WARNING")))
    val master = system.actorOf(Props(classOf[MasterProxy], Util.parseHostList(masters)), MASTER)

    val client = new MasterClient(master)
    LOG.info("Client ")
    client.shutdownApplication(config.getInt("appid"))

    system.shutdown()
  }

  start
}
