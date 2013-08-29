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
package com.signalcollect.fraudppuchino.repeatedanalysis

import com.signalcollect.Vertex
import com.signalcollect.GraphEditor
import com.signalcollect.Edge
import scala.collection.JavaConversions._ 

/**
 * This Vertex aims to facilitate the repeated execution of possibly different computations on the
 * same graph structure.
 */
class RepeatedAnalysisVertex[Id](val id: Id) extends Vertex[Id, Any] {

  /**
   * Pluggable Algorithm definition
   */
  var algorithm: VertexAlgorithm = new DummyVertexAlgorithm
  def setAlgorithmImplementation(algorithmFactory: (RepeatedAnalysisVertex[Id]) => VertexAlgorithm) {
    algorithm = algorithmFactory.apply(this)
  }
  
  def removeAlgorithmImplementation = new DummyVertexAlgorithm

  /**
   * Holds results from previous computations and makes them available
   */
  var results: collection.mutable.Map[String, Any] = new java.util.HashMap[String, Any](0)

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
  def storeAttribute(key: String, value: Any) {results.put(key, value)}
  
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

  var outgoingEdges: collection.mutable.Map[Any, Edge[_]] = new java.util.HashMap[Any, Edge[_]](0)

  def addEdge(edge: Edge[_], graphEditor: GraphEditor[Any, Any]): Boolean = {
    outgoingEdges.get(edge.targetId) match {
      case None =>
        algorithm.noitfyTopologyChange
        outgoingEdges.put(edge.targetId, edge)
        edge.onAttach(this, graphEditor)
        true
      case Some(edge) =>
        false
    }
  }

  def removeEdge(targetId: Any, graphEditor: GraphEditor[Any, Any]): Boolean = {
    val outgoingEdge = outgoingEdges.get(targetId)
    outgoingEdge match {
      case None =>
        false
      case Some(edge) =>
        algorithm.noitfyTopologyChange
        outgoingEdges.remove(targetId)
        true
    }
  }

  def removeAllEdges(graphEditor: GraphEditor[Any, Any]): Int = {
    val edgesRemoved = outgoingEdges.size
    for (outgoingEdge <- outgoingEdges.keys) {
      removeEdge(outgoingEdge, graphEditor)
    }
    edgesRemoved
  }

  def edgeCount(): Int = outgoingEdges.size

  //Stuff that is delegated to the actual algorithm implementation
  def state(): Any = algorithm.getState
  def setState(state: Any): Unit = algorithm.setState(state)
  def deliverSignal(signal: Any, sourceId: Option[Any], graphEditor: GraphEditor[Any, Any]): Boolean = algorithm.deliverSignal(signal, sourceId, graphEditor)
  def executeSignalOperation(graphEditor: GraphEditor[Any, Any]): Unit = algorithm.executeSignalOperation(graphEditor, outgoingEdges.values)
  def executeCollectOperation(graphEditor: GraphEditor[Any, Any]): Unit = algorithm.executeCollectOperation(graphEditor)
  def scoreSignal(): Double = algorithm.scoreSignal
  def scoreCollect(): Double = algorithm.scoreCollect

  def afterInitialization(graphEditor: GraphEditor[Any, Any]): Unit = {}
  def beforeRemoval(graphEditor: GraphEditor[Any, Any]): Unit = {}

  override def toString: String = "ID: " + id + " State: " + algorithm.getState + " (" + algorithm + ") " + results.map(result => result._1.toString + " " + result._2.toString)

}