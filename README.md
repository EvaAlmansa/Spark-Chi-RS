# Chi-Spark-RS: An Spark-built evolutionary fuzzy rule selection algorithm in imbalanced classification for big data problems

Abstract:
The significance and benefits of addressing classification tasks in Big Data applications is beyond any doubt. To do so, learning algorithms must be scalable to cope with such a high volume of data. The most suitable option to reach this objective is by using a MapReduce programming scheme, in which algorithms are automatically executed in a distributed and fault tolerant way. Among different available tools that support this framework, Spark has emerged as a “de facto” solution when using iterative approaches. In this work, our goal is to design and implement an Evolutionary Fuzzy Rule Selection algorithm within a Spark environment. To do so, we build different local rule bases within each Map Task that are later optimized by means of a genetic process. With this procedure, we seek to minimize the total number of rules that are gathered by each Reduce task to obtain a compact and accurate Fuzzy Rule Based Classification System. In particular, we set the experimental framework in the scenario of imbalanced classification. Therefore, the final objective will be analyzing the best synergy between the novel Evolutionary Fuzzy Rule Selection algorithm and the solutions applied to cope with skewed class distributions, namely cost-sensitive learning, random under-sampling and random-oversampling.

http://ieeexplore.ieee.org/document/8015520/

## Source code

This Scala source code contains the **Chi et al's algorithm** implementation for *MapReduce*. 

It may be run with *cost-sensitive learning* (higher RW for minority class) and rule selection within each Map task. 

The sintax for running the program is the following one:

**spark-submit --class run.fuzzyGenetic --executor-memory 52G --total-executor-cores <#Maps> Chi-Spark-CS-RS-1.0.jar <ParamFile.txt> <dataset_folder> <header_file> <train_file> <test_file> <output_dir>**

The Parameter File includes by default:

* inference=winning_rule
* num_linguistic_labels=3
* cost_sensitive=1
* num_individuals=50
* num_evaluations=0
* alpha=0.7
* init_seed=10000
* cross_validation=0

1. **inference** is the aggregation mechanism for determining the output class label, either winning_rule (the one rule with the highest compability degree) of additive_combination (all rules are used).
1. **num_linguistic_labels** determines the number of partitions for the grid search
1. **cost_sensitive** refers to whether the RW is updated in accordance with the class distribution (1) or not (0)
1. **num_individuals**, **num_evaluations**, **alpha**, and **init_seed** are parameters for the CHC procedure in case of Rule Selection (num_evaluations > 0)
1. **cross_validation** is used to carry out a direct FCV durinng the running of the program (the dataset file is iterated from 1 to the value pointed out here).

The dataset files must follow the KEEL [http://www.keel.es] format. Header file includes just the information about the attributes and classes (no @data is needed), whereas the train and test files includes the examples separated by commas.
