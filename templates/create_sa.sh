#https://docs.confluent.io/cloud/current/access-management/identity/service-accounts.html

#The service account description have to follow the format described to achieve that KafkaShowBack services works.
confluent iam service-account create {SERVICE_ACCOUNT_NAME} --description "Service account for the {CLUSTER},{TEAM},{APPLICATION} application"

confluent api-key create --service-account {SERVICE_ACCOUNT_ID} --resource {CLUSTER_ID}  --description {SERVICE_ACCOUNT_DESCRIPTION}
