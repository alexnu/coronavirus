#!/bin/sh

if [ -z "$PASSPHRASE" ]; then
  echo "Error: Missing variable PASSPHRASE"
  exit 1
fi

gpg -d --batch --passphrase $PASSPHRASE /etc/nginx/certs/coronavirus.fullchain.pem.gpg >  /etc/nginx/certs/coronavirus.fullchain.pem \
  && gpg -d --batch --passphrase $PASSPHRASE /etc/nginx/certs/coronavirus.privkey.pem.gpg > /etc/nginx/certs/coronavirus.privkey.pem \
  && gpg -d --batch --passphrase $PASSPHRASE /etc/nginx/certs/buyormine.fullchain.pem.gpg >  /etc/nginx/certs/buyormine.fullchain.pem \
  && gpg -d --batch --passphrase $PASSPHRASE /etc/nginx/certs/buyormine.privkey.pem.gpg > /etc/nginx/certs/buyormine.privkey.pem \
  && ./docker-entrypoint.sh "$@"
