FROM node:lts-alpine

RUN npm install -g turnilo@1.23.2
COPY config.yaml .
EXPOSE 9090

CMD turnilo --config config.yaml
