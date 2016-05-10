###2016.5.7

完成Academy API的网络请求，JSON数据解析，封装成GraphNode数据结构

GraphNode表示图中的一个节点，从API获取到的GraphNode可以知道其所有相邻的节点id。

* PaperNode表示根据paper id查询到的一个文章节点
* AuthorNode表示根据author id查询到的一个作者节点
* CiteNode表示根据paper id查询到的一个反向引用节点（将废弃）
* RefNode表示根据paper id和其RId查询到的一个前向引用节点

###2016.5.8

修复一些Academy API请求的bug

实现基本的路由算法

###2016.5.10

url增加count配置参数，默认值设为1000

寻路算法bug待修复

一篇文献被大量引用时，CiteNode不能正常工作，CiteNode将废弃