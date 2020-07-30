# AWS Secrets Agent

Reads secrets from AWS SecretsManager and writes to a file. Intended to be run as a kubernetes Init Container to create a secrets.properties file in a volume mount that is
 accessible to other containers in the pod.

## Configuration Settings
| Key  | Required | Description | Example value |
| :--- | :--- | :--- | :--- |
| `secret.name`  | Yes  | Friendly name of the AWS Secret | `valws/lab.main.t.aor.valws.vaw2` |
| `region`  | Yes  | AWS Region where secrets are stored | `us-east-1` |
| `secrets.filename`  | Yes  | The relative path and filename of the output file. | `/secrets/secrets.properties` |
| `is.local.run`  | No | Whether this is a local run. Defaults to `false`  | `true` |

If `is.local.run` is `true`:
* /local/config/service.properties is used.
* local AWS credentials are used.

## Environment Variables
| Variable  | Required | Description | Example value |
| :--- | :--- | :--- | :--- |
| `DOCKER_REPO`  | Yes  | The Docker repository to deploy the image to | `testrepo` |

Alternatively, this can be passed in to the maven command using the `-D` option. For example:
`mvn clean install -Denv.DOCKER_REPO=test`

## To run and test locally
Run `run.sh`
This runs the Docker container.

## Example Kubernetes configurations

### Deployment.yaml
* An IAM Role with permissions to access SecretsManager is required.
* AWS Secrets Agent is configured as an Init Container.
* AWS Secrets Agent mounts and writes to a volume that is also mounted by another container in the pod.
* AWS Secrets Agent mounts the config volume and location that matches ConfigMap.yaml.

In the example below, the following keys are used for AWS Secrets Agent:
* `spec.template.metadata.annotations.iam.amazonaws.com/role`
* `spec.template.spec.initContainers`
* `spec.template.spec.containers.<valws>.volumeMounts.<app-secrets>`
* `spec.template.spec.volumes.<awssec-config>`
* `spec.template.spec.volumes.<app-secrets>`

```yaml
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: lab-main-t-aor-valws-vaw2
  namespace: workflow
  labels:
    environmentShortName: lab
    regionShortName: main
    partitionShortName: t
    serviceShortName: valws
    clusterSuffix: vaw2
    clusterTitle: lab-main-t-aor-valws-vaw2
spec:
  replicas: 2
  revisionHistoryLimit: 2
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 25%
      maxUnavailable: 25%
  selector:
    matchLabels:
      clusterTitle: lab-main-t-aor-valws-vaw2
  template:
    metadata:
      annotations:
        iam.amazonaws.com/role: arn:aws:iam::0000000001:role/CustomerManaged-ValidationWebService
      labels:
        environmentShortName: lab
        regionShortName: main
        partitionShortName: t
        serviceShortName: valws
        clusterSuffix: vaw2
        clusterTitle: lab-main-t-aor-valws-vaw2
    spec:
      initContainers:
        - name: awssec
          image: docker-lab.repo.com/awssec:0.0.3
          volumeMounts:
            - name: awssec-config
              mountPath: /app/config
            - name: app-secrets
              mountPath: /app/secrets
      containers:
      - name: valws
        image: docker-lab.repo.com/valws:2.0.8
        imagePullPolicy: Always
        ports:
        - containerPort: 8080
          name: service
        resources:
          requests:
            cpu: 100m
            memory: 2048Mi
          limits:
            cpu: 4000m
            memory: 2048Mi
        readinessProbe:
          httpGet:
            path: validation/management/alive
            port: 8080
          initialDelaySeconds: 10
          periodSeconds: 10
          timeoutSeconds: 60
          successThreshold: 1
          failureThreshold: 30
        livenessProbe:
          httpGet:
            path: validation/management/alive
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 20
          timeoutSeconds: 60
          successThreshold: 1
          failureThreshold: 3
        volumeMounts:
        - name: config
          mountPath: /app/config
        - name: app-secrets
          mountPath: /app/secrets
      volumes:
      - name: config
        configMap:
          name: lab-main-t-aor-valws-vaw2
          items:
          - key: service-properties
            path: service.properties
          - key: env-properties
            path: env.properties
      - name: awssec-config
        configMap:
          name: lab-main-t-aor-valws-vaw2
          items:
            - key: awssec-properties
              path: service.properties
            - key: awssec-env-properties
              path: env.properties
      - name: app-secrets
        emptyDir: {}

```

### ConfigMap.yaml
In the example below, the following keys are used for AWS Secrets Agent:
* `data.awssec-env-properties`
* `data.awssec-properties`

```yaml
---
apiVersion: v1
kind: ConfigMap
metadata:
  name: lab-main-t-aor-valws-vaw2
  namespace: workflow
  labels:
    environmentShortName: lab
    regionShortName: main
    partitionShortName: t
    serviceShortName: valws
    clusterSuffix: vaw2
    clusterTitle: lab-main-t-aor-valws-vaw2
data:
  env-properties: |
    ALIVE_MBEAN="application=ValidationService,name=aliveCheckConfiguration"
  service-properties: |
    ws.base.url=http://validation.test.corp.mycompany.com/validation/web
    ws.instance.name=Test
  awssec-env-properties: |
    LOG_CONFIG=/app/monitors/logback-syslog.xml
    LOG_LEVEL=INFO
  awssec-properties: |
    secret.name=valws/lab.main.t.aor.valws.vaw2
    region=us-east-1
    secrets.filename=/secrets/secrets.properties

```