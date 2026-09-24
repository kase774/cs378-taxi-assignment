#!/usr/bin/env bash
#
# Run a single service in its own JVM.
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
# Properties are passed to the app as system properties (key=value -> -Dkey=value).
# See Parameters.java:
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
#   HEAP=4g     max heap (-Xmx) for the service JVM; default 4g.
#               This is only a ceiling; the JVM grows the heap as needed.
#               Lower it (e.g. HEAP=1g) on a memory constrained VM.
#   BUILD=1     force a rebuild of the jar before running.
#               A rebuild also happens automatically if the jar is missing.

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

JAVA_PROPS=()
for arg in "$@"; do
    if [[ "$arg" == -D* ]]; then
        JAVA_PROPS+=("$arg")
    else
        JAVA_PROPS+=("-D${arg}")
    fi
done

# ---- build the shaded jar if needed -------------------------------------
find_jar() {
    ls target/*-jar-with-dependencies.jar 2>/dev/null | head -n1 || true
}

JAR="$(find_jar)"
if [[ "${BUILD:-0}" == "1" || -z "$JAR" || ! -f "$JAR" ]]; then
    if ! command -v mvn >/dev/null 2>&1; then
        echo "!! no runnable jar and maven (mvn) not found" >&2
        echo "   build it elsewhere with: mvn -DskipTests compile assembly:single" >&2
        exit 1
    fi
    echo ">> building: mvn -q -DskipTests compile assembly:single"
    mvn -q -DskipTests compile assembly:single
    JAR="$(find_jar)"
fi

if [[ -z "$JAR" || ! -f "$JAR" ]]; then
    echo "!! no runnable jar found in target/" >&2
    exit 1
fi

echo ">> java -Xmx${HEAP} ${JAVA_PROPS[*]} -cp $JAR $MAIN_CLASS"
exec java "-Xmx${HEAP}" "${JAVA_PROPS[@]}" -cp "$JAR" "$MAIN_CLASS"
