#!/bin/bash

cd $(dirname $0)

if [ "$EC2_TEST" != "true" ]; then
    exit 0
fi

if [ ! -x "$(which aws)" ]; then
    sudo pip install awscli
fi

if [ ! -e key.pub ]; then
    ssh-keygen -t rsa -f key -N ''
fi

cat > user-data << EOF
#!/bin/bash

terminate()
{
    sleep 1800
    poweroff -f
}

setup()
{
    mkdir -p /root/.ssh
    echo '$(<key.pub)' > /root/.ssh/authorized_keys
    curl -sL https://get.docker.io/ | sh
    apt-get install -y python-eventlet python-pip
    pip install --upgrade pip tox
    hash -r
    mkdir -p /mnt/dstack
    ln -s /mnt/dstack /var/lib

    mkdir -p /var/lib/dstack/etc/dstack/agent
    cat > /var/lib/dstack/etc/dstack/agent/agent.conf << EOF2
export DSTACK_AGENT_TESTS=true
EOF2
}

terminate &
setup 2>&1 | tee /var/log/setup.log
EOF

ID=$(aws ec2 run-instances --image-id ami-d8ac909d                      \
                      --security-group-ids all                          \
                      --user-data file://user-data                      \
                      --instance-initiated-shutdown-behavior terminate  \
                      --query 'Instances[0].InstanceId'                 \
                      --output text)

echo $ID > ec2-id
