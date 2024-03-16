
cd {CONFLUENT_TOOLS_DIRECTORY}/bin

echo "########Starting consumers##########"

./kafka-console-consumer  --consumer.config {CONFIG_PROPERTIES_SA} --topic {TOPIC_CREATED_50_PARTITIONS} --bootstrap-server {BOOTSTRA_SERVER} > {LOG_DIRECTORY} &

sa2_pid1=$!

./kafka-console-consumer  --consumer.config  {CONFIG_PROPERTIES_SA} --topic demo_showback_topic_3_partitions --bootstrap-server {BOOTSTRA_SERVER} > {LOG_DIRECTORY} &

sa2_pid2=$!

./kafka-console-consumer  --consumer.config  {CONFIG_PROPERTIES_SA} --topic demo_showback_topic_50_partitions --bootstrap-server {BOOTSTRA_SERVER} > {LOG_DIRECTORY} &

sa3_pid1=$!

./kafka-console-consumer  --consumer.config  {CONFIG_PROPERTIES_SA} --topic demo_showback_topic_50_partitions --bootstrap-server {BOOTSTRA_SERVER} > {LOG_DIRECTORY} &

sa3_pid2=$!

./kafka-console-consumer  --consumer.config  {CONFIG_PROPERTIES_SA} --topic demo_showback_topic_50_partitions --bootstrap-server {BOOTSTRA_SERVER} > {LOG_DIRECTORY} &

sa3_pid3=$!

./kafka-console-consumer  --consumer.config  {CONFIG_PROPERTIES_SA} --topic demo_showback_topic_50_partitions --bootstrap-server {BOOTSTRA_SERVER} > {LOG_DIRECTORY} &

sa3_pid4=$!

./kafka-console-consumer  --consumer.config  {CONFIG_PROPERTIES_SA} --topic demo_showback_topic_50_partitions --bootstrap-server {BOOTSTRA_SERVER} > {LOG_DIRECTORY} &

sa3_pid5=$!

./kafka-console-consumer  --consumer.config  {CONFIG_PROPERTIES_SA} --topic demo_showback_topic_100_partitions --bootstrap-server {BOOTSTRA_SERVER} > {LOG_DIRECTORY} &

sa1_pid=$!


./kafka-console-consumer  --consumer.config  {CONFIG_PROPERTIES_SA} --topic demo_showback_topic_50_partitions --bootstrap-server {BOOTSTRA_SERVER} > {LOG_DIRECTORY} &

sa4_pid=$!
echo "########Starting producers##########"

./kafka-producer-perf-test --topic demo_showback_topic_50_partitions --num-records 200000 --record-size 1000 --throughput 10000000 --producer.config  {CONFIG_PROPERTIES_SA}


./kafka-producer-perf-test --topic demo_showback_topic_100_partitions --num-records 200000 --record-size 1000 --throughput 10000000 --producer.config  {CONFIG_PROPERTIES_SA}

./kafka-producer-perf-test --topic demo_showback_topic_50_partitions --num-records 200000 --record-size 1000 --throughput 10000000 --producer-props batch.size=64000 linger.ms=5 --producer.config  {CONFIG_PROPERTIES_SA}


./kafka-producer-perf-test --topic demo_showback_topic_3_partitions --num-records 200000 --record-size 1000 --throughput 10000000 --producer-props ack=all --producer.config {CONFIG_PROPERTIES_SA}

echo "########Killing consumers##########"

kill -9 $sa2_pid1 $sa2_pid2 $sa3_pid1 $sa3_pid2 $sa3_pid3 $sa3_pid4 $sa3_pid5 $sa1_pid $sa4_pid

