#!/bin/bash
set -e

cd "$(dirname "$0")"

./start-backend.sh
./start-frontend.sh

echo ""
echo "All services started."
echo "  Frontend: http://localhost:3000"
echo "  Backend:  http://localhost:8080"
