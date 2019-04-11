Request a token
```
curl --request POST \
  --url https://danielvaughan.eu.auth0.com/oauth/token  \
  --header 'content-type: application/json' \
  --data '{"client_id":"Zdsog4nDAnhQ99yiKwMQWAPc2qUDlR99","client_secret":"t-OAE-GQk_nZZtWn-QQezJxDsLXmU7VSzlAh9cKW5vb87i90qlXGTvVNAjfT9weF","audience":"http://localhost:8080","grant_type":"client_credentials"}'
```

This returns an access token to use with requests to the API e.g.
```json
{
  "access_token":"eyJ0eXAiOiJKV...",
  "expires_in":86400,"token_type":"Bearer"
}
```

Create submission

```
curl --request POST \
    --url http://localhost:8080/submissionEnvelopes \
    --header 'content-type: application/json' \
    --header 'authorization: Bearer eyJ0eXAiOiJKV...' \
    --data {}
```
    