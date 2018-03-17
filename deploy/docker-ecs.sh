#!/bin/bash

exit 0

#788626906849.dkr.ecr.us-east-1.amazonaws.com/apis-api
if [ "$BRANCH" == "master" ]; then

  gpg --decrypt --batch --passphrase "$GMAIL_GPG" deploy/aws.credentials.gpg \
    | sed -E 's/^\[/#\[/g' \
    | sed -E 's/^(.*)\s=\s/\U\1=/g' \
    | source /dev/stdin

  docker --version  # document the version travis is using
  pip install --user awscli # install aws cli w/o sudo
  export PATH=$PATH:$HOME/.local/bin # put aws in the path
  eval $(aws ecr get-login --region us-east-1) #needs AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY envvars
  FULL_NAME=$PROJECT_NAME:$BUILD_VERSION
  docker build -t $PROJECT_NAME .
  docker tag $FULL_NAME 788626906849.dkr.ecr.us-east-1.amazonaws.com/$FULL_NAME
  docker push 788626906849.dkr.ecr.us-east-1.amazonaws.com/$FULL_NAME

else 
  echo "Not master branch, skipping AWS deploy"
fi

