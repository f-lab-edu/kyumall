docker rm kyumall-local-db
docker run --name kyumall-local-db -e MYSQL_ROOT_PASSWORD=12 -e MYSQL_DATABASE=kyumall -d -p 3336:3306 mysql
