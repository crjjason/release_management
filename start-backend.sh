#!/bin/bash
set -e

cd "$(dirname "$0")"

if [ -f backend.pid ]; then
  PID=$(cat backend.pid)
  if kill -0 "$PID" 2>/dev/null; then
    echo "Backend is already running (PID: $PID)"
    exit 0
  fi
fi

echo "Building backend..."
cd backend
/usr/local/bin/mvn package -DskipTests -q

echo "Starting backend on port 8080..."
cd ..
nohup java -jar backend/target/backend-0.0.1-SNAPSHOT.jar > backend.log 2>&1 &
echo $! > backend.pid

sleep 2
if kill -0 $(cat backend.pid) 2>/dev/null; then
  echo "Backend started successfully (PID: $(cat backend.pid))"
else
  echo "Backend failed to start. Check backend.log for details."
  rm -f backend.pid
  exit 1
fi
