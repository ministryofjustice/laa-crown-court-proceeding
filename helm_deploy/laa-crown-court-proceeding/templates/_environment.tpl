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
  - name: CDA_BASE_URL
    value: {{ .Values.cdaApi.baseUrl }}
  - name: CDA_OAUTH_URL
    value: {{ .Values.cdaApi.oauthUrl }}
  - name: EVIDENCE_API_BASE_URL
    value: {{ .Values.evidenceApi.baseUrl }}
  - name: EVIDENCE_API_OAUTH_URL
    value: {{ .Values.evidenceApi.oauthUrl }}
  - name: CLOUD_PLATFORM_QUEUE_REGION
    value: {{ .Values.cloudPlatform.aws.sqs.region }}
  - name: HEARING_RESULTED_QUEUE
    value: {{ .Values.cloudPlatform.aws.sqs.queue.hearingResulted }}
  - name: PROSECUTION_CONCLUDED_QUEUE
    value: {{ .Values.cloudPlatform.aws.sqs.queue.prosecutionConcluded.url }}
  - name: PROSECUTION_CONCLUDED_LISTENER_ENABLED
    value: {{ .Values.cloudPlatform.aws.sqs.queue.prosecutionConcluded.listenerEnabled | quote }}
  - name: PROSECUTION_CONCLUDED_DATABASE_NAME
    value: {{ .Values.cloudPlatform.aws.sqs.queue.prosecutionConcluded.dbUsername | quote }}
  - name: PROSECUTION_CONCLUDED_SCHEDULE_ENABLED
    value: {{ .Values.cloudPlatform.aws.sqs.queue.prosecutionConcluded.scheduleEnabled | quote }}
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
  - name: MAAT_API_OAUTH_CLIENT_ID
    valueFrom:
        secretKeyRef:
            name: maat-api-oauth-client-id
            key: MAAT_API_OAUTH_CLIENT_ID
  - name: MAAT_API_OAUTH_CLIENT_SECRET
    valueFrom:
        secretKeyRef:
            name: maat-api-oauth-client-secret
            key: MAAT_API_OAUTH_CLIENT_SECRET
  - name: CDA_OAUTH_CLIENT_ID
    valueFrom:
        secretKeyRef:
            name: cda-oauth-client-id
            key: CDA_OAUTH_CLIENT_ID
  - name: CDA_OAUTH_CLIENT_SECRET
    valueFrom:
        secretKeyRef:
            name: cda-oauth-client-secret
            key: CDA_OAUTH_CLIENT_SECRET
  - name: EVIDENCE_API_OAUTH_CLIENT_ID
    valueFrom:
        secretKeyRef:
            name: evidence-oauth-client-id
            key: EVIDENCE_API_OAUTH_CLIENT_ID
  - name: EVIDENCE_API_OAUTH_CLIENT_SECRET
    valueFrom:
        secretKeyRef:
            name: evidence-oauth-client-secret
            key: EVIDENCE_API_OAUTH_CLIENT_SECRET
  - name: NOTIFY_KEY
    value: {{ .Values.emailClient.notify_key }}
  - name: NOTIFY_TEMPLATE_ID
    value: {{ .Values.emailClient.notify_template_id }}
  - name: NOTIFY_RECIPIENT
    value: {{ .Values.emailClient.notify_recipient }}

{{- end -}}
