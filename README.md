# Redis failover处理

在redis中，公有云平台提供了集群的paas服务，不同于一般的master和slave，它们是相对独立的复制集群。
当跨region的failover发生的时候，需要手动的修改连接字符串等。为了让程序不重启，就需要增加一个task来实现连接字符串的重置。
在redis中的java客户端，有如下三种，分别给出简单介绍和代码实现。

## Redis 客户端通信协议#
Redis制定了RESP（Redis Serialization Protocol，Redis序列化协议）实现客户端与服务端的正常交互，这种协议简单高效，既能够被机器解析，又容易被人类识别。

RESP可以序列化不同的数据类型，如整型、字符串、数组还有一种特殊的Error类型。需要执行的Redis命令会封装为类似于字符串数组的请求然后通过Redis客户端发送到Redis服务端。Redis服务端会基于特定的命令类型选择对应的一种数据类型进行回复。

## Redis 的 Java 客户端#

Redis 官方推荐的 Java 客户端有Jedis、lettuce 和 Redisson。

### 1. Jedis

Jedis 是老牌的 Redis 的 Java 实现客户端，提供了比较全面的 Redis 命令的支持，其官方网址是：http://tool.oschina.net/uploads/apidocs/redis/clients/jedis/Jedis.html。

优点：

支持全面的 Redis 操作特性（可以理解为API比较全面）。
缺点：

使用阻塞的 I/O，且其方法调用都是同步的，程序流需要等到 sockets 处理完 I/O 才能执行，不支持异步；
Jedis 客户端实例不是线程安全的，所以需要通过连接池来使用 Jedis。
### 2. lettuce

lettuce （[ˈletɪs]），是一种可扩展的线程安全的 Redis 客户端，支持异步模式。如果避免阻塞和事务操作，如BLPOP和MULTI/EXEC，多个线程就可以共享一个连接。lettuce 底层基于 Netty，支持高级的 Redis 特性，比如哨兵，集群，管道，自动重新连接和Redis数据模型。lettuce 的官网地址是：https://lettuce.io/

优点：

支持同步异步通信模式；
Lettuce 的 API 是线程安全的，如果不是执行阻塞和事务操作，如BLPOP和MULTI/EXEC，多个线程就可以共享一个连接。

### 3. Redisson

Redisson 是一个在 Redis 的基础上实现的 Java 驻内存数据网格（In-Memory Data Grid）。它不仅提供了一系列的分布式的 Java 常用对象，还提供了许多分布式服务。其中包括( BitSet, Set, Multimap, SortedSet, Map, List, Queue, BlockingQueue, Deque, BlockingDeque, Semaphore, Lock, AtomicLong, CountDownLatch, Publish / Subscribe, Bloom filter, Remote service, Spring cache, Executor service, Live Object service, Scheduler service) Redisson 提供了使用Redis 的最简单和最便捷的方法。Redisson 的宗旨是促进使用者对Redis的关注分离（Separation of Concern），从而让使用者能够将精力更集中地放在处理业务逻辑上。Redisson的官方网址是：https://redisson.org/

优点：

使用者对 Redis 的关注分离，可以类比 Spring 框架，这些框架搭建了应用程序的基础框架和功能，提升开发效率，让开发者有更多的时间来关注业务逻辑；
提供很多分布式相关操作服务，例如，分布式锁，分布式集合，可通过Redis支持延迟队列等。
缺点：

Redisson 对字符串的操作支持比较差。
### 4. 使用建议

结论：lettuce + Redisson

Jedis 和 lettuce 是比较纯粹的 Redis 客户端，几乎没提供什么高级功能。Jedis 的性能比较差，所以如果你不需要使用 Redis 的高级功能的话，优先推荐使用 lettuce。

Redisson 的优势是提供了很多开箱即用的 Redis 高级功能，如果你的应用中需要使用到 Redis 的高级功能，建议使用 Redisson。具体 Redisson 的高级功能可以参考：https://redisson.org/