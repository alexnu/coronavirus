#!/bin/sh

if [ -z "$PASSPHRASE" ]; then
  echo "Error: Missing variable PASSPHRASE"
  exit 1
fi

gpg -d --batch --passphrase $PASSPHRASE /etc/nginx/certs/fullchain.pem.gpg >  /etc/nginx/certs/fullchain.pem \
  && gpg -d --batch --passphrase $PASSPHRASE /etc/nginx/certs/privkey.pem.gpg > /etc/nginx/certs/privkey.pem \
  && ./docker-entrypoint.sh "$@"
