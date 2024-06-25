#!/bin/bash

set -e
python3 -m venv venv
source venv/bin/activate
pip install --upgrade pip
pip install -r ./actions/requirements.txt

if [[ "$1" == "--fix-javadoc" ]]; then
    python3 ./actions/orchestrator.py --fix-javadoc
else
    python3 ./actions/orchestrator.py
fi