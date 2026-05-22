#!/usr/bin/env sh
set -eu

MESSAGE="${1:?usage: telegram-alert.sh <message>}"

if [ -z "${TELEGRAM_BOT_TOKEN:-}" ] || [ -z "${TELEGRAM_CHAT_ID:-}" ]; then
  echo "Telegram alert not sent because TELEGRAM_BOT_TOKEN or TELEGRAM_CHAT_ID is unset" >&2
  exit 0
fi

curl -fsS \
  -X POST "https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/sendMessage" \
  -d "chat_id=${TELEGRAM_CHAT_ID}" \
  --data-urlencode "text=${MESSAGE}" >/dev/null
