FROM openjdk:8-jre-alpine

RUN apk update && apk add bash && rm -rf /var/lib/apk/* /var/cache/apk/*

VOLUME /etc/ma1sd
VOLUME /var/ma1sd
EXPOSE 8090

ENV JAVA_OPTS=""
ENV CONF_FILE_PATH="/etc/ma1sd/ma1sd.yaml"
ENV SIGN_KEY_PATH="/var/ma1sd/sign.key"
ENV SQLITE_DATABASE_PATH="/var/ma1sd/ma1sd.db"

CMD [ "/start.sh" ]

ADD src/docker/start.sh /start.sh
ADD src/script/ma1sd /app/ma1sd
ADD build/libs/ma1sd.jar /app/ma1sd.jar
