#!/bin/bash
set -e

trap cleanup EXIT

cleanup()
{
    if [ -e $TEMP ]; then
        rm -rf $TEMP
    fi
    if [ -e $OLD ]; then
        rm -rf $OLD
    fi
}

source ${DSTACK_HOME:-/var/lib/dstack}/common/scripts.sh

DEST=$DSTACK_HOME/pyagent
TOX_INI=$DEST/tox.ini
MAIN=$DEST/main.py
RESTART=$DEST/reboot
OLD=$(mktemp -d ${DEST}.XXXXXXXX)
TEMP=$(mktemp -d ${DEST}.XXXXXXXX)

cd $(dirname $0)

stage()
{
    if [ "$DSTACK_AGENT_TESTS" = "true" ]; then
        cp -rf * $TEMP
    else
        cp -rf apply.sh dstack dist main.py $TEMP
    fi

    if [ -e $DEST ]; then
        mv $DEST ${OLD}
    fi
    mv $TEMP ${DEST}
    rm -rf ${OLD}
}

conf()
{
    CONF=(/etc/dstack/agent/agent.conf
          /var/lib/dstack/etc/dstack/agent/agent.conf)

    for conf_file in "${CONF[@]}"; do
        if [ -e $conf_file ]
        then
            source $conf_file
        fi
    done
}

run_tests()
{
    if [ "$DSTACK_AGENT_TESTS" != "true" ]; then
        return 0
    fi

    tox -c $TOX_INI
}

start()
{
    if [ -n "$NO_START" ]; then
        return 0
    fi
    echo $RANDOM > $RESTART
    chmod +x $MAIN
    if [ "$DSTACK_PYPY" = "true" ] && which pypy >/dev/null; then
        MAIN="pypy $MAIN"
    fi
    if [ "$DAEMON" = false ]; then
        info Executing $MAIN
        cleanup
        exec $MAIN
    else
        info Backgrounding $MAIN
        $MAIN &
    fi
}

while [ "$#" -gt 0 ]; do
    case $1 in
    --no-start)
        NO_START=true
        ;;
    --no-daemon)
        DAEMON=false
        ;;
    start)
        start
        exit 0
        ;;
    esac
    shift 1
done

conf
stage
run_tests
start
