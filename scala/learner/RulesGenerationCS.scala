package learner

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.log4j.{Level, Logger}

import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.conf.Configuration
import java.io.{BufferedWriter, OutputStreamWriter}

import core.{DataBase, Mediator, FuzzyRule, FuzzyVariable, KnowledgeBase, Population, Variable}
import utils.ConsequentPart

/**
 * Cost-sensitive version of RulesGenerationMapper 
 * @author Eva M. Almansa
 * @version 1.0
 */
class RulesGenerationCS() extends Serializable {
  
  /**
	 * DataBase
	 */
	private var dataBase: DataBase = null
  
	/**
	 * Rule Base
	 */
	private var ruleBase: Map[FuzzyRule, Array[ConsequentPart]] = null // Key: antecedents of the rule, Value: Classes of the rule
	private var classCost: Array[Double] = null // Cost associated to each class
	
	/**
	 * Variables's CHC genetic algorithm 
	 */
	var popSize, numEvaluations: Int = 0
	var alpha: Double = 0.0
	
	/**
	 * Counters
	 */
	private var startMs, endMs: Long = 0
	
	/**
	 * Temporal time output file
	 */
	private var time_outputFile: String = ""
	
	/**
	 * Rule Base
	 */
	//private var matchingDegrees: Array[Array[Double]] = null // Matching degrees of the classes of each rule

	/**
	 * Dataset
	 */
	private var inputValues: Array[Array[String]] = null // Input values of the instances of the current split of the dataset
	private var classLabels: Array[Byte] = null // Indices of the instances class labels
	
	/**
	 * Info. variables
	 */
	private var counter_class:  Array[Int] = null // Counter the occurrences related with positive class and negative class 
	private var counter_rules:  Array[Int] = null // Counter the occurrences related with positive class and negative class
	
	private def computeMatchingDegreesRule(){
		/*matchingDegrees = new Array[Double](ruleBaseTmp.size())(dataBase.getNumClasses())
		for (int i = 0; i < ruleBaseTmp.size(); i++)
			for (int j = 0; j < dataBase.getNumClasses(); j++)
				matchingDegrees[i][j] = 0.0f;
		membershipDegrees = new float[dataBase.getNumVariables()][Mediator.getNumLinguisticLabels()];
		rulesClasses = new ArrayList [ruleBaseTmp.size()];
		ruleBase = new byte[ruleBaseTmp.size()][dataBase.getNumVariables()];
		Iterator<Entry<ByteArrayWritable,ArrayList<Byte>>> iterator = ruleBaseTmp.entrySet().iterator();
		Entry<ByteArrayWritable,ArrayList<Byte>> ruleEntry;
		int i = 0;
		while (iterator.hasNext()){
			ruleEntry = iterator.next();
			ruleBase[i] = ruleEntry.getKey().getBytes(); // Antecedents of the rule
			rulesClasses[i] = ruleEntry.getValue(); // Classes of the rule
			i++;
		}*/
	}

	private def computeMatchingDegreesAll(){
	
		/*for (i <- 0 to (inputValues.length - 1)){
			// Compute the membership degree of the current value to all linguistic labels
			for (j <- 0 to (dataBase.getNumVariables() - 1)) {
				if (dataBase.get(j).isInstanceOf[FuzzyVariable])
					for (label <- 0 to (Mediator.getNumLinguisticLabels() - 1))
						membershipDegrees(j)(label) = dataBase.computeMembershipDegree(j,label,inputValues(i)(j));
			}
			// Compute the matching degree of the example with all rules
			for (j <- 0 to (ruleBase.length - 1)){
				matchingDegrees(j)(classLabels(i)) += dataBase.computeMatchingDegree(
						membershipDegrees, ruleBase(j), inputValues(i))
			}
		}*/
	}

	private def computeRuleWeight(i: Int): Double = {
	  
		var currentRW, ruleWeight, sum, sumOthers: Double = 0.0
		/*classIndex = -1
		for (j <- 0 to (matchingDegrees(i).length - 1)){
			sum += matchingDegrees(i)(j)
		}
		for (j <- 0 to (matchingDegrees(i).length - 1)){
			if (rulesClasses(i).contains(j.toByte)){
				sumOthers = sum - matchingDegrees(i)(j)
				currentRW = (matchingDegrees(i)(j) - sumOthers) / sum; //P-CF
				if (currentRW > ruleWeight){
					ruleWeight = currentRW
					classIndex = j.toByte
				}
			}
		}*/
		ruleWeight
	} 
	
	def setup(sc: SparkContext, conf: SparkConf): RulesGenerationCS = {
    
		//Starting logger
    var logger = Logger.getLogger(this.getClass())    
    
    /**
		 * STEP 1: Read Learner configuration (paths, labels, and so on)
		 */
		try {
			//Mediator.readLearnerConfiguration(conf)
			popSize = Mediator.getPopSize()
			numEvaluations = Mediator.getNumEvaluations()
			alpha = Mediator.getAlpha()
			time_outputFile = Mediator.getLearnerOutputPath()+"//"+Mediator.TIME_STATS_DIR
		}
		catch{
		   case e: Exception => {
        System.err.println("\nSTAGE 1: ERROR READING CONFIGURATION => ")
        e.printStackTrace()
        System.exit(-1)}
		}
		
		/**
		 * STEP 2: Read DataBase configuration (create fuzzy partitions)
		 */
		try{
			dataBase = new DataBase(sc, Mediator.getHeaderPath())
			//println(dataBase.toString())
		}catch{
		  case e: Exception => {
        System.err.println("\nMAP: ERROR BUILDING DATA BASE\n")
        e.printStackTrace()
        System.exit(-1)}
		}
		
		ruleBase = Map[FuzzyRule, Array[ConsequentPart]]()
		inputValues = Array[Array[String]]()
		classLabels = Array[Byte]()
		
		/**
		 * Compute the cost of each class
		 */
		classCost = Array.fill(dataBase.getNumClasses())(1.0)
		if(Mediator.getCostSensitive()){
  		var numExamples: Array[Long] = dataBase.getClassNumExamples()
  		if (numExamples(0) < numExamples(1)){
  			classCost(1) = (numExamples(0).toDouble)/(numExamples(1).toDouble) // Maj = (1/IR)
  		  //classCost(0) = (numExamples(1).toDouble)/(numExamples(0).toDouble) // Min = (IR)
  		}
  		else if (numExamples(0) > numExamples(1)){
  			classCost(0) = (numExamples(1).toDouble)/(numExamples(0).toDouble) // Maj = (1/IR)
  		  //classCost(1) = (numExamples(0).toDouble)/(numExamples(1).toDouble) // Min = (IR)
  		}
		}
		this
	}
  
  def ruleBasePartition(index: Int, values: Iterator[String], sc: SparkContext): Iterator[KnowledgeBase] = {
    
    startMs = System.currentTimeMillis()
    
    var populationSet = Set[FuzzyRule]()
    var kb = new KnowledgeBase(dataBase) 
    counter_class = Array.fill(dataBase.getNumClasses())(0) //Counter of ocurrences
    counter_rules = Array.fill(dataBase.getNumClasses())(0) //Counter of ocurrences
    
    while(values.hasNext){
      val value = values.next
      var input: Array[String] = null
      input = value.replaceAll(" ", "").split(",")
      
      if(input.length == (dataBase.getPosClassLabels() + 1)){     
        val classIndex: Byte = dataBase.getClassIndex(input(dataBase.getPosClassLabels()))
        
        /*if (classIndex == -1){
  				throw new SecurityException("\nERROR RULES GENERATION: The class is not correct, this algorithm is not preparated for empty or N/A data, error="+input(dataBase.getPosClassLabels())+"*\n")
  			}else*/
        if (classIndex != -1){
  			
          inputValues = inputValues :+ input      
          classLabels = classLabels :+ classIndex
          
          counter_class(classIndex) = counter_class(classIndex) + 1 
          
          val antecedents = dataBase.getRuleFromExample(input)
          val newRule = new FuzzyRule(antecedents, dataBase.getNumClasses())
          var classEntry: Array[ConsequentPart] = null // (Index, RuleWeight)
          
          val matchingDegrees = Array.fill(dataBase.getNumClasses())(0.0)  
        	val membershipDegrees = Array.fill(dataBase.getNumVariables(), dataBase.getNumLinguisticLabels())(0.0)
        	 
        	// Compute the membership degree of the current value to all linguistic labels
        	for (j <- 0 to (dataBase.getNumVariables() - 1)) {
          		if (dataBase.get(j).isInstanceOf[FuzzyVariable]){
          			for (label <- 0 to (dataBase.getNumLinguisticLabels() - 1)){
          				membershipDegrees(j)(label) = dataBase.computeMembershipDegree(j.toByte, label.toByte, input(j))
          			}
          		}
        	}
          
        	// Compute the matching degree of the example with a rule
        	matchingDegrees(classIndex) = (dataBase.computeMatchingDegree(membershipDegrees, antecedents, input) * classCost(classIndex))       
        	
        	var consequent = Array[ConsequentPart]()
        	consequent = consequent :+ new ConsequentPart(classIndex, matchingDegrees(classIndex))
        	val aux = ruleBase.get(newRule)
        	if( aux == None){
        	  ruleBase += (newRule -> consequent)
        	}else{
        	  classEntry = aux.get
        	  var contains: Boolean = false
        	  for(i <- 0 to (classEntry.length - 1)){
        	    if(classEntry(i).getClassIndex() == classIndex){
        	      contains = true
        	      classEntry(i).addRuleWeight(matchingDegrees(classIndex))        	   
        	    }
        	  }
            if (!contains){
            	classEntry = classEntry :+ (consequent(0))
            	ruleBase += (newRule -> classEntry)
            }
        	} 	
  			}
      }
    }
     
    for (rule <- ruleBase){
      var weight, weightOther, sumTotal: Double = 0.0
      var classIndex,s: Byte = 0
      for(consequent <- rule._2){
        sumTotal = sumTotal + consequent.getRuleWeight()
        if(consequent.getRuleWeight() > weight){
          if((weight > 0.0) && (weightOther == 0.0)){
            weightOther = weight
          }
          weight = consequent.getRuleWeight()
          classIndex =  consequent.getClassIndex()
        }else if((consequent.getRuleWeight() < weightOther) || (weightOther == 0.0)){
          weightOther = consequent.getRuleWeight()
        }
      }
      
      weight = (weight - weightOther)/sumTotal //P-CF
      
      //logger.info("@ Rule - " + rule._1.getAntecedent().deep.mkString(" | ") + " | C=" + classIndex + " | W=" + weight)
      if(weight > 0){
        //println("@ Index="+ index.toString+", Rule - " + rule._1.getAntecedent().deep.mkString(" ") + " | C=" + classIndex + " | W=" + weight)
        
        val res = new FuzzyRule(rule._1.getAntecedent(), classIndex, weight, dataBase.getNumClasses())
        
        counter_rules(classIndex) = counter_rules(classIndex) + 1 
  		 
  		  //populationSet += res
        kb.addFuzzyRule(res)
      }
    }
    
    kb.initCounterRules(counter_rules)
    
    var pop = new Population()
		if (kb.size() > 0 && numEvaluations > 0){
		  //println("kb before=" + kb.toString())
			pop = new Population(kb,popSize,numEvaluations,1.0,62,alpha,inputValues,classLabels)
			kb = pop.Generation(sc)
		}

    //println("@ Map= "+index.toString+" | Counter class=> Positive= " + kb.getCounter().getPositive() + ", Negative= " + kb.getCounter().getNegative())
     
    /**
		 *  Write execution time
		 */    
		endMs = System.currentTimeMillis()
		try {
		  
		  val conf = sc.hadoopConfiguration
  		val fs = FileSystem.get(conf)
  		var textPath: Path = null
		  if(!fs.exists(new Path(time_outputFile+"//mapper"+index+".txt"))){
			  textPath = new Path(time_outputFile+"//mapper"+index+".txt")
		  }else {
		    
		    var create = false
		    var i: Int = 1
		    var aux = "00"
		    
		    while(i<100 && !create){
		      if (i == 10)
		        aux = "0"
		      
		      if(!fs.exists(new Path(time_outputFile+"//mapper"+index+"_"+aux+i+".txt"))){
    			  textPath = new Path(time_outputFile+"//mapper"+index+"_"+aux+i+".txt")
    			  create = true
    		  }
		      i = i + 1
		    }
		  }
		  
		  var bwText = new BufferedWriter(new OutputStreamWriter(fs.create(textPath,true)))
		  
    	bwText.write("Execution time (seconds): "+((endMs-startMs)/1000.0))
    	bwText.close()
      
		}catch{
		  case e: Exception => {
	    System.err.println("\nMAPPER: ERROR WRITING EXECUTION TIME")
		  e.printStackTrace()
		  System.err.println(-1)
		 }
		}
		
		//kb.addCounterClass(counter_class)
			
		Iterator(kb)
  }
  
}

/**
 * Distributed RulesGenerationCS class.
 *
 * @author Eva M. Almansa 
 */
object RulesGenerationCS {
  /**
   * Initial setting necessary.
   */
  def setup(sc: SparkContext, conf: SparkConf) = {
    new RulesGenerationCS().setup(sc, conf)
  }
}
