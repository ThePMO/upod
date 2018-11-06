#!/bin/bash

set -e

LICENSE_HTML="app/assets/licenses.html"
LINE="=========================================================================="

TEMP=$(mktemp)
function cleanup {
  rm -rf "${TEMP}"
}
trap cleanup EXIT

(
cat << EOF
<html>
<body>
<pre>
EOF

for file in third-party-licenses/*; do
  echo "${LINE}"
  if [[ "${file}" =~ .*\.sh$ ]]; then
    source ${file}
    echo "${ATTRIBUTION}"
    echo
    cat "${LICENSE_FILE}"
    unset ATTRIBUTION
    unset LICENSE_FILE
  else
    cat "${file}"
  fi
  echo
  echo "${LINE}"
done

cat << EOF
</pre>
</body>
</html>
EOF
) > ${TEMP}

RET=0
if [[ "${1}" == "check" ]]; then
  cmp "${LICENSE_HTML}" "${TEMP}" || RET=1
else
  mv "${TEMP}" "${LICENSE_HTML}"
fi

rm -f "${TEMP}"

exit ${RET}