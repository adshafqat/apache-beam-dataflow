
## Build and Deploy
- Clone this repository (for better understanding the code, use the ```feature``` branch rather than ```master``` branch as the master branch will always content the latest code)
- Open a command prompt and cd (change directory) to the root folder of the cloned repository
- Build the jar using command 
```
mvn clean install
```
- Build and push the template in Artifact Registry of GCP using command 
```
gcloud dataflow flex-template build gs://<BUCKET_PATH_WITH_FOLDER>/dataflow-template.json --image-gcr-path="<ARTIFACT_REGISTRY_PATH>/dataflow:latest" --sdk-language=JAVA --flex-template-base-image=JAVA11 --jar="<FULL_PATH_OF_YOUR_CLONED_FOLDER>\target\pipeline-<VERSION>.jar" --env=FLEX_TEMPLATE_JAVA_MAIN_CLASS="com.crosscutdata.pipeline.textio.TextIOPipeline"



gcloud dataflow flex-template build gs://fiverr_temp/dataflow/dataflow-template.json --image-gcr-path="us-central1-docker.pkg.dev/fiverr-2431/dataflow/dataflow:latest" --sdk-language=JAVA --flex-template-base-image=JAVA11 --jar="target/streaming-1.0.0.jar" --env=FLEX_TEMPLATE_JAVA_MAIN_CLASS="com.crosscutdata.streaming.StreamingPipeline"



gcloud dataflow flex-template build gs://fiverr_temp/dataflow/dataflow-transform-template.json --image-gcr-path="us-central1-docker.pkg.dev/fiverr-2431/dataflow/dataflow-transform:latest" --sdk-language=JAVA --flex-template-base-image=JAVA11 --jar="target/streaming-1.0.0.jar" --env=FLEX_TEMPLATE_JAVA_MAIN_CLASS="com.crosscutdata.pipeline.textio.TextIOPipeline"

gcloud dataflow flex-template build gs://fiverr_temp/dataflow/dataflow-transform-exception-template.json --image-gcr-path="us-central1-docker.pkg.dev/fiverr-2431/dataflow/dataflow-transform-exception:latest" --sdk-language=JAVA --flex-template-base-image=JAVA11 --jar="target/streaming-1.0.0.jar" --env=FLEX_TEMPLATE_JAVA_MAIN_CLASS="com.crosscutdata.pipeline.textio.TextIOPipeline"


gcloud dataflow flex-template build gs://fiverr_temp/dataflow/dataflow-transform-parameters-template.json --image-gcr-path="us-central1-docker.pkg.dev/fiverr-2431/dataflow/dataflow-transform-parameters:latest" --sdk-language=JAVA --flex-template-base-image=JAVA11 --jar="target/pipeline-1.3.0.jar" --env=FLEX_TEMPLATE_JAVA_MAIN_CLASS="com.crosscutdata.pipeline.textio.TextIOPipeline"


```
- Deploy the template in Dataflow of GCP using command 
```
gcloud dataflow flex-template run "data-pipeline" --template-file-gcs-location="gs://<BUCKET_PATH_WITH_FOLDER>/dataflow-template.json" --parameters=configPath=crosscutdata-gcp:crosscutdata-bucket:configs/config_1.3.json


gcloud dataflow flex-template run "streaming-etl" --template-file-gcs-location="gs://fiverr_temp/dataflow/dataflow-template.json"


gcloud dataflow flex-template run "data-transformation-pipeline" --template-file-gcs-location="gs://fiverr_temp/dataflow/dataflow-transform-template.json"

gcloud dataflow flex-template run "data-transformation-exception-pipeline" --template-file-gcs-location="gs://fiverr_temp/dataflow/dataflow-transform-exception-template.json"

gcloud dataflow flex-template run "data-transformation-parameters-pipeline" --template-file-gcs-location="gs://fiverr_temp/dataflow/dataflow-transform-parameters-template.json" --parameters=configPath=fiverr-2431:fiverr_temp:dataflow/config_1.3.json



mvn compile exec:java -D exec.mainClass=com.crosscutdata.pipeline.textio.WordCount -D exec.args="--inputFile=sample.txt --output=counts" -P direct-runner


```
- Go to GCP Dataflow console to check
