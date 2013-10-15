/*
 *  @author Daniel Strebel
 *
 *  Copyright 2013 University of Zurich
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.signalcollect.fraudppuccino.repeatedanalysis

import com.signalcollect.Vertex
import com.signalcollect.GraphEditor
import com.signalcollect.Edge
import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.AbstractBuffer

/**
 * This Vertex aims to facilitate the repeated execution of possibly different computations on the
 * same graph structure.
 */
class RepeatedAnalysisVertex[Id](val id: Id) extends Vertex[Id, Any] {

  /**
   * Pluggable Algorithm definition
   */
  var algorithm: VertexAlgorithm = new DummyVertexAlgorithm
  var nextAlgorithm: RepeatedAnalysisVertex[_] => VertexAlgorithm = null

  def setAlgorithmImplementation(algorithmFactory: (RepeatedAnalysisVertex[Id]) => VertexAlgorithm) {
    algorithm = algorithmFactory.apply(this)
  }

  def removeAlgorithmImplementation = algorithm = new DummyVertexAlgorithm

  def loadNextAlgorithm = if (nextAlgorithm != null) {
    algorithm = nextAlgorithm(this)
    nextAlgorithm = null
  }

  /**
   * Holds results from previous computations and makes them available
   */
  val results: collection.mutable.Map[String, Any] = new java.util.HashMap[String, Any](0)

  /**
   * Store the current computational state to make it available to subsequent computations
   *
   * @param key unique key to store states on this vertex
   */
  def retainState(key: String) { results.put(key, algorithm.getState) }

  /**
   * Store some computational state to make it available to subsequent computations
   *
   * @param key unique key to store states on this vertex
   */
  def storeAttribute(key: String, value: Any) { results.put(key, value) }

  /**
   * Returns the results from previous computations
   *
   * @param key Identifier of the state that should be retrieved
   */
  def getResult(key: String): Option[Any] = { results.get(key) }

  /**
   * Deletes the stored state with the specified key
   *
   * @param key The key to be deleted
   */
  def dropState(key: String) { results.remove(key) }

  var outgoingEdges = ArrayBuffer[(Any, EdgeMarker)]()

  /**
   * Adds edges without checking for duplicates
   * The edge object is trashed in order to save memory
   */
  def addEdge(edge: Edge[_], graphEditor: GraphEditor[Any, Any]): Boolean = {

    edge match {
      case markerEdge: EdgeMarkerWrapper => outgoingEdges += ((markerEdge.edgeTarget, markerEdge.marker))
      case otherEdge: Edge[_] => outgoingEdges += ((edge.targetId, UnknownEdge))
    }
    true
  }

  def removeEdge(targetId: Any, graphEditor: GraphEditor[Any, Any]): Boolean = {
    val sizeBefore = outgoingEdges.size
    outgoingEdges = outgoingEdges.filter(_._1 != targetId)
    outgoingEdges.size != sizeBefore
  }

  def removeAllEdges(graphEditor: GraphEditor[Any, Any]): Int = {
    val edgesRemoved = outgoingEdges.size
    outgoingEdges.clear()
    edgesRemoved
  }

  def removeEdgesOfType(edgeType: EdgeMarker) {
    outgoingEdges = outgoingEdges.filter(_._2 != edgeType)
  }

  def edgeCount(): Int = outgoingEdges.size

  //Stuff that is delegated to the actual algorithm implementation
  def state(): Any = algorithm.getState
  def setState(state: Any): Unit = algorithm.setState(state)

  def deliverSignal(signal: Any, sourceId: Option[Any], graphEditor: GraphEditor[Any, Any]): Boolean = {
    val hasCollected = algorithm.deliverSignal(signal, sourceId, graphEditor)
    loadNextAlgorithm
    hasCollected
  }
  def executeSignalOperation(graphEditor: GraphEditor[Any, Any]): Unit = algorithm.executeSignalOperation(graphEditor, outgoingEdges)

  def executeCollectOperation(graphEditor: GraphEditor[Any, Any]): Unit = {
    algorithm.executeCollectOperation(graphEditor)
    loadNextAlgorithm
  }
  def scoreSignal(): Double = algorithm.scoreSignal
  def scoreCollect(): Double = algorithm.scoreCollect

  def afterInitialization(graphEditor: GraphEditor[Any, Any]): Unit = {}
  def beforeRemoval(graphEditor: GraphEditor[Any, Any]): Unit = {}

  override def toString: String = "ID: " + id + " State: " + algorithm.getState + " (" + algorithm + ") " + results.map(result => result._1.toString + " " + result._2.toString)

}