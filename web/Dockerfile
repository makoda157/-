# JRE 17 の軽量イメージ
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# DB待機に使うncを入れる
RUN apk add --no-cache netcat-openbsd

# 手順2で作成したJARを web/ 配下に "app.jar" という名前で置いてください
COPY app.jar /app/app.jar

# 起動スクリプト
COPY startup.sh /app/startup.sh
RUN chmod +x /app/startup.sh

EXPOSE 8080

# 任意のJVMオプションを渡せるように
ENV JAVA_OPTS=""

ENTRYPOINT ["/app/startup.sh"]
