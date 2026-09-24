#!/usr/bin/env bash
#
# Launch the full Hours job locally:
#   2 map services (one per dataset part)
#   2 intermediate reduction services
#   1 final reduction service (runs in the foreground and prints its output)
#
# Topology:
#
#   HoursMapService (dataset=1) --> HourIntermediateReductionService #1 --\
#                                                                         >--> HourFinalReductionService
#   HoursMapService (dataset=2) --> HourIntermediateReductionService #2 --/
#
# Everything is configured through -D system properties (see Parameters.java):
#   server.port     port a server binds to
#   server.address  host:port a client connects to
#   dataset         1 = p1, 2 = p2, 3 = small
#
# The final reduction needs to be listening before the intermediates start,
# so a background "deployer" waits for it and then starts the other four.

set -euo pipefail

cd "$(dirname "$0")"

HOST="${HOST:-127.0.1.1}"
FINAL_PORT="${FINAL_PORT:-31001}"
INT1_PORT="${INT1_PORT:-31002}"
INT2_PORT="${INT2_PORT:-31003}"

MAIN_HOURS_MAP="edu.utexas.cs.cs378.hours.HoursMapService"
MAIN_INT="edu.utexas.cs.cs378.hours.HourIntermediateReductionService"
MAIN_FINAL="edu.utexas.cs.cs378.hours.HourFinalReductionService"

LOG_DIR="logs"
mkdir -p "$LOG_DIR"

# ---- build ---------------------------------------------------------------
if [[ "${SKIP_BUILD:-0}" != "1" ]]; then
    if ! command -v mvn >/dev/null 2>&1; then
        echo "!! maven (mvn) not found; install it or run with SKIP_BUILD=1" >&2
        exit 1
    fi
    echo ">> building: mvn -q -DskipTests clean compile assembly:single"
    mvn -q -DskipTests clean compile assembly:single
fi

JAR="$(ls target/*-jar-with-dependencies.jar 2>/dev/null | head -n1 || true)"
if [[ -z "$JAR" || ! -f "$JAR" ]]; then
    echo "!! no runnable jar found; run: mvn clean compile assembly:single" >&2
    exit 1
fi
echo ">> using $JAR"

# ---- process tracking / cleanup -----------------------------------------
CHILD_PIDS_FILE="$(mktemp)"
cleanup() {
    if [[ -f "$CHILD_PIDS_FILE" ]]; then
        while read -r pid; do
            [[ -n "$pid" ]] && kill "$pid" 2>/dev/null || true
        done < "$CHILD_PIDS_FILE"
        rm -f "$CHILD_PIDS_FILE"
    fi
    [[ -n "${DEPLOYER_PID:-}" ]] && kill "$DEPLOYER_PID" 2>/dev/null || true
}
trap cleanup EXIT INT TERM

# wait until a log file contains a needle (servers log "server started ...")
wait_for_log() {
    local file="$1" needle="$2" tries="${3:-300}"
    local i
    for ((i = 0; i < tries; i++)); do
        if [[ -f "$file" ]] && grep -q "$needle" "$file"; then
            return 0
        fi
        sleep 0.2
    done
    echo "!! timed out waiting for '$needle' in $file" >&2
    return 1
}

# ---- deployer: starts intermediates then maps once final is up ----------
(
    wait_for_log "$LOG_DIR/final.log" "server started"

    echo ">> starting intermediate reductions (-> final $HOST:$FINAL_PORT)"
    for port in "$INT1_PORT" "$INT2_PORT"; do
        name="intermediate-$port"
        java -Dserver.port="$port" -Dserver.address="$HOST:$FINAL_PORT" \
            -cp "$JAR" "$MAIN_INT" >"$LOG_DIR/$name.log" 2>&1 &
        echo $! >>"$CHILD_PIDS_FILE"
        wait_for_log "$LOG_DIR/$name.log" "server started"
        echo ">>   $name up on port $port"
    done

    echo ">> starting map services (p1 -> $INT1_PORT, p2 -> $INT2_PORT)"
    java -Ddataset=1 -Dserver.address="$HOST:$INT1_PORT" \
        -cp "$JAR" "$MAIN_HOURS_MAP" >"$LOG_DIR/map1.log" 2>&1 &
    echo $! >>"$CHILD_PIDS_FILE"

    java -Ddataset=2 -Dserver.address="$HOST:$INT2_PORT" \
        -cp "$JAR" "$MAIN_HOURS_MAP" >"$LOG_DIR/map2.log" 2>&1 &
    echo $! >>"$CHILD_PIDS_FILE"

    echo ">> all services launched; see $LOG_DIR/{map1,map2,intermediate-*}.log"
) &
DEPLOYER_PID=$!

# ---- final reduction (foreground, output shown) -------------------------
echo ">> starting final reduction on port $FINAL_PORT (foreground)"
java -Dserver.port="$FINAL_PORT" -cp "$JAR" "$MAIN_FINAL" 2>&1 | tee "$LOG_DIR/final.log"
