FROM ibuildthecloud/ubuntu-core-base:13.10
RUN apt-get update && \
     apt-get install -y python curl python-eventlet pypy
ADD nsenter /usr/sbin/nsenter
