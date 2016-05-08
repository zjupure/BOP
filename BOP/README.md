###2016.5.7

完成Academy API的网络请求，JSON数据解析，封装成GraphNode数据结构

GraphNode表示图中的一个节点，从API获取到的GraphNode可以知道其所有相邻的节点id。

* PaperNode表示根据paper id查询到的一个文章节点
* AuthorNode表示根据author id查询到的一个作者节点
* CiteNode表示根据paper id查询到的一个反向引用节点

