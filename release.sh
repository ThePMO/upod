#!/bin/bash

UPOD_RELEASE_KEYSTORE_LOCATION=${1}
UPOD_RELEASE_KEYSTORE_KEY_ALIAS=${2}
GRADLE_BIN=${3}

test ${#} -lt 2 && { echo "usage: ${0} keystore alias [gradle-executable]"; exit 1; }

if [[ -z "${GRADLE_BIN}" ]]; then
  echo "No gradle executable defined, using system's default. Remember that newer gradle versions will not work"
  sleep 5
fi

./generate-licenses.sh check || { echo "The generated licenses file does not seem up to date"; exit 1; }

read -s -p 'Keystore Password: ' UPOD_RELEASE_KEYSTORE_PASSWORD
echo
read -s -p 'Key Password: ' UPOD_RELEASE_KEYSTORE_KEY_PASSWORD
echo

export UPOD_RELEASE_KEYSTORE_LOCATION UPOD_RELEASE_KEYSTORE_KEY_ALIAS UPOD_RELEASE_KEYSTORE_PASSWORD UPOD_RELEASE_KEYSTORE_KEY_PASSWORD

${GRADLE_BIN} release
