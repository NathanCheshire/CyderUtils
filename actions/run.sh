#!/bin/bash

set -e
python3 -m venv venv
source venv/bin/activate
pip install --upgrade pip
pip install -r ./actions/requirements.txt
python3 ./actions/orchestrator.py
