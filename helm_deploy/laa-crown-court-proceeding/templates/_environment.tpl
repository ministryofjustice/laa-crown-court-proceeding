{{/* vim: set filetype=mustache: */}}
{{/*
Environment variables for service containers
*/}}
{{- define "laa-crown-court-proceeding.env-vars" }}
env:
  - name: AWS_REGION
    value: {{ .Values.aws_region }}
  - name: SENTRY_DSN
    value: {{ .Values.sentry.dsn }}
  - name: SENTRY_ENV
    value: {{ .Values.java.host_env }}
  - name: SENTRY_SAMPLE_RATE
    value: {{ .Values.sentry.sampleRate | quote }}
  - name: MAAT_API_BASE_URL
    value: {{ .Values.maatApi.baseUrl }}
  - name: MAAT_API_OAUTH_URL
    value: {{ .Values.maatApi.oauthUrl }}
  - name: MAAT_API_OAUTH_CLIENT_ID
    value: {{ .Values.maatApi.clientId }}
  - name: MAAT_API_OAUTH_CLIENT_SECRET
    value: {{ .Values.maatApi.clientSecret }}
  - name: CDA_BASE_URL
    value: {{ .Values.cdaApi.baseUrl }}
  - name: CDA_OAUTH_URL
    value: {{ .Values.cdaApi.oauthUrl }}
  - name: CDA_OAUTH_CLIENT_ID
    value: {{ .Values.cdaApi.clientId }}
  - name: CDA_OAUTH_CLIENT_SECRET
    value: {{ .Values.cdaApi.clientSecret }}
  - name: EVIDENCE_API_BASE_URL
    value: {{ .Values.evidenceApi.baseUrl }}
  - name: EVIDENCE_API_OAUTH_URL
    value: {{ .Values.evidenceApi.oauthUrl }}
  - name: EVIDENCE_API_OAUTH_CLIENT_ID
    value: {{ .Values.evidenceApi.clientId }}
  - name: EVIDENCE_API_OAUTH_CLIENT_SECRET
    value: {{ .Values.evidenceApi.clientSecret }}
  - name: CLOUD_PLATFORM_QUEUE_REGION
    value: {{ .Values.cloudPlatform.aws.sqs.region }}
  - name: CLOUD_PLATFORM_QUEUE_ACCESS_KEY
    value: {{ .Values.cloudPlatform.aws.sqs.accessKey }}
  - name: CLOUD_PLATFORM_QUEUE_SECRET_KEY
    value: {{ .Values.cloudPlatform.aws.sqs.secretKey }}
  - name: HEARING_RESULTED_QUEUE
    value: {{ .Values.cloudPlatform.aws.sqs.queue.hearingResulted }}
  - name: PROSECUTION_CONCLUDED_QUEUE
    value: {{ .Values.cloudPlatform.aws.sqs.queue.prosecutionConcluded.url }}
  - name: PROSECUTION_CONCLUDED_LISTENER_ENABLED
    value: {{ .Values.cloudPlatform.aws.sqs.queue.prosecutionConcluded.listenerEnabled }}
  - name: PROSECUTION_CONCLUDED_SCHEDULE_ENABLED
      value: {{ .Values.cloudPlatform.aws.sqs.queue.prosecutionConcluded.scheduleEnabled }}
  - name: JWT_ISSUER_URI
    value: {{ .Values.jwt.issuerUri }}
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
