# Java 기반 이미지를 사용
FROM eclipse-temurin:17-jdk

# 작업 디렉토리 지정
WORKDIR /app

# 로컬 시스템에서 Docker 컨테이너로 파일 복사
ARG JAR_FILE=./build/libs/be-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

# 빌드 결과인 jar 파일을 실행하기 위한 명령 지정
ENTRYPOINT ["sh", "-c", "java -jar /app/app.jar"]



