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

package io.opencensus.spring.brave;

import brave.propagation.CurrentTraceContext.ScopeDecorator;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.sleuth.autoconfig.TraceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

/**
 * {@link org.springframework.boot.autoconfigure.EnableAutoConfiguration
 * Auto-configuration} that allows inter-operation between Sleuth(Brave) and OpenCensus.
 */
@Configuration
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@ConditionalOnProperty(name = "spring.opencensus.brave.enabled", matchIfMissing = true)
@AutoConfigureBefore(TraceAutoConfiguration.class)
@EnableConfigurationProperties(OpenCensusBraveProperties.class)
public class OpenCensusBraveAutoConfiguration {

  // once sleuth injects a list of these, it should work intuitively!
  @Bean
  ScopeDecorator openCensusScopeDecorator() {
    return new OpenCensusBraveScopeDecorator();
  }
}
