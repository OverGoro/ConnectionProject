mkdir -p tmp && touch tmp/models_java_files.txt &&\
 echo  > tmp/models_java_files.txt && \
 find \( -name '*BLM*.java' -o -name '*DALM*.java' -o -name '*DTO*.java' -o -name '*Converter*.java' \) -exec cat {} + >> tmp/models_java_files.txt
