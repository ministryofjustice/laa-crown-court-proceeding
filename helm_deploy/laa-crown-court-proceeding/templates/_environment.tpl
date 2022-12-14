{{/* vim: set filetype=mustache: */}}
{{/*
Environment variables for service containers
*/}}
{{- define "laa-crown-court-proceeding.env-vars" }}
env:
  - name: AWS_REGION
    value: {{ .Values.aws_region }}
  - name: SENTRY_DSN
    value: {{ .Values.sentry_dsn }}
  - name: SENTRY_ENV
    value: {{ .Values.java.host_env }}
  - name: MAAT_API_BASE_URL
    value: {{ .Values.maatApi.baseUrl }}
  - name: MAAT_API_OAUTH_URL
    value: {{ .Values.maatApi.oauthUrl }}
  - name: MAAT_API_OAUTH_CLIENT_ID
    value: {{ .Values.maatApi.clientId }}
  - name: MAAT_API_OAUTH_CLIENT_SECRET
    value: {{ .Values.maatApi.clientSecret }}
  - name: CLOUD_PLATFORM_QUEUE_REGION
    value: {{ .Values.cloudplatform.aws.sqs.region }}
  - name: CLOUD_PLATFORM_QUEUE_ACCESS_KEY
    value: {{ .Values.cloudplatform.aws.sqs.accesskey }}
  - name: CLOUD_PLATFORM_QUEUE_SECRET_KEY
    value: {{ .Values.cloudplatform.aws.sqs.secretkey }}
  - name: HEARING_RESULTED_QUEUE
    value: {{ .Values.cloudplatform.aws.sqs.queue.hearingResulted }}
  - name: PROSECUTION_CONCLUDED_QUEUE
    value: {{ .Values.cloudplatform.aws.sqs.queue.prosecutionConcluded }}
  - name: DATASOURCE_HOST_PORT
    valueFrom:
      secretKeyRef:
        name: rds-postgresql-instance-output
        key: rds_instance_endpoint
  - name: DATASOURCE_DBNAME
    valueFrom:
      secretKeyRef:
        name: rds-postgresql-instance-output
        key: database_name
  - name: DATASOURCE_USERNAME
    valueFrom:
      secretKeyRef:
        name: rds-postgresql-instance-output
        key: database_username
  - name: DATASOURCE_PASSWORD
    valueFrom:
      secretKeyRef:
        name: rds-postgresql-instance-output
        key: database_password
{{- end -}}
