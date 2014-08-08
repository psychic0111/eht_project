function showuploadwindow(){
	
var html = $("#attTemp").html();
var submit = function (v, h, f) {
    if (f.yourname == '') {
        $.jBox.tip("请输入您的姓名。", 'error', { focusId: "yourname" }); // 关闭设置 yourname 为焦点
        return false;
    }
    $.jBox.tip("你叫：" + f.yourname);
    //$.jBox.tip("你叫：" + h.find("#yourname").val());
    //$.jBox.tip("你叫：" + h.find(":input[name='yourname']").val());

    return true;
};
$.jBox(html, { title: "你叫什么名字？", submit: submit });
}