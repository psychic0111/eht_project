<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@include file="/context/mytags.jsp"%>

<script type="text/javascript" src="plug-in/Highcharts-2.2.5/js/highcharts.src.js"></script>
<script type="text/javascript" src="plug-in/Highcharts-2.2.5/js/modules/exporting.src.js"></script>
<c:set var="ctxPath" value="${pageContext.request.contextPath}" />

<script type="text/javascript">
function getcount(){
	var   start=document.getElementsByName("start")[0].value;
	var end=document.getElementsByName("end")[0].value;
	if(start==''||end==''){
          alert("请填写起始时间和截止时间");
          return;
		}
	if(start>end){
		alert("起始时间必须小于截止时间");
        return;
		}
	var chart;
	$.ajax({
		type : "POST",
		url : "${ctxPath}/countController.do?report&reportType=column",
		data:{start:start,end:end},
		success : function(jsondata) {
			
			data = eval(jsondata);
			console.log(data);
			chart = new Highcharts.Chart({
				chart : {
					renderTo : 'containerCol',
					plotBackgroundColor : null,
					plotBorderWidth : null,
					plotShadow : false
				},
				title : {
					text : '班级学生人数比例分析-<b>柱状图</b>'
				},
				xAxis : {
					categories : [ '专题数量', '目录数量', '条目数量', '专题数量']
				},
				tooltip : {
					 percentageDecimals : 1,
					 formatter: function() {
						return  '<b>'+this.point.name + '</b>:' +  Highcharts.numberFormat(this.percentage, 1) +'%';
						}

				},
				exporting:{ 
	                filename:'column',  
	                url:'${ctxPath}/countController.do?export'//
	            },
				plotOptions : {
					column : {
						allowPointSelect : true,
						cursor : 'pointer',
						showInLegend : true,
						dataLabels : {
							enabled : true,
							color : '#000000',
							connectorColor : '#000000',
							formatter : function() {
								return '<b>' + this.point.name + '</b>: ' +Highcharts.numberFormat(this.percentage, 1)+"%";
							}
						}
					}
				},
				series : data
			});
		}
	});
	
}

</script>

<div fit="true" class="layout"
	style="width: 1100px; height: 70px;">
	<div class="panel layout-panel layout-panel-center"
		style="left: 0px; top: 0px; width: 1100px;">
		<div style="padding: 1px; width: 1096px; height: 60px;"
			region="center"
			class="panel-noscroll panel-body panel-body-noheader layout-body"
			title="">
			<div class="panel datagrid" style="width: 1096px;">
				<div class="panel-header" style="width: 1084px;">
					<div class="panel-title">
						内容统计
					</div>
					<div class="panel-tool"></div>
				</div>
				<div class="datagrid-wrap panel-body" title=""
					style="width: 1094px; height: 359px;">
					<div style="padding: 3px; height: auto;" id="jeecgDemoListtb"
						class="datagrid-toolbar">
						<div class="datagrid-toolbar" style="height: 30px;">
						    <span style="display: -moz-inline-box; display: inline-block;">
						    <span   style="display: -moz-inline-box; display: inline-block; width: 80px; text-align: right; text-overflow: ellipsis; -o-text-overflow: ellipsis; overflow: hidden; white-space: nowrap;">时间：</span>
							<input id="start" type="text" style="width: 100px; display: none;" class="easyui-datebox datebox-f combo-f" comboname="start">
								<span style="display: -moz-inline-box; display: inline-block; width: 8px; text-align: right;">~</span>
							<input type="text"  style="width: 100px; display: none;" class="easyui-datebox datebox-f combo-f" comboname="end">
							</span>
							<span style="float: right"><a onclick="getcount();" iconcls="icon-search" class="easyui-linkbutton l-btn" href="#" id="">
								查询
							</a><a onclick="" iconcls="icon-reload" class="easyui-linkbutton l-btn" href="#" id="">
								重置
							</a>
							</span>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>
<span id="containerCol" style="float: left; width: 100%; height: 90%"></span>

