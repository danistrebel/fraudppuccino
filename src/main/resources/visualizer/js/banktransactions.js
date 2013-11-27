/**
 * Bitcoin specific functionality for the visualizer
 */

function BankTransactions () {
}

BankTransactions.getDetailsFor = function(element)
{
	if (element.account) {
		var account = element.account;
		var details = '<h1>Account #' + element.name + '</h1>'
				+ '<table class="table table-striped">'
				+ '<tr><td># Transactions in</td><td>' + account["in-count"]
				+ '</tr>' + '<tr><td># Transactions out</td><td>'
				+ account["out-count"] + '</td></tr>'
				+ '<tr><td>Total of incoming Transactions</td><td>' + account["in"]
				+ ' CHF</td></tr>' + '<tr><td>Total of outgoing Transactions</td><td>'
				+ account["out"] + ' CHF</td></tr>' + '</table>'
		return details;
	}

	else if (element.transaction) {
		var transaction = element.transaction
		return BankTransactions.getTransactionDetails(transaction);
	} else if (element.transactions) {
		var transactionDetails = '';
		$.each(element.transactions, function(i, t) {
			transactionDetails += BankTransactions.getTransactionDetails(t);
		});
		return transactionDetails;
	}
}

BankTransactions.getTransactionDetails = function(transaction)
{
	var transactionDate = new Date(transaction.time * 1000);
	var details = '<h1>Transaction #' + Math.abs(transaction.id) + '</h1>'
			+ '<table class="table table-striped">'
			+ '<tr><td>Transaction Value</td><td>' + transaction.value
			+ ' CHF</td></tr>' + '<tr><td>Time</td><td>'
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
BankTransactions.getReportSignature = function(report) {
	return 'CHF ' + Math.round(report.flow * 10000) / 10000 + ', ' + report.members.length + ' transactions'
	
}