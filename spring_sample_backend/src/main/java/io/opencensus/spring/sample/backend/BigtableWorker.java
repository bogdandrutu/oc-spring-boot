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

import com.google.cloud.bigtable.hbase.BigtableConfiguration;
import java.io.IOException;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * A {@code BackendWorker} that talks to Bigtable.
 */
@Component
@ConditionalOnProperty(name = "spring.opencensus.sample.backend.bigtable.enabled")
final class BigtableWorker implements BackendWorker {

  // Refer to table metadata names by byte array in the HBase API
  private static final byte[] TABLE_NAME = Bytes.toBytes("HelloBigtable");
  private static final byte[] COLUMN_FAMILY_NAME = Bytes.toBytes("cf1");
  private static final byte[] COLUMN_NAME = Bytes.toBytes("greeting");
  private static final byte[] ROW_NAME = Bytes.toBytes("greeting0");

  // Write some friendly greetings to Cloud Bigtable
  private static final String GREETING = "Hello World!";

  @Autowired
  private BigtableWorkerProperties bigtableWorkerProperties;

  BigtableWorker() {
  }

  @Override
  public void doInit() {
    // Create the Bigtable connection, use try-with-resources to make sure it gets closed
    try (Connection connection = BigtableConfiguration.connect(
        bigtableWorkerProperties.getProjectId(),
        bigtableWorkerProperties.getInstanceId())) {
      // The admin API lets us create and delete tables
      Admin admin = connection.getAdmin();
      HTableDescriptor descriptor = new HTableDescriptor(TableName.valueOf(TABLE_NAME));
      descriptor.addFamily(new HColumnDescriptor(COLUMN_FAMILY_NAME));
      admin.createTable(descriptor);
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  @Override
  public void doCleanup() {
    // Create the Bigtable connection, use try-with-resources to make sure it gets closed
    try (Connection connection = BigtableConfiguration.connect(
        bigtableWorkerProperties.getProjectId(),
        bigtableWorkerProperties.getInstanceId())) {
      // The admin API lets us create and delete tables
      Admin admin = connection.getAdmin();
      TableName tableName = TableName.valueOf(TABLE_NAME);
      admin.disableTable(tableName);
      admin.deleteTable(tableName);
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  @Override
  public void doWork() {
    // Create the Bigtable connection, use try-with-resources to make sure it gets closed
    try (Connection connection = BigtableConfiguration.connect(
        bigtableWorkerProperties.getProjectId(),
        bigtableWorkerProperties.getInstanceId())) {
      // Retrieve the table.
      Table table = connection.getTable(TableName.valueOf(TABLE_NAME));

      // Put a single row into the table.
      Put put = new Put(ROW_NAME);
      put.addColumn(COLUMN_FAMILY_NAME, COLUMN_NAME, Bytes.toBytes(GREETING));
      table.put(put);

      // Get a single row from the table.
      Result getResult = table.get(new Get(ROW_NAME));
      String greeting = Bytes.toString(getResult.getValue(COLUMN_FAMILY_NAME, COLUMN_NAME));
      if (!greeting.equals(GREETING)) {
        throw new RuntimeException("Invalid get after put.");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}