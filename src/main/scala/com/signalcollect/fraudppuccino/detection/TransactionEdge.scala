package com.signalcollect.fraudppuccino.detection

import com.signalcollect.DefaultEdge
import com.signalcollect.fraudppuccino.repeatedanalysis.EdgeMarker
import com.signalcollect.fraudppuccino.repeatedanalysis.EdgeMarkerWrapper

class TransactionEdge(targetTransactionID: Int) extends EdgeMarkerWrapper(targetTransactionID, Transaction)

case object Transaction extends EdgeMarker