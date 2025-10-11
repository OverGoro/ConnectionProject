mkdir -p tmp && touch tmp/main_java_files.txt &&\
 echo  > tmp/main_java_files.txt && \
 find -name '*Service*.java' -name '*Controller*.java' -exec cat {} + > tmp/main_java_files.txt
