# https://www.confluent.io/blog/confluent-terraform-provider-intro/?ajs_aid=81ed1b46-7507-4e17-acb4-e91a1aa4b742&ajs_uid=870042
# Configure the Confluent Provider
terraform {
  required_providers {
    confluent = {
      source  = "confluentinc/confluent"
      version = "1.61.0"
    }
  }
}

provider "confluent" {
  cloud_api_key    = var.confluent_cloud_api_key    # optionally use CONFLUENT_CLOUD_API_KEY env var
  cloud_api_secret = var.confluent_cloud_api_secret # optionally use CONFLUENT_CLOUD_API_SECRET env var
}

resource "confluent_environment" "development" {
  display_name = REPLACE_BY_ENVIRONMENT_NAME

  lifecycle {
    prevent_destroy = true
  }
}

resource "confluent_kafka_cluster" "dedicated" {
  display_name = REPLACE_BY_CLUSTER_NAME
  availability = "SINGLE_ZONE" # can be replaced by multi zone
  cloud        = REPLACE_BY_CLOUD_PROVIDER
  region       = REPLACE_BY_CLOUD_REGION
  dedicated {
    cku = 1
  }
  environment {
    id = confluent_environment.development.id
  }

  lifecycle {
    prevent_destroy = true
  }
}

resource "confluent_service_account" "app-manager" {
  display_name = "cluster_name-application_name-organization_name"
  description  = "Service account for the cluster_name,organization_name,application_name application"
}

resource "confluent_kafka_topic" "orders" {
  topic_name         = "orders"

  lifecycle {
    prevent_destroy = true
  }
}



# Create more resources ...