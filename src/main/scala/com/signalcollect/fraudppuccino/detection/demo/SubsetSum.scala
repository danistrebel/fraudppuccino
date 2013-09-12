package com.signalcollect.fraudppuccino.detection.demo

object SubsetSum extends App {
  val target = 55
  val results = new Array[Long](20)

  for (upperBound <- 1 to 20) {

    val set = (1 to upperBound).toList

    //    for (i <- 1 to 10) {
    //      naiveSubsetSum(set, target)
    //    }
    //
    //    var totalRuntime = 0l
    //    for (i <- 1 to 10) {
    //      val start2 = System.currentTimeMillis()
    //      naiveSubsetSum(set, target)
    //      val duration2 = System.currentTimeMillis - start2
    //      println(upperBound + "\t" + duration2)
    //      totalRuntime += duration2
    //    }
    //
    //    println("============================")

    for (i <- 1 to 10) {
      dynamicProgrammingSubsetSum(set, target)
    }
    var totalRuntime = 0l
    for (i <- 1 to 10) {
      val start2 = System.currentTimeMillis()
      dynamicProgrammingSubsetSum(set, target)
      val duration2 = System.currentTimeMillis - start2
      //      println(upperBound + "\t" + duration2)
      totalRuntime += duration2
    }

    results(upperBound - 1) = totalRuntime

  }

  for (i <- 0 until 20) {
    println(results(i))
  }

  def naiveSubsetSum(set: List[Int], targetValue: Int) = set.toSet.subsets.filter(_.sum == targetValue).toList

  def dynamicProgrammingSubsetSum(set: List[Int], targetValue: Int) = {
    var subsets = set.map(elem => (List(elem), elem, set.dropWhile(_ != elem).drop(1)))
    while (!(subsets.exists(_._2 == targetValue) || subsets.isEmpty)) { //expanding is stopped if the sum is reached or all possible combinations are expanded
      subsets = subsets.filter(partialResult => !partialResult._3.isEmpty && partialResult._2 < targetValue) //drop all with no more remaining options
//      subsets = subsets.filter(partialResult => partialResult._2 < targetValue) //drop all with no more remaining options
//
      //subsets = subsets.flatMap(partialResult => partialResult._3.tails.filter(!_.isEmpty).map(tail => (tail.head :: partialResult._1, partialResult._2 + tail.head, tail.tail)))

//      subsets = subsets.flatMap(partialResult => partialResult._3.tails.map(tail => {
//        tail match {
//        	case elementToAdd::unexpanded => (elementToAdd::partialResult._1, partialResult._2 + elementToAdd, unexpanded)
//        	case Nil => (List(),0, List())
//        }	
//      }))

      
      //            subsets = subsets.map(partialResult => {
      //              for (tail <- partialResult._3.tails if !tail.isEmpty) yield (tail.head :: partialResult._1, partialResult._2 + tail.head, tail.tail)
      //            }).flatten
      subsets = subsets.flatMap(partialResult => {
        partialResult._3.map(elementToAdd => {
          (elementToAdd :: partialResult._1, partialResult._2 + elementToAdd, partialResult._3.dropWhile(_ != elementToAdd).drop(1))
        })
      })

    }
  }

  //   subsets = subsets.map(partialResult => {
  //        partialResult._3.map(elementToAdd => {
  //          (elementToAdd :: partialResult._1, partialResult._2 + elementToAdd, partialResult._3.dropWhile(_ != elementToAdd).drop(1))
  //        })
  //      }).flatten

  //println(subsets.filter(_._2 == targetValue)) 

}