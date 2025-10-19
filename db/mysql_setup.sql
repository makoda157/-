-- 文字コードと照合順序（utf8mb4 + 日本語向け照合）
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- DB作成（存在しない場合のみ）
CREATE DATABASE IF NOT EXISTS `daily_report_system`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

-- アプリ用ユーザーを作成（既にあれば一旦DROPして再作成でも可）
CREATE USER IF NOT EXISTS 'repuser'@'%' IDENTIFIED BY 'reppass';
GRANT ALL PRIVILEGES ON `daily_report_system`.* TO 'repuser'@'%';
FLUSH PRIVILEGES;

