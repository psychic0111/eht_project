/*
 * 鲁军(lujun) 
 * http://www.cnblogs.com/idche/
 * email: idche@qq.com
 * GTalk: jelle.lu
 * 2010-10-30
 */


(function(){

// 上面是数据
var config = {
		boxID:"autoTalkBox",
		valuepWrap:'autoTalkText',
		wrap:'recipientsTips',
		listWrap:"autoTipsUserList",
		position:'autoUserTipsPosition',
		positionHTML:'<span id="autoUserTipsPosition">&nbsp;123</span>',
		className:'autoSelected'
	};
var html = '<div id="autoTalkBox"style="z-index:-2000;top:$top$px;left:$left$px;width:$width$px;height:$height$px;z-index:1;position:absolute;scroll-top:$SCTOP$px;overflow:hidden;overflow-y:auto;visibility:hidden;word-break:break-all;word-wrap:break-word;*letter-spacing:0.6px;"><span id="autoTalkText"></span></div><div id="recipientsTips" class="recipients-tips"><ul id="autoTipsUserList"></ul></div>';
var listHTML = '<li><a title="$ACCOUNT$" rel="$ID$" >$NAME$(@$SACCOUNT$)</a></li>';


/*
 * D 基本DOM操作
 * $(ID)
 * DC(tn) TagName
 * EA(a,b,c,e)
 * ER(a,b,c)
 * BS()
 * FF
 */
var D = {
	$:function(ID){
		return document.getElementById(ID)
	},
	DC:function(tn){
		return document.createElement(tn);
	},
    EA:function(a, b, c, e) {
        if (a.addEventListener) {
            if (b == "mousewheel") b = "DOMMouseScroll";
            a.addEventListener(b, c, e);
            return true
        } else return a.attachEvent ? a.attachEvent("on" + b, c) : false
    },
    ER:function(a, b, c) {
        if (a.removeEventListener) {
            a.removeEventListener(b, c, false);
            return true
        } else return a.detachEvent ? a.detachEvent("on" + b, c) : false
    },
	BS:function(){
		var db=document.body,
			dd=document.documentElement,
			top = db.scrollTop+dd.scrollTop;
			left = db.scrollLeft+dd.scrollLeft;
		return { 'top':top , 'left':left };
	},
	
	FF:(function(){
		var ua=navigator.userAgent.toLowerCase();
		return /firefox\/([\d\.]+)/.test(ua);
	})()
};

/*
 * TT textarea 操作函数
 * info(t) 基本信息
 * getCursorPosition(t) 光标位置
 * setCursorPosition(t, p) 设置光标位置
 * add(t,txt) 添加内容到光标处
 */
var TT = {
	
	info:function(t){
		var o = t.getBoundingClientRect();
		var w = t.offsetWidth;
		var h = t.offsetHeight;
		return {top:o.top, left:o.left, width:w, height:h};
	},
	
	getCursorPosition: function(t){
		if (document.selection) {
			t.focus();
			var ds = document.selection;
			var range = null;
			range = ds.createRange();
				if (t.nodeName === 'TEXTAREA') {// input
                var stored_range = range.duplicate();
			stored_range.moveToElementText(t);
			stored_range.setEndPoint("EndToEnd", range);
			t.selectionStart = stored_range.text.length - range.text.length;
			t.selectionEnd = t.selectionStart + range.text.length;
			return t.selectionStart;
            }else{
			range.moveStart('character', -t.value.length);
                return range.text.length;
			
			}
		} else return t.selectionStart
	},
	
	setCursorPosition:function(t, p){
		var n = p == 'end' ? t.value.length : p;
		if(document.selection){
			var range = t.createTextRange();
			range.moveEnd('character', -t.value.length);         
			range.moveEnd('character', n);
			range.moveStart('character', n);
			range.select();
		}else{
			t.setSelectionRange(n,n);
			t.focus();
		}
	},
	
	add:function (t, txt){
		var val = t.value;
		var wrap = wrap || '' ;
		
			var cp = parseInt(t.getAttribute('yuhao'));
			var ubbLength = t.value.length;
			t.value = t.value.slice(0,cp) + txt + t.value.slice(cp, ubbLength);
			t.setAttribute('yuhao',cp + txt.length);
			this.setCursorPosition(t, cp + txt.length); 
		
	},
	
	del:function(t, n){
		var p = parseInt(t.getAttribute('yuhao'));
		var s = t.scrollTop;
		t.value = t.value.slice(0,p - n) + t.value.slice(p);
		this.setCursorPosition(t ,p - n);
		t.setAttribute('yuhao',p - n);
		D.FF && setTimeout(function(){t.scrollTop = s},10);
		
	}

}


/*
 * DS 数据查找
 * inquiry(data, str, num) 数据, 关键词, 个数
 * Download by http://www.jb51.net
 */

var DS = {
	inquiry:function(data, str, num){
		if(str == '') return data.slice(0, num);

		var reg = new RegExp(str, 'i');
		var i = 0;
		//var dataUserName = {};
		var sd = [];

		while(sd.length < num && i < data.length){
			if(reg.test(data[i]['user'])){
				sd.push(data[i]);
				//dataUserName[data[i]['user']] = true;
			}
			i++;
		}			
		return sd;
	}
}


/*
 * selectList
 * _this
 * index
 * list
 * selectIndex(code) code : e.keyCode
 * setSelected(ind) ind:Number
 */


var selectList = {
	_this:null,
	index:-1,
	list:null,
	selectIndex:function(code){
		if(D.$(config.wrap).style.display == 'none') return true;
		var i = selectList.index;
		switch(code){
		   case 40:
			 i = i + 1;
			 break
		   case 38:
			 i = i - 1;
			 break
		   case 13:
			return selectList._this.enter();
			break
		}

		i = i >= selectList.list.length ? 0 : i < 0 ? selectList.list.length-1 : i;
		return selectList.setSelected(i);
	},
	setSelected:function(ind){
		if(selectList.index >= 0) selectList.list[selectList.index].className = '';
		selectList.list[ind].className = config.className;
		selectList.index = ind;
		return false;
	}

}



/*
 *
 */
var AutoTips = function(A,date){

	var friendsData = [
{user:"FLY100",name:"\u9646\u7ef4\u6881"},
{user:"G_9o_Karr",name:"\u4e01\u6208"},
{user:"LV5203344",name:"\u8463\u52c7"},
{user:"VIVI520007",name:"\u8587\u8587"},
{user:"WDQ826343036",name:"\u5434\u4e1c\u5f3a"},
{user:"ZZ-20100912",name:"\u5218\u73cd"},
{user:"aa394378840",name:"\u9c81\u5229"},
{user:"airiafans",name:"\u7231RIA\u96c6\u7ed3\u53f7"},
{user:"alilya",name:"\u9875\u9762\u59b9_\u9648\u601d\u5e06"},
{user:"amity1985",name:"\u7231\u5fb7\u57fa\u91d1\u4f1a"},
{user:"aoi_sola",name:"\u82cd\u4e95\u8001\u5e08"},
{user:"austinjin",name:"AustinGeek"},
{user:"bang",name:"bang"},
{user:"beebuzz",name:"beebuzz"},
{user:"bobo_js",name:"\u8349\u4f9d\u5c71"},
{user:"by727938837",name:"\u5348\u591c\u60c5\u6bd2"},
{user:"cailiangyu",name:"\u5305\u5b50"},
{user:"carlchang",name:"\u5f20\u632f"},
{user:"cheng6290575",name:"\u6768\u6210\u6210"},
{user:"dailysite_",name:"\u6bcf\u65e5\u4e00\u7ad9"},
{user:"dongwon_198295017",name:"vincy"},
{user:"fcname",name:"\u51af\u8d85"},
{user:"feiwen8772",name:"\u611a\u4eba\u7801\u5934"},
{user:"gouweifeng",name:"\u82df\u4f1f\u5cf0"},
{user:"gxgddu",name:"\u9633\u5149\u666e\u7167"},
{user:"hanjuanj",name:"\u97d3\u9e97\u9d51"},
{user:"hejiehedabao",name:"\u4f55\u6d01"},
{user:"hellfig",name:"\u60c5\u4f55\u4ee5\u582a"},
{user:"iceboylc",name:"\u4e09\u6bdb"},
{user:"jane8817",name:"\u5218\u598d"},
{user:"jingmeilitt",name:"\u552f\u7ffe"},
{user:"juntang",name:"\u5510\u9a8f"},
{user:"kelvin1129",name:"\u5468\u4e07\u5cf0"},
{user:"lastwinnersky",name:"\u674e\u6653\u5cf0"},
{user:"leadfast",name:"\u6211\u7ed9\u4f60\u4f20\u7b54\u6848"},
{user:"lengxiaohua",name:"\u51b7\u7b11\u8bdd\u7cbe\u9009"},
{user:"li_wei",name:"\u674e"},
{user:"light_force",name:"\u9c81\u4ee3\u4e7e"},
{user:"lulululu",name:"\u9ec4\u5c0f\u9e7f"},
{user:"mg345732481",name:"\u5218\u68a6\u9f99"}
];
friendsData =date;
	var elem = A.id ? D.$(A.id) : A.elem;
	var checkLength = 5;
	var _this = {};
	var key = '';

	_this.start = function(){
		if(!D.$(config.boxID)){
			var h = html.slice();
			var info = TT.info(elem);
			var div = D.DC('DIV');
			var bs = D.BS();
			h = h.replace('$top$',(info.top + bs.top)).
					replace('$left$',(info.left + bs.left)).
					replace('$width$',info.width).
					replace('$height$',info.height).
					replace('$SCTOP$','0');
			div.innerHTML = h;
			document.body.appendChild(div);
		}else{
			_this.updatePosstion();
		}
	}
	
  	_this.keyupFn = function(e){
		var e = e || window.event;
		var code = e.keyCode;
		if(code == 38 || code == 40 || code == 13) {
			if(code==13 && D.$(config.wrap).style.display != 'none'){
				_this.enter();
			}
			return false;
		}
		var cp = TT.getCursorPosition(elem);
		elem.setAttribute('yuhao',cp);
		if(!cp) return _this.hide();
		var valuep = elem.value.slice(0, cp);
		var val = valuep.slice(-checkLength);
		var chars = val.match(/(\w+)?@(\w+)$|@$/);
		if(chars == null) return _this.hide();
		var char = chars[2] ? chars[2] : '';
		D.$(config.valuepWrap).innerHTML = valuep.slice(0,valuep.length - char.length).replace(/\n/g,'<br/>').
											replace(/\s/g,'&nbsp;') + config.positionHTML;
		_this.showList(char);
	}
	
	_this.showList = function(char){
		key = char;
		var data = DS.inquiry(friendsData, char, 5);
		var html = listHTML.slice();
		var h = '';
		var len = data.length;
		if(len == 0){_this.hide();return;}
		var reg = new RegExp(char);
		var em = '<em>'+ char +'</em>';
		for(var i=0; i<len; i++){
			var hm = data[i]['user'].replace(reg,em);
			h += html.replace(/\$ACCOUNT\$|\$NAME\$/g,data[i]['name']).
						replace('$SACCOUNT$',hm).replace('$ID$',data[i]['user']);
		}
		
		_this.updatePosstion();
		var p = D.$(config.position).getBoundingClientRect();
		var bs = D.BS();
		var d = D.$(config.wrap).style;
		d.left = p.left - 5 + 'px';
		if (elem.nodeName === 'TEXTAREA'){
		d.top = p.top + 20 + bs.top + 'px';
		
		}else{
		d.top =TT.info(elem).top+ 20 + 'px';
		
		}
		
		
		D.$(config.listWrap).innerHTML = h;
		_this.show();
		
	}
	
	
	_this.KeyDown = function(e){
		var e = e || window.event;
		var code = e.keyCode;
		if(code == 38 || code == 40 || code == 13){
			return selectList.selectIndex(code);
		}
		return true;
	}
	
	_this.updatePosstion = function(){
		var p = TT.info(elem);
		var bs = D.BS();
		var d = D.$(config.boxID).style;
		d.top = p.top + bs.top +'px';
		d.left = p.left + bs.left + 'px';
		d.width = p.width+'px';
		d.height = p.height+'px';
		D.$(config.boxID).scrollTop = elem.scrollTop;
	}
	
	_this.show = function(){
		selectList.list = D.$(config.listWrap).getElementsByTagName('li');
		selectList.index = -1;
		selectList._this = _this;
		_this.cursorSelect(selectList.list);
		elem.onkeydown = _this.KeyDown;
		D.$(config.wrap).style.display = 'block';	
	}
	
	_this.cursorSelect = function(list){
		for(var i=0; i<list.length; i++){
			list[i].onmouseover = (function(i){
				return function(){selectList.setSelected(i)};
			})(i);
			list[i].onclick = _this.enter;
		}
	}
	
	_this.hide = function(){
		selectList.list = null;
		selectList.index = -1;
		selectList._this = null;
		D.ER(elem, 'keydown', _this.KeyDown);
		D.$(config.wrap).style.display = 'none';
	}
	
	_this.bind = function(){
		
		elem.onkeyup = _this.keyupFn;
		elem.onclick = _this.keyupFn;
		elem.onblur = function(){setTimeout(_this.hide, 100)}
		//elem.onkeyup= fn;
		//D.EA(elem, 'keyup', _this.keyupFn, false)
		//D.EA(elem, 'keyup', fn, false)
		//D.EA(elem, 'click', _this.keyupFn, false);
		//D.EA(elem, 'blur', function(){setTimeout(_this.hide, 100)}, false);
	}
	
	_this.enter = function(){
		TT.del(elem, key.length, key);
		TT.add(elem, selectList.list[selectList.index].getElementsByTagName('A')[0].rel+' ');
		_this.hide();
		return false;
	}
	
	return _this;
	
}

window.userAutoTips = function(args,date){
		var a = AutoTips(args,date);
			a.start();
			a.bind();
	}
		
})()

