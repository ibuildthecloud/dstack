#!/bin/bash

cd $(dirname $0)

for i in before.d/*; do
    ./$i
done
