{{/* vim: set filetype=mustache: */}}
{{/*
Environment variables for service containers
*/}}
{{- define "laa-crown-court-proceeding.env-vars" }}
env:
  - name: AWS_REGION
    value: {{ .Values.aws_region }}
  - name: SENTRY_DSN
    valueFrom:
      secretKeyRef:
        name: sentry-dsn
        key: SENTRY_DSN
  - name: SENTRY_ENV
    value: {{ .Values.java.host_env }}
  - name: SENTRY_SAMPLE_RATE
    value: {{ .Values.sentry.sampleRate | quote }}
  - name: LOG_LEVEL
    value: {{ .Values.logging.level }}
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
    valueFrom:
      secretKeyRef:
        name: sqs-prosecution-concluded-db-username
        key: PROSECUTION_CONCLUDED_DB_USERNAME
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
    valueFrom:
      secretKeyRef:
        name: email-client-notify-key
        key: EMAIL_CLIENT_NOTIFY_KEY
  - name: NOTIFY_TEMPLATE_ID
    value: {{ .Values.emailClient.notify_template_id }}
  - name: NOTIFY_RECIPIENT
    value: {{ .Values.emailClient.notify_recipient }}
  - name: REPORT_CRON_REACTIVATED_CASES
    value: {{ .Values.cron.report_reactivated_cases }}

{{- end -}}
