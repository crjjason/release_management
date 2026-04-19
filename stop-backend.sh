#!/bin/bash

cd "$(dirname "$0")"

if [ -f backend.pid ]; then
  PID=$(cat backend.pid)
  if kill -0 "$PID" 2>/dev/null; then
    echo "Stopping backend (PID: $PID)..."
    kill "$PID"
    sleep 1
  fi
  rm -f backend.pid
fi

# Fallback: kill any process on port 8080
PORT_PID=$(lsof -ti:8080 2>/dev/null)
if [ -n "$PORT_PID" ]; then
  echo "Cleaning up remaining backend process on port 8080..."
  kill $PORT_PID 2>/dev/null
  sleep 1
fi

echo "Backend stopped."
