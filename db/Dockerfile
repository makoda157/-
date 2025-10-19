FROM mysql:8.4

# DB名・ユーザー・パスワードは application.properties に合わせる
ENV MYSQL_DATABASE=daily_report_system \
    MYSQL_USER=repuser \
    MYSQL_PASSWORD=reppass \
    MYSQL_ROOT_PASSWORD=rootpass

# 初回起動時の初期化SQL（DB/ユーザーの再作成は冪等にしてあります）
COPY mysql_setup.sql /docker-entrypoint-initdb.d/01_setup.sql
