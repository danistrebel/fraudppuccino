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

import com.signalcollect._
import com.signalcollect.interfaces._

/**
 * Used to identify different edge types 
 */ 
abstract class EdgeMarker

/**
 * Marker for edges where its type is unknown
 */ 
case object UnknownEdge extends EdgeMarker

/**
 * Wrapper to transport EdgeMarker information via the default Edge interface
 */ 
case class EdgeMarkerWrapper(edgeTarget: Any, marker: EdgeMarker) extends Edge[Any] {
  type Source = Vertex[_,_]
  
  def id: EdgeId[_] = null

  override def sourceId: Any = null
  def targetId = null
  def source = null

  /** Called when the edge is attached to a source vertex */
  def onAttach(source: Vertex[_, _], graphEditor: GraphEditor[Any, Any]) = {}

  /** The weight of this edge. */
  def weight: Double = 0.0
  
  def executeSignalOperation(sourceVertex: Vertex[_, _], graphEditor: GraphEditor[Any, Any]) = {}
}
