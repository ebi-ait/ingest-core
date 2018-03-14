# Spreadsheet Submission

## Postman
These commands are also available in a [Postman Collection](https://www.getpostman.com/collections/92b868f180560438a9ec).

## Request Authentication Token
```
curl -X POST \
  https://danielvaughan.eu.auth0.com/oauth/token \
  -H 'content-type: application/json' \
  -d '{"client_id":"Zdsog4nDAnhQ99yiKwMQWAPc2qUDlR99","client_secret":"t-OAE-GQk_nZZtWn-QQezJxDsLXmU7VSzlAh9cKW5vb87i90qlXGTvVNAjfT9weF","audience":"http://localhost:8080","grant_type":"client_credentials"}'
```
* Authentication token (`$token`) is in the access `access_token` field

## Uploading a spreadsheet
```
curl -X POST \
  http://ingest.dev.data.humancellatlas.org/api_upload \
  -H 'Authorization: Bearer $token' \
  -H 'content-type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW' \
  -F file=@spreadsheet/v5_spleen_plainHeaders.xlsx
```  

