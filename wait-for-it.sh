#!/usr/bin/env bash
# wait-for-it.sh

TIMEOUT=15
QUIET=0
WAITFORIT_HOST=
WAITFORIT_PORT=
WAITFORIT_COMMAND=
shift_args=false

usage()
{
    cat << USAGE >&2
Usage:
    $0 host:port [-t timeout] [-- command args]
    -h | --help             Show this message
    -q | --quiet            Don't output any status messages
    -t TIMEOUT | --timeout=TIMEOUT
                            Timeout in seconds, zero for no timeout
    -- COMMAND ARGS         Execute COMMAND ARGS after the service is available
USAGE
    exit 1
}

wait_for_it_echo()
{
    if [ "$QUIET" -ne 1 ]; then
        echo "$@"
    fi
}

while [ "$#" -gt 0 ]; do
    case "$1" in
        *:* )
            if ! $shift_args; then
                WAITFORIT_HOST=$(echo "$1" | cut -d: -f1)
                WAITFORIT_PORT=$(echo "$1" | cut -d: -f2)
            fi
            ;;
        -q | --quiet)
            QUIET=1
            ;;
        -t)
            TIMEOUT="$2"
            if [[ "$TIMEOUT" != "" ]]; then
                shift
            else
                wait_for_it_echo "Error: -t requires an argument."
                usage
            fi
            ;;
        --timeout=*)
            TIMEOUT="${1#*=}"
            ;;
        --)
            shift_args=true
            ;;
        *)
            if $shift_args; then
                WAITFORIT_COMMAND="$WAITFORIT_COMMAND $1"
            else
                wait_for_it_echo "Unknown argument: $1"
                usage
            fi
            ;;
    esac
    shift
done

if [ "$WAITFORIT_HOST" = "" ] || [ "$WAITFORIT_PORT" = "" ]; then
    wait_for_it_echo "Error: you need to provide a host and port in the format host:port"
    usage
fi

if command -v nc >/dev/null 2>&1; then
    CONNECT_TOOL="nc"
elif command -v bash >/dev/null 2>&1; then
    CONNECT_TOOL="bash"
else
    wait_for_it_echo "Error: neither nc nor bash found for connection check."
    exit 1
fi

wait_for_service()
{
    if [ "$CONNECT_TOOL" = "nc" ]; then
        nc -z "$WAITFORIT_HOST" "$WAITFORIT_PORT"
    elif [ "$CONNECT_TOOL" = "bash" ]; then
        (exec 3<>/dev/tcp/"$WAITFORIT_HOST"/"$WAITFORIT_PORT") 2>/dev/null
    fi
}

wait_for_it_echo "Waiting for $WAITFORIT_HOST:$WAITFORIT_PORT to be available..."

start_ts=$(date +%s)
while :
do
    if [ "$TIMEOUT" -gt 0 ] && [ $(( $(date +%s) - start_ts )) -gt "$TIMEOUT" ]; then
        wait_for_it_echo "Error: Timeout occurred after $TIMEOUT seconds waiting for $WAITFORIT_HOST:$WAITFORIT_PORT"
        exit 1
    fi

    if wait_for_service; then
        wait_for_it_echo "$WAITFORIT_HOST:$WAITFORIT_PORT is available after $(( $(date +%s) - start_ts )) seconds."
        break
    else
        sleep 1
    fi
done

if [ "$WAITFORIT_COMMAND" != "" ]; then
    wait_for_it_echo "Executing command: $WAITFORIT_COMMAND"
    exec $WAITFORIT_COMMAND
else
    exit 0
fi