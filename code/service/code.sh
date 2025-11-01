mkdir -p tmp && touch tmp/all_java_no_test_files.txt && echo > tmp/all_java_no_test_files.txt && find -name '*.java' ! -name '*Test.java' -exec cat {} + > tmp/all_java_no_test_files.txt && cp src/main/resources/application.properties tmp/ && cp build.gradle tmp/


mkdir -p tmp && touch tmp/all_java_no_test_files.txt && echo > tmp/all_java_no_test_files.txt && find -name '*.java' ! -name '*Test.java' -exec cat {} + > tmp/all_java_no_test_files.txt && cp src/main/resources/application.properties tmp/ && cp build.gradle tmp/
