mkdir -p tmp && touch tmp/kafka_java_files.txt && \
 echo  > tmp/kafka_java_files.txt && \
 find \( -name '*Response*.java' -o -name '*Command*.java' -o -name "*Constant*.java" \) -exec cat {} + >> tmp/kafka_java_files.txt
