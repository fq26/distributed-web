#!/bin/bash

###################### TA HAVE TO EDIT THIS ##################
# number of instances to launch
N=5

# resiliency to maintain
F=2

# S3 bucket name to bring war file and other stuffs in
S3_BUCKET="edu-cornell-cs-cs5300s16-gk256"

# keypair to ssh instance, important for reboot process
KEYPAIR=proj1bfinal
##############################################################

# deployable war file
WAR_FILE="proj1b.war"

# simpleDB domain name to hold ipAddress-svrID pairs
IPID_DOMAIN="SERVERIDS"
CONFIG_DOMAIN="RESILIENCY"

# aws config params
INSTALL_FILE="install.sh"
INSTANCE_TYPE="ami-08111162"

# aws credentials should be in ~/.aws/credentials
# enable simpleDB and us-east to work with configured image-id
aws configure set default.region us-east-1
aws configure set preview.sdb true

# upload war file to simpleDB
echo ">>>>>> Uploading war file to AWS S3"
aws s3 cp $WAR_FILE s3://${S3_BUCKET}/$WAR_FILE

# reset simpleDB
echo ">>>>>> Cleaning IPID simpleDB domain"
aws sdb delete-domain --domain-name $IPID_DOMAIN
aws sdb create-domain --domain-name $IPID_DOMAIN

echo ">>>>>> Cleaning CONFIG simpleDB domain"
aws sdb delete-domain --domain-name $CONFIG_DOMAIN
aws sdb create-domain --domain-name $CONFIG_DOMAIN

# put config variables to simpleDB
echo ">>>>>> Writing config variables to simpleDB"
aws sdb put-attributes --domain-name $CONFIG_DOMAIN --item-name N \
    --attributes Name=N,Value=$N,Replace=true
aws sdb put-attributes --domain-name $CONFIG_DOMAIN --item-name F \
    --attributes Name=F,Value=$F,Replace=true

# launch N instances
echo ">>>>>> Lunching N instances of EC2"
aws ec2 run-instances --image-id $INSTANCE_TYPE --count $N --instance-type t2.micro --user-data file://$INSTALL_FILE --key-name $KEYPAIR
