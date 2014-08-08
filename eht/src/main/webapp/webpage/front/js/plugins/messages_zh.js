/*
 * Translated default messages for the jQuery validation plugin.
 * Locale: ZH (Chinese, 中文 (Zhōngwén), 汉语, 漢語)
 */
(function ($) {
	$.extend($.validator.messages, {
		required: "必填字段",
		remote: "该字段值已存在",
		email: "邮件格式不正确",
		url: "网址格式不正确",
		date: "日期格式不正确",
		dateISO: "请输入合法的日期 (ISO).",
		number: "请输入的数字",
		digits: "只能输入整数",
		creditcard: "请输入合法的信用卡号",
		equalTo: "请再次输入相同的值",
		accept: "请输入拥有合法后缀名的字符串",
		maxlength: $.validator.format("最多{0}个字符"),
		minlength: $.validator.format("至少 {0}个字符"),
		rangelength: $.validator.format("长度介于 {0} 和 {1} 之间"),
		range: $.validator.format("值范围在{0}至{1}"),
		max: $.validator.format("最大值为{0}"),
		ip: "请输入正确的IP地址",
		min: $.validator.format("最小值为{0}"),
		regexp: "包含非法字符"
	});
}(jQuery));