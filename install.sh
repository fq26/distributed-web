#!/bin/bash

# deployable war file
WAR_FILE="hello.war"

# S3 bucket name to bring war file and other stuffs in
S3_BUCKET="edu-cornell-cs-cs5300s16-gk256"

# simpleDB domain name to hold ipAddress-svrID pairs
DB_DOMAIN="LSI"

# number of instances to launch
N_INSTANCE=1

# file path to store tmp files and instance file
HOME_PATH="/home/ec2-user/"

# file name to store ipAddress-svrID pairs in file system
INSTANCE_FILE=instances.txt

# AWS credentials to connect with aws cli
# It's actually dangerous to store credentials in Github
# TODO: generate new keys and think of another way to store
AWS_KEY="AKIAI4FQXGCVF2BFTXYQ"
AWS_SECRET="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"

# print each cmd executed
set -ex

##### IGNORE BELOW, Tomcat8 supports > Java7
# install java8 to be compatible with tomcat8
# assume java7 is preconfigured in EC2
# JAVA_VER=$(java -version 2>&1 | sed 's/.*version "\(.*\)\.\(.*\)\..*"/\1\2/; 1q')
# if [[ "$JAVA_VER" -eq "18" ]]
# then
#     echo "Java8 is already pre-installed"
# elif [[ "$JAVA_VER" -eq "17" ]]
# then
#     echo "Installing Java8 then removing Java7"
#     sudo yum -y install java-1.8.0
#     sudo yum -y remove java-1.7.0-openjdk
# else
#     echo "Installing Java8"
#     sudo yum -y install java-1.8.0
# fi

# install tomcat8
sudo yum -y install tomcat8-webapps tomcat8-docs-webapp tomcat8-admin-webapps

# aws config credentials, enable simpleDB
aws configure set aws_access_key_id $AWS_KEY
aws configure set aws_secret_access_key $AWS_SECRET
aws configure set default.region us-east-1
aws configure set preview.sdb true

# install app code
S3_URL="$S3_BUCKET/$WAR_FILE"
sudo aws s3 cp s3://$S3_URL $HOME_PATH
WAR_URL="$HOME_PATH$WAR_FILE"
sudo cp $WAR_URL /usr/share/tomcat8/webapps

# determine internal IP of this instance, save it to file
wget http://169.254.169.254/latest/meta-data/local-ipv4 -P $HOME_PATH

# assign serverID for this instance, save it to file
wget http://169.254.169.254/latest/meta-data/ami-launch-index -P $HOME_PATH

######## create-domain should be done by launch-script ###########
# aws sdb delete-domain --domain-name $DB_DOMAIN
# aws sdb create-domain --domain-name $DB_DOMAIN

# save ipAddr:svrID pairs to simpleDB
IP_ADDR=local-ipv4
IP_ADDR="$HOME_PATH$IP_ADDR"
AMI_IDX=ami-launch-index
AMI_IDX="$HOME_PATH$AMI_IDX"
aws sdb put-attributes --domain-name $DB_DOMAIN --item-name `cat $IP_ADDR` \
    --attributes Name=`cat $IP_ADDR`,Value=`cat $AMI_IDX`,Replace=true

# wait for all instances to write
CURR=0
while [ $CURR -lt $N_INSTANCE ]
do
    CURR="$(aws sdb select --select-expression "SELECT COUNT(*) FROM $DB_DOMAIN" --output text | grep -o '[0-9]*')"

    echo "Waiting for other instances"
    sleep 5
done

# write all ipAddress from simpleDB to file
INSTANCE_FILE="$HOME_PATH$INSTANCE_FILE"
aws sdb select --select-expression "SELECT * FROM $DB_DOMAIN" --output text | grep -v "ITEMS" > $INSTANCE_FILE
sed -i 's/ATTRIBUTES//g; s/^[ \t]*//' $INSTANCE_FILE

# start tomcat
sudo service tomcat8 start
