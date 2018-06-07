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

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * A {@code BackendWorker} that talks to Cloud Storage.
 */
@Component
@ConditionalOnProperty(name = "spring.opencensus.sample.backend.cloudstorage.enabled")
public class CloudStorageWorker implements BackendWorker {
  private static final String BUCKET_NAME = "oc-sample-bucket-name";
  private static final String BLOB_NAME = "oc-sample-blob-name";
  private static final String CONTENT_STRING = "a simple blob";

  @Autowired
  private CloudStorageWorkerProperties cloudStorageWorkerProperties;

  CloudStorageWorker() {
  }

  @Override
  public void doInit() {
    Storage storage = StorageOptions.getDefaultInstance().toBuilder().setProjectId
        (cloudStorageWorkerProperties.getProjectId()).build().getService();
    storage.create(BucketInfo.of(BUCKET_NAME));
  }

  @Override
  public void doWork() {
    Storage storage = StorageOptions.getDefaultInstance().toBuilder().setProjectId
        (cloudStorageWorkerProperties.getProjectId()).build().getService();
    // Upload a blob.
    BlobId blobId = BlobId.of(BUCKET_NAME, BLOB_NAME);
    BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("text/plain").build();
    Blob blob = storage.create(blobInfo, CONTENT_STRING.getBytes(UTF_8));
    // Read a blob.
    byte[] content = storage.readAllBytes(blobId);
    String contentString = new String(content, UTF_8);
    if (!contentString.equals(CONTENT_STRING)) {
      throw new RuntimeException("Invalid read after upload.");
    }
    // Delete a blob.
    storage.delete(blobId);
  }

  @Override
  public void doCleanup() {
    Storage storage = StorageOptions.getDefaultInstance().toBuilder().setProjectId
        (cloudStorageWorkerProperties.getProjectId()).build().getService();
    storage.get(BUCKET_NAME).delete();
  }
}
