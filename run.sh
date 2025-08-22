#!/bin/bash

if [ -z "$1" ]; then
  echo "Uso: $0 [client|bootstrap]"
  exit 1
fi

if [[ "$1" != "client" && "$1" != "bootstrap" ]]; then
  echo "Erro: argumento inválido. Use 'client' ou 'bootstrap'."
  exit 1
fi

CLASSPATH="app/build/classes/java/main:bcprov-jdk18on-1.80.jar"

MAIN_CLASS="ssd.proj.App"

java -cp "$CLASSPATH" $MAIN_CLASS "$1"
