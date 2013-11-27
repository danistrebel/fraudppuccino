/**
 * Bitcoin specific functionality for the visualizer
 */

function Bitcoin () {
}

Bitcoin.getDetailsFor = function(element)
{
	if (element.account) {
		var account = element.account;
		var details = '<h1>Account #' + element.name + '</h1>'
				+ '<table class="table table-striped">'
				+ '<tr><td># Transactions in</td><td>' + account["in-count"]
				+ '</tr>' + '<tr><td># Transactions out</td><td>'
				+ account["out-count"] + '</td></tr>'
				+ '<tr><td>BTC Transactions in</td><td>' + Bitcoin.satoshiToBtc(account["in"])
				+ ' BTC</td></tr>' + '<tr><td>BTC Transactions out</td><td>'
				+ Bitcoin.satoshiToBtc(account["out"]) + ' BTC</td></tr>' + '</table>'
		return details;
	}

	else if (element.transaction) {
		var transaction = element.transaction
		return Bitcoin.getTransactionDetails(transaction);
	} else if (element.transactions) {
		var transactionDetails = '';
		$.each(element.transactions, function(i, t) {
			transactionDetails += Bitcoin.getTransactionDetails(t);
		});
		return transactionDetails;
	}
}

Bitcoin.getTransactionDetails = function(transaction)
{
	var transactionDate = new Date(transaction.time * 1000);
	var details = '<h1>Transaction #' + Math.abs(transaction.id) + '</h1>'
			+ '<table class="table table-striped">'
			+ '<tr><td>BTC Transaction Value</td><td>' + Bitcoin.satoshiToBtc(transaction.value)
			+ ' BTC</td></tr>' + '<tr><td>Time</td><td>'
			+ transactionDate.toLocaleDateString() + ' '
			+ transactionDate.toLocaleTimeString() + '</td></tr>'
			+ '<tr><td>Source</td><td>' + transaction.src + '</td></tr>'
			+ '<tr><td>Target</td><td>' + transaction.target + '</td></tr>'
			+ '<tr><td>Cross Country</td><td>' + transaction.xCountry
			+ '</td></tr>' + '</table>'
	return details;
}

/**
 * Characteristic information that represents the report in the component list.
 */
Bitcoin.getReportSignature = function(report) {
	return 'BTC ' + Bitcoin.satoshiToBtc(report.flow) + ', ' + report.members.length + ' transactions'
	
}

Bitcoin.satoshiToBtc = function(satoshi) {
	return Math.round(satoshi / 100000000 * 10000) / 10000
}
