#!/bin/bash

cd "$(dirname "$0")"

if [ -f frontend.pid ]; then
  PID=$(cat frontend.pid)
  if kill -0 "$PID" 2>/dev/null; then
    echo "Stopping frontend (PID: $PID)..."
    kill "$PID"
    sleep 1
  fi
  rm -f frontend.pid
fi

# Fallback: kill any process on port 3000
PORT_PID=$(lsof -ti:3000 2>/dev/null)
if [ -n "$PORT_PID" ]; then
  echo "Cleaning up remaining frontend process on port 3000..."
  kill $PORT_PID 2>/dev/null
  sleep 1
fi

echo "Frontend stopped."
