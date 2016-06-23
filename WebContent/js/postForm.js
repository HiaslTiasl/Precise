$(function () {
	$("form").submit(function (e) {
		$.ajax({
			method: this.method,
			url: this.action,
			data: $("textarea").val(),
			dataType: "json",
			contentType: "application/json"
		}).then(function () {
			$("h2").text("OK");
		}, function (jqXHR, textStatus, errorThrown) {
			$("h2").text(errorThrown);
			$("#answer").html(jqXHR.responseText);
		});
		e.preventDefault();
		return false;
	});
});