#!/bin/bash

if [ "$BRANCH" == "master" ]; then

  ./gradlew dockerBuildImage

  gpg --decrypt --batch --passphrase "$GMAIL_GPG" deploy/aws.credentials.gpg \
    | sed -E 's/^\[/#\[/g' \
    | sed -E 's/^(.*)\s=\s/\1=/g' \
    | source /dev/stdin

  REGION="us-east-1"
  echo "aws_access_key_id sha256sum = $(echo $aws_access_key_id | sha256sum)"

  docker --version  # document the version travis is using
  if [[ "$(whereis aws)" = *":" ]]; then
    pip install --user awscli # install aws cli w/o sudo
  fi
  export PATH="$PATH:$HOME/.local/bin" # put aws in the path
  eval $(aws ecr get-login --no-include-email --region us-east-1) #needs AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY envvars
  FULL_NAME="$PROJECT_NAME:$BUILD_VERSION"
  docker tag "$FULL_NAME" "788626906849.dkr.ecr.us-east-1.amazonaws.com/$FULL_NAME"
  docker push "788626906849.dkr.ecr.us-east-1.amazonaws.com/$PROJECT_NAME"

else 
  echo "Not master branch, skipping AWS deploy"
fi

