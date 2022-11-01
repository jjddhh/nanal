# !/bin/bash

# docker가 없다면, docker 설치
if ! type docker > /dev/null
then
  echo "docker does not exist"
  echo "Start installing docker"
  sudo yum -y upgrade
  sudo yum -y install docker
fi

# docker-compose가 없다면 docker-compose 설치
if ! type docker-compose > /dev/null
then
  echo "docker-compose does not exist"
  echo "Start installing docker-compose"
  sudo curl -L "https://github.com/docker/compose/releases/download/1.27.3/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
  sudo chmod +x /usr/local/bin/docker-compose
  sudo ln -s /usr/local/bin/docker-compose /usr/bin/docker-compose
fi

echo "create build file"
cd /home/ec2-user/nanal-test
sudo chmod +x ./gradlew
./gradlew build

echo "start docker"
sudo service docker start

echo "stop containers and delete containers and images"
docker stop $(docker ps -a -q)
docker rm $(docker ps -a -q)
docker rmi $(docker images -q)

echo "start docker-compose up: ubuntu"
sudo docker-compose -f /home/ec2-user/nanal/docker-compose.yml up --build -d