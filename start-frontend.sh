#!/bin/bash
set -e

cd "$(dirname "$0")"

if [ -f frontend.pid ]; then
  PID=$(cat frontend.pid)
  if kill -0 "$PID" 2>/dev/null; then
    echo "Frontend is already running (PID: $PID)"
    exit 0
  fi
fi

echo "Starting frontend on port 3000..."
cd frontend
nohup npx next dev > ../frontend.log 2>&1 &
echo $! > ../frontend.pid

cd ..
sleep 3
if kill -0 $(cat frontend.pid) 2>/dev/null; then
  echo "Frontend started successfully (PID: $(cat frontend.pid))"
else
  echo "Frontend failed to start. Check frontend.log for details."
  rm -f frontend.pid
  exit 1
fi
