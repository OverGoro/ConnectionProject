mkdir -p tmp && touch tmp/gradle_files.txt && echo  > tmp/gradle_files.txt && find -name '*build.gradle' -exec cat {} + > tmp/gradle_files.txt
