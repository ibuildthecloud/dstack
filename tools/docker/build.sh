#!/bin/bash

cd $(dirname $0)
if ! docker images | grep -q ^dstack-buildenv; then
    ./build-env.sh
fi
cd ../..
mkdir -p dist/artifacts
if [ "$1" == "enter" ]; then
	shift
	docker run -rm -e MAVEN_ARGS=$MAVEN_ARGS -v $(pwd):/root -t -i "$@" dstack-buildenv bash
elif [ "$1" == "test" ]; then
	docker run -rm -e MAVEN_ARGS=$MAVEN_ARGS -e BUILD_USER_ID=$(id -u) -v $(pwd):/root -t -i dstack-buildenv /root/tools/build/runtests.sh
else
	docker run -rm -e MAVEN_ARGS=$MAVEN_ARGS -e BUILD_USER_ID=$(id -u) -v $(pwd):/root -t -i dstack-buildenv /root/dstack.sh build
fi
