#!/usr/bin/env bash
#
# Run a service
#
# Usage:
#   ./run.sh <service> [property ...]
#
# <service> is one of these short names (a fully-qualified class also works):
#   hours-map      edu.utexas.cs.cs378.hours.HoursMapService
#   hours-int      edu.utexas.cs.cs378.hours.HourIntermediateReductionService
#   hours-final    edu.utexas.cs.cs378.hours.HourFinalReductionService
#   drivers-map    edu.utexas.cs.cs378.drivers.DriversMapService
#   drivers-int    edu.utexas.cs.cs378.drivers.DriverIntermediateReductionService
#   drivers-final  edu.utexas.cs.cs378.drivers.DriverFinalReductionService
#
# Properties are passed straight to the app as system properties via Maven
# (key=value becomes -Dkey=value). See Parameters.java:
#   server.port      port this service binds to
#   server.address   host:port this service connects to
#   dataset          1 = p1, 2 = p2, 3 = small
#   clients          number of intermediates the final reduction expects
#   connect.*        connect retry settings
#
# Examples:
#   ./run.sh hours-map dataset=1 server.address=127.0.1.1:31002
#   ./run.sh hours-int server.port=31002 server.address=127.0.1.1:31001
#   ./run.sh hours-final server.port=31001 clients=1
#
# Env:
#   HEAP=4g     heap size for the JVM (sets MAVEN_OPTS -Xms/-Xmx; default 4g)
#   COMPILE=0   skip the "compile" phase and just run exec:java

set -euo pipefail

cd "$(dirname "$0")"

HEAP="${HEAP:-4g}"

if [[ $# -lt 1 ]]; then
    echo "usage: $0 <service> [key=value ...]" >&2
    exit 1
fi

SERVICE="$1"
shift

case "$SERVICE" in
    hours-map)      MAIN_CLASS="edu.utexas.cs.cs378.hours.HoursMapService" ;;
    hours-int)      MAIN_CLASS="edu.utexas.cs.cs378.hours.HourIntermediateReductionService" ;;
    hours-final)    MAIN_CLASS="edu.utexas.cs.cs378.hours.HourFinalReductionService" ;;
    drivers-map)    MAIN_CLASS="edu.utexas.cs.cs378.drivers.DriversMapService" ;;
    drivers-int)    MAIN_CLASS="edu.utexas.cs.cs378.drivers.DriverIntermediateReductionService" ;;
    drivers-final)  MAIN_CLASS="edu.utexas.cs.cs378.drivers.DriverFinalReductionService" ;;
    *.*)            MAIN_CLASS="$SERVICE" ;;
    *)              echo "!! unknown service: $SERVICE" >&2; exit 1 ;;
esac

MAVEN_ARGS=()
for arg in "$@"; do
    if [[ "$arg" == -D* ]]; then
        MAVEN_ARGS+=("$arg")
    else
        MAVEN_ARGS+=("-D${arg}")
    fi
done

if ! command -v mvn >/dev/null 2>&1; then
    echo "!! maven (mvn) not found; install it to run this script" >&2
    exit 1
fi

# exec:java runs in the Maven JVM, so the heap is set through MAVEN_OPTS
export MAVEN_OPTS="-Xms${HEAP} -Xmx${HEAP}${MAVEN_OPTS:+ ${MAVEN_OPTS}}"

GOALS=()
if [[ "${COMPILE:-1}" == "1" ]]; then
    GOALS+=(compile)
fi
GOALS+=(exec:java)

echo ">> mvn ${GOALS[*]} -Dexec.mainClass=$MAIN_CLASS ${MAVEN_ARGS[*]}"
exec mvn -q "${GOALS[@]}" -Dexec.mainClass="$MAIN_CLASS" "${MAVEN_ARGS[@]}"
