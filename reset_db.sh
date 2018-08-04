#!/usr/bin/env bash
docker-compose stop db
sudo rm -rf .data
docker-compose up -d db