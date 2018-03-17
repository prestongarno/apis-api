#!/bin/bash

if [ "$BRANCH" == "master" ]; then

  ./gradlew dockerBuildImage

  gpg --decrypt --batch --passphrase "$GMAIL_GPG" deploy/aws.credentials.gpg \
    | sed -E 's/^\[/#\[/g' \
    | sed -E 's/^(.*)\s=\s/\1=/g' \
    | source /dev/stdin

  REGION="us-east-1"
  echo "aws_access_key_id sha256sum = $(echo $aws_access_key_id | sha256sum)"

  docker --version
  if [[ "$(whereis aws)" = *":" ]]; then pip install --user awscli; fi
  export PATH="$PATH:$HOME/.local/bin"
  eval $(aws ecr get-login --no-include-email --region us-east-1)
  FULL_NAME="apis-api:1.0-SNAPSHOT"
  echo "FULL_NAME=$FULL_NAME"
  docker tag $FULL_NAME 788626906849.dkr.ecr.us-east-1.amazonaws.com/$FULL_NAME
  docker images
  docker push 788626906849.dkr.ecr.us-east-1.amazonaws.com/$FULL_NAME

else 
  echo "Not master branch, skipping AWS deploy"
fi

