SHOW DATABASES;

USE investment_aggregator_springboot_db;

SHOW TABLES;

SELECT * FROM tb_accounts;

SELECT 
  BIN_TO_UUID(account_id) AS account_id,
  description,
  BIN_TO_UUID(user_id) AS user_id
FROM tb_accounts;

SELECT * FROM tb_billingaddress;

SELECT
    BIN_TO_UUID(account_id) AS account_id,
    number,
    street
FROM tb_billingaddress;

SELECT * FROM tb_users;

SELECT
    BIN_TO_UUID(user_id) as user_id,
    creation_timestamp,
    email,
    password,
    update_timestamp,
    username
FROM tb_users;

SELECT * FROM tb_stocks;