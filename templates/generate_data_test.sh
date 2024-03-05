
cd {CONFLUENT_TOOLS_DIRECTORY}/bin

echo "########Sa 2 is consuming data for topic with 50 parttions ############"

./kafka-console-consumer  --consumer.config {CONFIG_PROPERTIES_SA_2} --topic {TOPIC_CREATED_50_PARTITIONS} --bootstrap-server {BOOTSTRA_SERVER} > {LOG_DIRECTORY} &

sa2_pid1=$!

./kafka-console-consumer  --consumer.config /Users/irodriguez/Documents/Conferences/Confluent2024/config_sa2.properties --topic demo_showback_topic_3_partitions --bootstrap-server pkc-gq156m.eastus.azure.confluent.cloud:9092 > {LOG_DIRECTORY} &

sa2_pid2=$!
echo "###########Sa 3 is consuming with 5 consumers data for topic with 50 parttions#############"

./kafka-console-consumer  --consumer.config /Users/irodriguez/Documents/Conferences/Confluent2024/config_sa3.properties --topic demo_showback_topic_50_partitions --bootstrap-server pkc-gq156m.eastus.azure.confluent.cloud:9092 > {LOG_DIRECTORY} &

sa3_pid1=$!

./kafka-console-consumer  --consumer.config /Users/irodriguez/Documents/Conferences/Confluent2024/config_sa3.properties --topic demo_showback_topic_50_partitions --bootstrap-server pkc-gq156m.eastus.azure.confluent.cloud:9092 > {LOG_DIRECTORY} &

sa3_pid2=$!

./kafka-console-consumer  --consumer.config /Users/irodriguez/Documents/Conferences/Confluent2024/config_sa3.properties --topic demo_showback_topic_50_partitions --bootstrap-server pkc-gq156m.eastus.azure.confluent.cloud:9092 > {LOG_DIRECTORY} &

sa3_pid3=$!

./kafka-console-consumer  --consumer.config /Users/irodriguez/Documents/Conferences/Confluent2024/config_sa3.properties --topic demo_showback_topic_50_partitions --bootstrap-server pkc-gq156m.eastus.azure.confluent.cloud:9092 > {LOG_DIRECTORY} &

sa3_pid4=$!

./kafka-console-consumer  --consumer.config /Users/irodriguez/Documents/Conferences/Confluent2024/config_sa3.properties --topic demo_showback_topic_50_partitions --bootstrap-server pkc-gq156m.eastus.azure.confluent.cloud:9092 > {LOG_DIRECTORY} &

sa3_pid5=$!

echo "########Sa 1 is consuming data for topic with 100 parttions##########"
#compare sa1 with increase CKU + Parttions

./kafka-console-consumer  --consumer.config /Users/irodriguez/Documents/Conferences/Confluent2024/config_sa1.properties --topic demo_showback_topic_100_partitions --bootstrap-server pkc-gq156m.eastus.azure.confluent.cloud:9092 > {LOG_DIRECTORY} &

sa1_pid=$!

echo "########Sa 4  is consuming data for topic with 100 parttions but with bacths size configure##########"

#compare s4 with sa2 network write

./kafka-console-consumer  --consumer.config /Users/irodriguez/Documents/Conferences/Confluent2024/config_sa4.properties --topic demo_showback_topic_50_partitions --bootstrap-server pkc-gq156m.eastus.azure.confluent.cloud:9092 > {LOG_DIRECTORY} &

sa4_pid=$!

echo "########Sa 1 is producing data for topic with 50 parttions##########"

./kafka-producer-perf-test --topic demo_showback_topic_50_partitions --num-records 200000 --record-size 1000 --throughput 10000000 --producer.config /Users/irodriguez/Documents/Conferences/Confluent2024/config_sa1.properties 

#sa1_pid=$!

echo "########Sa 3 is producing data for topic with 100 parttions##########"

./kafka-producer-perf-test --topic demo_showback_topic_100_partitions --num-records 200000 --record-size 1000 --throughput 10000000 --producer.config /Users/irodriguez/Documents/Conferences/Confluent2024/config_sa3.properties 

echo "########Sa 1 is producing data for topic with 50 parttions with non default configurations##########"

./kafka-producer-perf-test --topic demo_showback_topic_50_partitions --num-records 200000 --record-size 1000 --throughput 10000000 --producer-props batch.size=64000 linger.ms=5 --producer.config /Users/irodriguez/Documents/Conferences/Confluent2024/config_sa1.properties 


echo "########Sa 1 is producing data for topic with 3 parttions with ACK ALL##########"

./kafka-producer-perf-test --topic demo_showback_topic_3_partitions --num-records 200000 --record-size 1000 --throughput 10000000 --producer-props ack=all --producer.config /Users/irodriguez/Documents/Conferences/Confluent2024/config_sa1.properties 


echo "########Kill consumers and producers##########"


kill -9 $sa2_pid1
kill -9 $sa2_pid2=
kill -9 $sa3_pid1 
kill -9 $sa3_pid2
kill -9 $sa3_pid3
kill -9 $sa3_pid4
kill -9 $sa3_pid5
kill -9 $sa1_pid
kill -9 $sa4_pid
##$sa1_pid

