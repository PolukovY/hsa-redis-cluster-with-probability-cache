# redis-cluster 

## Docker Compose template of Redis cluster

The template defines the topology of the Redis cluster

```
master:
  image: redis:3
slave:
  image: redis:3
  command: redis-server --slaveof redis-master 6379
  links:
    - master:redis-master
sentinel:
  build: sentinel
  environment:
    - SENTINEL_DOWN_AFTER=5000
    - SENTINEL_FAILOVER=5000    
  links:
    - master:redis-master
    - slave
```

There are following services in the cluster,

* master: Redis master
* slave:  Redis slave
* sentinel: Redis sentinel


The sentinels are configured with a "mymaster" instance with the following properties -

```
sentinel monitor mymaster redis-master 6379 2
sentinel down-after-milliseconds mymaster 5000
sentinel parallel-syncs mymaster 1
sentinel failover-timeout mymaster 5000
```

The details could be found in sentinel/sentinel.conf

The default values of the environment variables for Sentinel are as following

* SENTINEL_QUORUM: 2
* SENTINEL_DOWN_AFTER: 30000
* SENTINEL_FAILOVER: 180000



## Play with it

Build the sentinel Docker image

```
docker-compose build
```

Start the redis cluster

```
docker-compose up -d
```

Check the status of redis cluster

```
docker-compose ps
```

The result is 

```
         Name                        Command               State          Ports        
--------------------------------------------------------------------------------------
redis_master_1     docker-entrypoint.sh redis ...   Up      6379/tcp            
redis_sentinel_1   docker-entrypoint.sh redis ...   Up      26379/tcp, 6379/tcp 
redis_slave_1      docker-entrypoint.sh redis ...   Up      6379/tcp     
```

Scale out the instance number of sentinel

```
docker-compose scale sentinel=3
```

Scale out the instance number of slaves

```
docker-compose scale slave=2
```

Check the status of redis cluster

```
docker-compose ps
```

The result is 

```
         Name                        Command               State          Ports        
--------------------------------------------------------------------------------------
redis_master_1     docker-entrypoint.sh redis ...   Up      6379/tcp            
redis_sentinel_1   docker-entrypoint.sh redis ...   Up      26379/tcp, 6379/tcp 
redis_sentinel_2   docker-entrypoint.sh redis ...   Up      26379/tcp, 6379/tcp 
redis_sentinel_3   docker-entrypoint.sh redis ...   Up      26379/tcp, 6379/tcp 
redis_slave_1      docker-entrypoint.sh redis ...   Up      6379/tcp            
redis_slave_2      docker-entrypoint.sh redis ...   Up      6379/tcp            
```

Execute the test scripts
```
./test.sh
```
to simulate stop and recover the Redis master. And you will see the master is switched to slave automatically. 

Or, you can do the test manually to pause/unpause redis server through

```
docker pause rediscluster_master_1
docker unpause rediscluster_master_1
```
And get the sentinel information with the following commands

```
docker exec rediscluster_sentinel_1 redis-cli -p 26379 SENTINEL get-master-addr-by-name mymaster
```


## Reference useful documentation 
 - https://redis.io/topics/sentinel
 - https://redis.io/commands/hset
 
 
## Application logic

- Create data
    - logic save data to redis and create a scheduler with ttl less than redis TTL in this example 
    (Notes: The logic of how we create a scheduler not optimal better to use ScheduledExecutorService better thread reusability.)
        Redis TTL 120
        Refresh scheduler TTL 105
```
curl -X POST \
  'http://127.0.0.1:9001/keys' \
  -H 'Content-Type: application/json' \
  -H 'Postman-Token: f6d28274-a901-437e-ad4c-93c0143d6e2d' \
  -H 'cache-control: no-cache' \
  -d '{
    "id": "user"
}'
```

- Application logs
```
2021-02-16 11:14:41.858  INFO 45316 --- [nio-9001-exec-1] com.levik.redis.service.UserService      : saveOrUpdate -> User(id=test)
2021-02-16 11:14:42.052  INFO 45316 --- [nio-9001-exec-1] com.levik.redis.service.UserService      : Start scheduler for User(id=test) close ttl 105000 millisecond now 2021-02-16T11:14:42.050507

2021-02-16 11:16:27.061  INFO 45316 --- [efresh-key-user] com.levik.redis.service.UserService      : Trigger update -> User(id=test) now 2021-02-16T11:16:27.061381
2021-02-16 11:16:27.061  INFO 45316 --- [efresh-key-user] com.levik.redis.service.UserService      : saveOrUpdate -> User(id=test)
2021-02-16 11:16:27.078  INFO 45316 --- [efresh-key-user] com.levik.redis.service.UserService      : Start scheduler for User(id=test) close ttl 105000 millisecond now 2021-02-16T11:16:27.078483
```

- Get data by id

```
curl -X GET 'http://127.0.0.1:9001/keys?id=user'
```

- Application logs

```
2021-02-16 11:20:34.304  INFO 45316 --- [nio-9001-exec-9] com.levik.redis.service.UserService      : getUserById -> user
```