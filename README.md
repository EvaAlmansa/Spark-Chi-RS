# Spark-Chi-RS

Evolutionary Fuzzy Rule Selection for Big Data

http://ieeexplore.ieee.org/document/8015520/

Abstract:
The significance and benefits of addressing classification tasks in Big Data applications is beyond any doubt. To do so, learning algorithms must be scalable to cope with such a high volume of data. The most suitable option to reach this objective is by using a MapReduce programming scheme, in which algorithms are automatically executed in a distributed and fault tolerant way. Among different available tools that support this framework, Spark has emerged as a “de facto” solution when using iterative approaches. In this work, our goal is to design and implement an Evolutionary Fuzzy Rule Selection algorithm within a Spark environment. To do so, we build different local rule bases within each Map Task that are later optimized by means of a genetic process. With this procedure, we seek to minimize the total number of rules that are gathered by each Reduce task to obtain a compact and accurate Fuzzy Rule Based Classification System. In particular, we set the experimental framework in the scenario of imbalanced classification. Therefore, the final objective will be analyzing the best synergy between the novel Evolutionary Fuzzy Rule Selection algorithm and the solutions applied to cope with skewed class distributions, namely cost-sensitive learning, random under-sampling and random-oversampling.

