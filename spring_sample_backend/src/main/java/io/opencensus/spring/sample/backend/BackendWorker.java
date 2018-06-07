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

/**
 * Backend worker interface.
 */
public interface BackendWorker {

  /**
   * Does init work. Called when the Backend receives a request to "/backend_init".
   */
  void doInit();

  /**
   * Does the real work. Called when the Backend receives a request to "/backend_work".
   */
  void doWork();

  /**
   * Does cleanup work. Called when the Backend receives a request to "/backend_cleanup".
   */
  void doCleanup();
}
