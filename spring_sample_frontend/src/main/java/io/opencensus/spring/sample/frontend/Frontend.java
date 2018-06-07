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

package io.opencensus.spring.sample.frontend;

import io.opencensus.exporter.trace.stackdriver.StackdriverTraceConfiguration;
import io.opencensus.exporter.trace.stackdriver.StackdriverTraceExporter;
import java.io.IOException;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * Frontend application.
 */
@SpringBootApplication
@EnableAutoConfiguration
@RestController
public class Frontend {

  @Autowired
  RestTemplate restTemplate;

  public static void main(String[] args) throws IOException {
    SpringApplication.run(Frontend.class, args);

    StackdriverTraceExporter.createAndRegister(StackdriverTraceConfiguration.builder().build());
  }

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @RequestMapping("/")
  public String callBackend() {
    return "Call done.";
  }

  @RequestMapping("/init")
  public String doInit() {
    return restTemplate.getForObject("http://localhost:9000/backend-init", String.class);
  }

  @RequestMapping("/work")
  public String doWork() {
    return restTemplate.getForObject("http://localhost:9000/backend-work", String.class);
  }

  @RequestMapping("/cleanup")
  public String doCleanup() {
    return restTemplate.getForObject("http://localhost:9000/backend-cleanup", String.class);
  }

}