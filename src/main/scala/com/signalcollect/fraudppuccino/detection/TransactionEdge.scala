package com.signalcollect.fraudppuccino.detection

import com.signalcollect.DefaultEdge
import com.signalcollect.fraudppuccino.repeatedanalysis.EdgeMarker
import com.signalcollect.fraudppuccino.repeatedanalysis.EdgeMarkerWrapper

/**
 * Connects transactions to the source and target accounts
 */ 
class TransactionEdge(targetTransactionID: Int) extends EdgeMarkerWrapper(targetTransactionID, Transaction)

/**
 * Marker object
 */ 
case object Transaction extends EdgeMarker