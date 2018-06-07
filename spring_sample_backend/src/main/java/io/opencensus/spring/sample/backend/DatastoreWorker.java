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

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * A {@code BackendWorker} that talks to Datastore.
 */
@Component
@ConditionalOnProperty(name = "spring.opencensus.sample.backend.datastore.enabled")
public class DatastoreWorker implements BackendWorker {

  private static final String KIND_NAME = "Person";
  private static final String KEY_NAME = "john.doe@gmail.com";
  private static final String ACCESS_TIME = "access_time";

  @Autowired
  private DatastoreWorkerProperties datastoreWorkerProperties;

  DatastoreWorker() {
  }

  @Override
  public void doInit() {
    Datastore datastore = DatastoreOptions.getDefaultInstance().toBuilder().setProjectId
        (datastoreWorkerProperties.getProjectId()).build().getService();
    KeyFactory keyFactory = datastore.newKeyFactory().setKind(KIND_NAME);
    Key key = keyFactory.newKey(KEY_NAME);
    Entity entity = Entity.newBuilder(key)
        .set("name", "John Doe")
        .set("age", 51)
        .set("favorite_food", "pizza")
        .build();
    datastore.put(entity);
  }

  @Override
  public void doWork() {
    Datastore datastore = DatastoreOptions.getDefaultInstance().toBuilder().setProjectId
        (datastoreWorkerProperties.getProjectId()).build().getService();
    KeyFactory keyFactory = datastore.newKeyFactory().setKind(KIND_NAME);
    Key key = keyFactory.newKey(KEY_NAME);
    Entity entity = datastore.get(key);
    if (entity == null) {
      throw new RuntimeException("Entry not present. Call init.");
    }
    long now = System.currentTimeMillis();
    entity = Entity.newBuilder(entity)
        .set(ACCESS_TIME, now)
        .build();
    datastore.update(entity);
    if (datastore.get(key).getLong(ACCESS_TIME) != now) {
      throw new RuntimeException("Invalid get after update.");
    }
  }

  @Override
  public void doCleanup() {
    Datastore datastore = DatastoreOptions.getDefaultInstance().toBuilder().setProjectId
        (datastoreWorkerProperties.getProjectId()).build().getService();
    KeyFactory keyFactory = datastore.newKeyFactory().setKind(KIND_NAME);
    Key key = keyFactory.newKey(KEY_NAME);
    datastore.delete(key);
  }
}
