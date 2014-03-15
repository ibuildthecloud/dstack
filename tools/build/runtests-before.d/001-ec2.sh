#!/bin/bash
set -e

cd $(dirname $0)

INSTANCE_ID=../before.d/ec2-id
KEY=../before.d/key

get_dns()
{
    aws ec2 describe-instances --instance-ids $(<$INSTANCE_ID) \
                               --query 'Reservations[0].Instances[0].PublicDnsName' \
                               --output text
}

if [ "$EC2_TEST" != "true" ] || [ ! -e $INSTANCE_ID ]; then
    exit 0
fi

while [ "$(get_dns)" = "" ]; do
    echo Waiting for EC2
    sleep 1
done

DNS=$(get_dns)

trap '[ -e authorized_keys_tmp ] && rm authorized_keys_tmp' EXIT
curl -s http://localhost:8080/v1/authorized_keys > authorized_keys_tmp
mv authorized_keys_tmp authorized_keys

cat authorized_keys | ssh -o UserKnownHostsFile=/dev/null \
                          -o StrictHostKeyChecking=no     \
                          -i $KEY                         \
                          -l root                         \
                          $DNS --                         \
                          tee -a /root/.ssh/authorized_keys

curl -X POST http://localhost:8080/v1/agents -F uri="ssh://root@${DNS}:22"
