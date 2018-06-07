/*
 * Copyright 2018, OpenCensus Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opencensus.spring.sample.backend;

import io.opencensus.exporter.trace.stackdriver.StackdriverTraceConfiguration;
import io.opencensus.exporter.trace.stackdriver.StackdriverTraceExporter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Backend application.
 */
@SpringBootApplication
@EnableAutoConfiguration
@RestController
@EnableConfigurationProperties({
    BigtableWorkerProperties.class,
    CloudStorageWorkerProperties.class,
    DatastoreWorkerProperties.class})
public class Backend {

  private static Logger logger = Logger.getLogger(Backend.class.getName());
  static final List<BackendWorker> workers = new LinkedList<>();

  public static void main(String[] args) throws IOException {
    ApplicationContext context = SpringApplication.run(Backend.class, args);

    try {
      workers.add(context.getBean(BigtableWorker.class));
      logger.info("Bigtable configured succeed.");
    } catch (Exception e) {
      logger.info("Bigtable configured failed: " + e.getMessage());
    }
    try {
      workers.add(context.getBean(DatastoreWorker.class));
      logger.info("Datastore configured succeed.");
    } catch (Exception e) {
      logger.info("Datastore configured failed: " + e.getMessage());
    }
    try {
      workers.add(context.getBean(CloudStorageWorker.class));
      logger.info("CloudStorage configured succeed.");
    } catch (Exception e) {
      logger.info("CloudStorage configured failed " + e.getMessage());
    }

    StackdriverTraceExporter.createAndRegister(StackdriverTraceConfiguration.builder().build());
  }

  @RequestMapping("/backend-init")
  public String doInit() {
    for (BackendWorker worker : workers) {
      try {
        worker.doInit();
      } catch (Exception e) {
        logger.info(worker.getClass().getName() + " failed init " + e.getMessage());
      }
    }
    return "Done init.";
  }

  @RequestMapping("/backend-work")
  public String doWork() {
    for (BackendWorker worker : workers) {
      try {
        worker.doWork();
      } catch (Exception e) {
        logger.info(worker.getClass().getName() + " failed work " + e.getMessage());
      }
    }
    return "Done work.";
  }

  @RequestMapping("/backend-cleanup")
  public String doCleanup() {
    for (BackendWorker worker : workers) {
      try {
        worker.doCleanup();
      } catch (Exception e) {
        logger.info(worker.getClass().getName() + " failed cleanup " + e.getMessage());
      }

    }
    return "Done cleanup.";
  }
}