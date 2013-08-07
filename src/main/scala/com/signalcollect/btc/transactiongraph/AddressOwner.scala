package com.signalcollect.btc.transactiongraph

class AddressOwner(val addresses: List[PublicAddress] = List[PublicAddress]()) {
 
 
 def mergeOwners(other: AddressOwner): AddressOwner = {
   val merged = addresses++other.addresses
   new AddressOwner(merged)
 }
}