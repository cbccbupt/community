$(function(){
	$("#sendBtn").click(send_letter);
	$("#deleteBtn").click(delete_msg);
	$(".close").click(close_msg);
});

function send_letter() {
	$("#sendModal").modal("hide");
	var toName = $("#recipient-name").val();
	var content = $("#message-text").val();
	$.post(
		CONTEXT_PATH + "/letter/send",
		{"toName":toName,"content":content},
		function (data){
			data = $.parseJSON(data);
			if(data.code==0){
				$("#hintBody").text("发送成功！");
			}
			else{
				$("#hintBody").text(data.msg);
			}
			$("#hintModal").modal("show");
			setTimeout(function(){
				$("#hintModal").modal("hide");
				location.reload();
			}, 2000);
		}
	);
}

function delete_msg() {
	var id = $("#deleteId").val();
	$.post(
		CONTEXT_PATH + "/letter/delete",
		{"id":id},
		function (data){
			data = $.parseJSON(data);
			if(data.code==0){
				// TODO 删除数据
				$(this).parents(".media").remove();
			}
			else{
				alert(data.msg);
			}
			// location.reload();
			// $(this).parents(".media").remove();
		}
	);
}

function close_msg(){
	// TODO 删除数据
	$(this).parents(".media").remove();
}