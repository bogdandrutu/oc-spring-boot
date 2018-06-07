# Spring Boot with OpenCensus Sample

## Setup Credentials
1. Create a Google Cloud Platform Project
2. [Create a service account][CreateServiceAccountLink] with Trace Append permission. Furnish a new
JSON key and then set the credentials using the `GOOGLE_APPLICATION_CREDENTIALS` environment
variable or [using GCP Starter Core properties][GcpStarterCorePropertiesLink]. Alternatively, if you
have the [Google Cloud SDK][GoogleCloudSdkLink] installed and initialized and are logged in with
[application default credentials][ApplicationDefaultCredentialsLink], you can skip this step since
the sample will auto-discover those settings for you.
3. Enable the [Stackdriver Trace API][StackdriverTraceApiLink]

## Setup Bigtable
Follow the instructions in the [user documentation][BigtableInstanceLink] to create a Google Cloud
Platform project and Cloud Bigtable instance if necessary. You'll need to reference your project id
and instance id to run the application.

Make sure the right values are set in the `spring_sample_backend/config/application.properties`
* spring.opencensus.sample.backend.bigtable.enabled=true
* spring.opencensus.sample.backend.bigtable.projectId=YOUR_PROJECT_ID
* spring.opencensus.sample.backend.bigtable.instanceId=YOUR_INSTANCE_ID

## Setup CloudStorage
Follow the instructions in the [getting started][CloudStorageLink] to setup Cloud Storage and create
a Google Cloud Platform project if necessary. You'll need to reference your project id to run the
application.

Make sure the right values are set in the `spring_sample_backend/config/application.properties`
* spring.opencensus.sample.backend.cloudstorage.enabled=true
* spring.opencensus.sample.backend.cloudstorage.projectId=YOUR_PROJECT_ID

## Setup Datastore
Follow the instructions in the [getting started][DatastoreLink] to setup Datastore and create a
Google Cloud Platform project if necessary. You'll need to reference your project id to run the
application.

Make sure the right values are set in the `spring_sample_backend/config/application.properties`
* spring.opencensus.sample.backend.datastore.enabled=true
* spring.opencensus.sample.backend.datastore.projectId=YOUR_PROJECT_ID

## Run the Example

Setup the

```bash
$ mvn -pl :spring-sample-backend spring-boot:run -Dspring.config.location=spring_sample_backend/config/
$ mvn -pl :spring-sample-frontend spring-boot:run -Dspring.config.location=spring_sample_frontend/config/
```

* Go to `http://localhost:8081/` to check that the Frontend is up.
* Go to `http://localhost:8081/init` if this is the first time you are using the Sample. (e.g.
creates Bigtable table).
* Go to `http://localhost:8081/work` this can be called multiple times to generate traces.
* Go to `http://localhost:8081/cleanup` when you are done using the Sample. (e.g. deletes Bigtable
table)

To see the traces, navigate to Stackdriver Trace console's [Trace List][TraceListLink] view.

## Details about Implementation

The Frontend simply redirects incoming requests `/init`,`/work` and `/cleanup` to the
Backend via HTTP requests `/backend_init`,`/backend_work` and `/backend_cleanup`.

The Backend supports multiple types of workers including Bigtable, Datastore and Cloud Storage. For
every incoming request calls the corresponding action on the registered workers.

The current implementation uses [Sleuth][SleuthLink] and [GCP Spring Trace][GcpSpringTraceLink] to
generate traces for HTTP calls and propagate the `TraceContext` via Thread Local variables
in-process. For calls to GCP services (Bigtable, Datastore, Cloud Storage) which are instrumented
using [OpenCensus][OpenCensusLink] an extra module is required to translate the Brave TraceContext
into OpenCensus Span (see [here][OpenCensusBraveAutoConfigurationLink]).

### Caveat

1. The current design does not work properly if calls are going from libraries instrumented with
OpenCensus to libraries instrumented with Sleuth (e.g. http calls) but this should not be the case
in this example where only GCP client libraries are instrumented with OpenCensus.

[ApplicationDefaultCredentialsLink]: https://developers.google.com/identity/protocols/application-default-credentials
[BigtableInstanceLink]: https://cloud.google.com/bigtable/docs/creating-instance
[CloudStorageLink]: https://github.com/GoogleCloudPlatform/google-cloud-java/tree/master/google-cloud-clients/google-cloud-storage#getting-started
[CreateServiceAccountLink]: https://cloud.google.com/docs/authentication/getting-started#creating_the_service_account
[DatastoreLink]: https://github.com/GoogleCloudPlatform/google-cloud-java/tree/master/google-cloud-clients/google-cloud-datastore#getting-started
[GcpSpringTraceLink]: https://docs.spring.io/spring-cloud-gcp/docs/1.0.0.M2/reference/htmlsingle/#_spring_cloud_sleuth
[GcpStarterCorePropertiesLink]: https://github.com/spring-cloud/spring-cloud-gcp#spring-boot-starters
[GoogleCloudSdkLink]: https://cloud.google.com/sdk/
[OpenCensusLink]: https://opencensus.io/
[OpenCensusBraveAutoConfigurationLink]: https://github.com/bogdandrutu/oc-spring-boot/blob/master/src/main/java/io/opencensus/spring/brave
/OpenCensusBraveAutoConfiguration.java
[TraceListLink]: https://console.cloud.google.com/traces/traces
[SleuthLink]: https://cloud.spring.io/spring-cloud-sleuth/
[StackdriverTraceApiLink]: https://console.cloud.google.com/apis/api/cloudtrace.googleapis.com/overview
