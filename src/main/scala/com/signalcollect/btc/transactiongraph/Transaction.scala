package com.signalcollect.btc.transactiongraph

import com.signalcollect.Vertex
import com.signalcollect.DataGraphVertex
import com.signalcollect.DataFlowVertex

class Transaction(hash: Any, outputs: List[PublicAddress] = List()) extends DataGraphVertex(hash, false) {
	override def toString(): String = {
	  "transaction: " + hash 
	}
	
	def collect: Boolean = {
	  true
	}
}