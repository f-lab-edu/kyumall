docker rm kyumall-test-db
docker run --name kyumall-test-db -e MYSQL_ROOT_PASSWORD=12 -e MYSQL_DATABASE=kyumall -d -p 3346:3306 mysql
