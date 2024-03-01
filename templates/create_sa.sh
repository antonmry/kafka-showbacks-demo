#https://docs.confluent.io/cloud/current/access-management/identity/service-accounts.html


confluent iam service-account create {SERVICE_ACCOUNT_NAME} --description {SERVICE_ACCOUNT_DESCRIPTION}

confluent api-key create --service-account {SERVICE_ACCOUNT_ID} --resource {CLUSTER_ID}  --description {SERVICE_ACCOUNT_DESCRIPTION}
