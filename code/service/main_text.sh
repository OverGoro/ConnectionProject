mkdir -p tmp && touch tmp/main_java_files.txt &&\
 echo  > tmp/main_java_files.txt && \
 find \( -name '*Service*.java' -o -name '*Controller*.java' ! -name '*Json*' \) -exec cat {} + >> tmp/main_java_files.txt
