# Redis proxy方案

很多老的proxy方案，比如codis等都是实现proxy sharding用，并不是用作简单用作协议实现和重连。
这里主要尝试了redis-cluster-proxy和envoy，在测试下envoy稳定性和社区热度都要高一些，所以选择了envoy方案。

Envoy 可以作为 Redis 代理，在集群的实例间对命令进行分区。在这种模式下， Envoy 的目标是在一致性前提下维护可用性和分区容错。这是 Envoy 和 Redis 集群 关键差异。 Envoy 被设计为尽力而为的缓存，意味着它不会试图协调不一致的数据或者保持全局集群成员一致视图。它还支持基于不同的访问模式，驱逐或隔离需求，将命令从不同的工作负载路由到不同的上游集群。
https://cloudnative.to/envoy/intro/arch_overview/other_protocols/redis.html
Envoy Redis 特性:
•	Redis 协议 <https://redis.io/topics/protocol>_ 编解码
•	基于哈希的分区
•	Ketama 分布式一致性哈希算法
•	命令统计详情
•	主动和被动健康检查
•	哈希标记
•	路由前缀
•	下游客户端和上游服务器分别进行身份验证
•	针对所有请求或写请求监控
•	管控 读请求路由. 仅适用于 Redis 集群。
Redis 集群支持（实验性）
Envoy 目前为 Redis 集群 提供实验性支持。
服务可以使用以任意语言实现的非集群 Redis 客户端连接到代理，就像它是一个单节点 Redis 实例一样。Envoy 代理将跟踪集群拓扑，并根据 规范 向集群中正确的 Redis 节点发送命令。还可以将高级功能（例如从副本中读取）添加到 Envoy 代理中，而不用更新每种语言的 Redis 客户端。
Envoy 通过定期向集群中的随机节点发送 cluster slot cluster slots 命令来跟踪群集的拓扑，并维护以下信息：
•	已知节点列表
•	每个分片的主节点
•	集群节点的增加或减少

支持的命令
在协议级别支持管道。MULTI （事务块）不支持。尽可能使用管道以获得最佳性能。
在命令级别，Envoy 仅支持可以被可靠地哈希到一台服务器的命令。只有 AUTH 和 PING 命令例外。如果下游配置了密码，Envoy 将在本地处理 AUTH，并且在配置了密码之后，在身份认证成功之前，Envoy 不会处理任何其他命令。如果上游为整个集群配置了密码，Envoy 将在连接到上游服务器后透明地发送 AUTH 命令。Envoy 会立即为 PING 命令返回 PONG。PING 命令不接受参数。所有其他支持的参数必须包含一个 key。除了执行失败的情况外，所有支持的命令功能与原始 Redis 命令完全一致。

https://www.envoyproxy.io/docs/envoy/latest/intro/arch_overview/other_protocols/redis


Envoy Redis-cluster的支持：

因为在redis sharding之后，是通过move到其他的node，如果默认的话，envoy不会转到其他的node，而是返回一个move error（包括node和 port信息）。
通过设置：
https://github.com/envoyproxy/envoy/issues/5697
enable_redirection: true
让envoy实现跳转。
 

然后就是让客户端保留cluster的slot信息以及keys等信息，让服务路由更高效
