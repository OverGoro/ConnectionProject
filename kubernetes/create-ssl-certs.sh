#!/bin/bash
mkdir -p nginx/ssl

# Генерация приватного ключа
openssl genrsa -out nginx/ssl/private.key 2048

# Генерация CSR (Certificate Signing Request)
openssl req -new -key nginx/ssl/private.key -out nginx/ssl/certificate.csr \
  -subj "/C=RU/ST=Moscow/L=Moscow/O=Company/CN=localhost"

# Генерация самоподписанного сертификата
openssl x509 -req -days 365 -in nginx/ssl/certificate.csr \
  -signkey nginx/ssl/private.key -out nginx/ssl/certificate.crt

# Установка прав
chmod 600 nginx/ssl/private.key
chmod 644 nginx/ssl/certificate.crt

echo "SSL сертификаты созданы в папке nginx/ssl/"