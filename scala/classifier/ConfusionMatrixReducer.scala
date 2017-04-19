package classifier

import org.apache.spark.AccumulatorParam

import core.{DataBase, Mediator}

/**
 * Reducer object used to sum received confusion matrices 2x2
 * @author Eva M. Almansa
 * @version 1.0
 */
object ConfusionMatrixReducer extends AccumulatorParam[Array[Array[Int]]] { 
  
  override def zero(initialValue: Array[Array[Int]]): Array[Array[Int]] = {
    Array.fill(initialValue.size, initialValue.size)(0)
  }

  override def addInPlace(s1: Array[Array[Int]], s2: Array[Array[Int]]): Array[Array[Int]] = {
      var solution: Array[Array[Int]] = Array.fill(s1.size, s1.size)(0)
 
      solution(0)(0) = s1(0)(0) + s2(0)(0) 
      solution(0)(1) = s1(0)(1) + s2(0)(1) 
      solution(1)(1) = s1(1)(1) + s2(1)(1)
      solution(1)(0) = s1(1)(0) + s2(1)(0)
       
      solution  
  }
	
  def metricsConfusionMatrix(solution: Array[Array[Int]], dataBase: DataBase): Double = {
    var TP, TN, FP, FN: Int = 0
    for(classIndex <- 0 to (solution.size - 1)){
      if(classIndex == 0){
        //println("@ C=" + dataBase.getClassLabel(classIndex.toByte) + " | TP =" + solution(classIndex)(0) + " FN =" + solution(classIndex)(1))
        TP = TP + solution(classIndex)(0)
        FN = FN + solution(classIndex)(1)
      }
      else{
        //println("@ C=" + dataBase.getClassLabel(classIndex.toByte) + " | FP =" + solution(classIndex)(0) + " TN =" + solution(classIndex)(1))
        FP = FP + solution(classIndex)(0)
        TN = TN + solution(classIndex)(1)
      }
    }

    val TPR = (TP/(TP+FN).toDouble)
    val FPR = (FP/(FP+TN).toDouble) //FP / (FP + TN)
    val AUC = ((1 + TPR - FPR) / 2.0)
    /*println("@ TPR => " + TPR)
    println("@ FPR => " + FPR)
    //println("@ TNR => " + (TN/(TN+FP).toDouble))
    println("@ AUC => " + AUC)*/

    AUC
  }
}

