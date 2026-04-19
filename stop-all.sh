#!/bin/bash

cd "$(dirname "$0")"

./stop-frontend.sh
./stop-backend.sh

echo ""
echo "All services stopped."
