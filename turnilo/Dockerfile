FROM node:lts-alpine

ENV DRUID_HOST localhost:8082

# install gettext for envsubst
#RUN apk update
#RUN apk add gettext

RUN npm install -g turnilo@1.23.2

EXPOSE 9090

CMD turnilo --druid $DRUID_HOST
