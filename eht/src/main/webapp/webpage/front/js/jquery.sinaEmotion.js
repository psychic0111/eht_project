/**
 * @author 夏の寒风
 * @time 2012-12-14
 */

//自定义hashtable
function Hashtable() {
    this._hash = new Object();
    this.put = function(key, value) {
        if (typeof (key) != "undefined") {
            if (this.containsKey(key) == false) {
                this._hash[key] = typeof (value) == "undefined" ? null : value;
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    this.remove = function(key) { delete this._hash[key]; }
    this.size = function() { var i = 0; for (var k in this._hash) { i++; } return i; }
    this.get = function(key) { return this._hash[key]; }
    this.containsKey = function(key) { return typeof (this._hash[key]) != "undefined"; }
    this.clear = function() { for (var k in this._hash) { delete this._hash[k]; } }
}

var emotions = new Array();
var categorys = new Array();// 分组
var uSinaEmotionsHt = new Hashtable();
var biaoqingcontex='';
// 初始化缓存，页面仅仅加载一次就可以了
$(function() {
			var data = [{"phrase":"[草泥马]","category":"","icon":"shenshou_thumb.gif"},{"phrase":"[神马]","category":"","icon":"horse2_thumb.gif"},{"phrase":"[浮云]","category":"","icon":"fuyun_thumb.gif"},{"phrase":"[给力]","category":"","icon":"geili_thumb.gif"},{"phrase":"[围观]","category":"","icon":"wg_thumb.gif"},{"phrase":"[威武]","category":"","icon":"vw_thumb.gif"},{"phrase":"[熊猫]","category":"","icon":"panda_thumb.gif"},{"phrase":"[兔子]","category":"","icon":"rabbit_thumb.gif"},{"phrase":"[奥特曼]","category":"","icon":"otm_thumb.gif"},{"phrase":"[囧]","category":"","icon":"j_thumb.gif"},{"phrase":"[互粉]","category":"","icon":"hufen_thumb.gif"},{"phrase":"[礼物]","category":"","icon":"liwu_thumb.gif"},{"phrase":"[呵呵]","category":"","icon":"smilea_thumb.gif"},{"phrase":"[嘻嘻]","category":"","icon":"tootha_thumb.gif"},{"phrase":"[哈哈]","category":"","icon":"laugh.gif"},{"phrase":"[可���]","category":"","icon":"tza_thumb.gif"},{"phrase":"[可怜]","category":"","icon":"kl_thumb.gif"},{"phrase":"[挖鼻屎]","category":"","icon":"kbsa_thumb.gif"},{"phrase":"[吃惊]","category":"","icon":"cj_thumb.gif"},{"phrase":"[害羞]","category":"","icon":"shamea_thumb.gif"},{"phrase":"[挤眼]","category":"","icon":"zy_thumb.gif"},{"phrase":"[闭嘴]","category":"","icon":"bz_thumb.gif"},{"phrase":"[鄙视]","category":"","icon":"bs2_thumb.gif"},{"phrase":"[爱你]","category":"","icon":"lovea_thumb.gif"},{"phrase":"[泪]","category":"","icon":"sada_thumb.gif"},{"phrase":"[偷笑]","category":"","icon":"heia_thumb.gif"},{"phrase":"[亲亲]","category":"","icon":"qq_thumb.gif"},{"phrase":"[生病]","category":"","icon":"sb_thumb.gif"},{"phrase":"[太开心]","category":"","icon":"mb_thumb.gif"},{"phrase":"[懒得理你]","category":"","icon":"ldln_thumb.gif"},{"phrase":"[右哼哼]","category":"","icon":"yhh_thumb.gif"},{"phrase":"[左哼哼]","category":"","icon":"zhh_thumb.gif"},{"phrase":"[嘘]","category":"","icon":"x_thumb.gif"},{"phrase":"[衰]","category":"","icon":"cry.gif"},{"phrase":"[委屈]","category":"","icon":"wq_thumb.gif"},{"phrase":"[吐]","category":"","icon":"t_thumb.gif"},{"phrase":"[打哈欠]","category":"","icon":"k_thumb.gif"},{"phrase":"[抱抱]","category":"","icon":"bba_thumb.gif"},{"phrase":"[怒]","category":"","icon":"angrya_thumb.gif"},{"phrase":"[疑问]","category":"","icon":"yw_thumb.gif"},{"phrase":"[馋嘴]","category":"","icon":"cza_thumb.gif"},{"phrase":"[拜拜]","category":"","icon":"88_thumb.gif"},{"phrase":"[思考]","category":"","icon":"sk_thumb.gif"},{"phrase":"[汗]","category":"","icon":"sweata_thumb.gif"},{"phrase":"[困]","category":"","icon":"sleepya_thumb.gif"},{"phrase":"[睡觉]","category":"","icon":"sleepa_thumb.gif"},{"phrase":"[钱]","category":"","icon":"money_thumb.gif"},{"phrase":"[失望]","category":"","icon":"sw_thumb.gif"},{"phrase":"[酷]","category":"","icon":"cool_thumb.gif"},{"phrase":"[花心]","category":"","icon":"hsa_thumb.gif"},{"phrase":"[哼]","category":"","icon":"hatea_thumb.gif"},{"phrase":"[鼓掌]","category":"","icon":"gza_thumb.gif"},{"phrase":"[晕]","category":"","icon":"dizzya_thumb.gif"},{"phrase":"[悲伤]","category":"","icon":"bs_thumb.gif"},{"phrase":"[抓狂]","category":"","icon":"crazya_thumb.gif"},{"phrase":"[黑线]","category":"","icon":"h_thumb.gif"},{"phrase":"[阴险]","category":"","icon":"yx_thumb.gif"},{"phrase":"[怒骂]","category":"","icon":"nm_thumb.gif"},{"phrase":"[心]","category":"","icon":"hearta_thumb.gif"},{"phrase":"[伤心]","category":"","icon":"unheart.gif"},{"phrase":"[猪头]","category":"","icon":"pig.gif"},{"phrase":"[ok]","category":"","icon":"ok_thumb.gif"},{"phrase":"[耶]","category":"","icon":"ye_thumb.gif"},{"phrase":"[good]","category":"","icon":"good_thumb.gif"},{"phrase":"[不要]","category":"","icon":"no_thumb.gif"},{"phrase":"[赞]","category":"","icon":"z2_thumb.gif"},{"phrase":"[来]","category":"","icon":"come_thumb.gif"},{"phrase":"[弱]","category":"","icon":"sad_thumb.gif"},{"phrase":"[蜡烛]","category":"","icon":"lazu_thumb.gif"},{"phrase":"[钟]","category":"","icon":"clock_thumb.gif"},{"phrase":"[话筒]","category":"","icon":"m_thumb.gif"},{"phrase":"[蛋糕]","category":"","icon":"cake.gif"},{"phrase":"[足球]","category":"","icon":"football.gif"},{"phrase":"[加油啊]","category":"","icon":"lxhjiayou_thumb.gif"},{"phrase":"[西瓜]","category":"","icon":"watermelon.gif"},{"phrase":"[风扇]","category":"","icon":"fan.gif"},{"phrase":"[肥皂]","category":"","icon":"soap_thumb.gif"},{"phrase":"[马到成功]","category":"","icon":"madaochenggong_thumb.gif"},{"phrase":"[泪流满面]","category":"","icon":"lxhtongku_thumb.gif"},{"phrase":"[江南style]","category":"","icon":"gangnamstyle_thumb.gif"},{"phrase":"[偷乐]","category":"","icon":"lxhtouxiao_thumb.gif"},{"phrase":"[得意地笑]","category":"","icon":"lxhdeyidixiao_thumb.gif"},{"phrase":"[炸鸡和啤酒]","category":"","icon":"zhaji_thumb.gif"},{"phrase":"[xkl转圈]","category":"","icon":"xklzhuanquan_thumb.gif"},{"phrase":"[lt切克闹]","category":"","icon":"ltqiekenao_thumb.gif"},{"phrase":"[din推撞]","category":"","icon":"dintuizhuang_thumb.gif"},{"phrase":"[老妈我��你]","category":"","icon":"mothersday_thumb.gif"},{"phrase":"[母亲节]","category":"","icon":"carnation_thumb.gif"},{"phrase":"[有钱]","category":"","icon":"youqian_thumb.gif"},{"phrase":"[随手拍]","category":"","icon":"suishoupai2014_thumb.gif"},{"phrase":"[拍照]","category":"","icon":"lxhpaizhao_thumb.gif"},{"phrase":"[地球一小时]","category":"","icon":"earth1r_thumb.gif"},{"phrase":"[国旗]","category":"","icon":"flag_thumb.gif"},{"phrase":"[许愿]","category":"","icon":"lxhxuyuan_thumb.gif"},{"phrase":"[雪]","category":"","icon":"snow_thumb.gif"},{"phrase":"[马上有对象]","category":"","icon":"mashangyouduixiang_thumb.gif"},{"phrase":"[青啤鸿运当头]","category":"","icon":"hongyun_thumb.gif"},{"phrase":"[让红包飞]","category":"","icon":"hongbaofei2014_thumb.gif"},{"phrase":"[ali做鬼脸]","category":"","icon":"alizuoguiliannew_thumb.gif"},{"phrase":"[ali哇]","category":"","icon":"aliwanew_thumb.gif"},{"phrase":"[酷库熊顽皮]","category":"","icon":"kxwanpi_thumb.gif"},{"phrase":"[bm可爱]","category":"","icon":"bmkeai_thumb.gif"},{"phrase":"[BOBO爱你]","category":"","icon":"boaini_thumb.gif"},{"phrase":"[转发]","category":"","icon":"lxhzhuanfa_thumb.gif"},{"phrase":"[ppb鼓掌]","category":"","icon":"ppbguzhang_thumb.gif"},{"phrase":"[xb压力]","category":"","icon":"xbyali_thumb.gif"},{"phrase":"[moc转发]","category":"","icon":"moczhuanfa_thumb.gif"},{"phrase":"[笑哈哈]","category":"","icon":"lxhwahaha_thumb.gif"}];
			for ( var i in data) {
				if (data[i].category == '') {
					data[i].category = '默认';
				}
				if (emotions[data[i].category] == undefined) {
					emotions[data[i].category] = new Array();
					categorys.push(data[i].category);
				}
				emotions[data[i].category].push( {
					name : data[i].phrase,
					icon : data[i].icon
				});
				uSinaEmotionsHt.put(data[i].phrase, data[i].icon);
			}
	
});

//替换
function AnalyticEmotion(s,context) {
	if(typeof (s) != "undefined") {
		var sArr = s.match(/\[.*?\]/g);
		if(sArr==null){
			return s;
		}
		for(var i = 0; i < sArr.length; i++){
			if(uSinaEmotionsHt.containsKey(sArr[i])) {
				var reStr = "<img src=\"" +context+ uSinaEmotionsHt.get(sArr[i]) + "\" height=\"22\" width=\"22\" />";
				s = s.replace(sArr[i], reStr);
			}
		}
	}
	return s;
}

(function($){
	$.fn.SinaEmotion = function(target,context){
		var context=context;
		var cat_current;
		var cat_page;
		$(this).click(function(event){
			event.stopPropagation();
			
			var eTop = target.offset().top + target.height() + 15;
			var eLeft = target.offset().left - 1;
			
			if($('#emotions .categorys')[0]){
				$('#emotions').css({top: eTop, left: eLeft});
				$('#emotions').toggle();
				return;
			}
			$('body').append('<div id="emotions"></div>');
			$('#emotions').css({top: eTop, left: eLeft});
			$('#emotions').html('<div>正在加载，请稍候...</div>');
			$('#emotions').click(function(event){
				event.stopPropagation();
			});
			
			$('#emotions').html('<div style="float:right"><a href="javascript:void(0);" id="prev">&laquo;</a><a href="javascript:void(0);" id="next">&raquo;</a></div><div class="categorys"></div><div class="container"></div><div class="page"></div>');
			$('#emotions #prev').click(function(){
				showCategorys(cat_page - 1);
			});
			$('#emotions #next').click(function(){
				showCategorys(cat_page + 1);
			});
			showCategorys();
			showEmotions();
			
		});
		$('body').click(function(){
			$('#emotions').remove();
		});
		$.fn.insertText = function(text){
			this.each(function() {
				if(this.tagName !== 'INPUT' && this.tagName !== 'TEXTAREA') {return;}
				if (document.selection) {
					this.focus();
					var cr = document.selection.createRange();
					cr.text = text;
					cr.collapse();
					cr.select();
				}else if (this.selectionStart || this.selectionStart == '0') {
					var 
					start = this.selectionStart,
					end = this.selectionEnd;
					this.value = this.value.substring(0, start)+ text+ this.value.substring(end, this.value.length);
					this.selectionStart = this.selectionEnd = start+text.length;
				}else {
					this.value += text;
				}
			});        
			return this;
		}
		function showCategorys(){
			var page = arguments[0]?arguments[0]:0;
			if(page < 0 || page >= categorys.length / 5){
				return;
			}
			$('#emotions .categorys').html('');
			cat_page = page;
			for(var i = page * 5; i < (page + 1) * 5 && i < categorys.length; ++i){
				$('#emotions .categorys').append($('<a href="javascript:void(0);">' + categorys[i] + '</a>'));
			}
			$('#emotions .categorys a').click(function(){
				showEmotions($(this).text());
			});
			$('#emotions .categorys a').each(function(){
				if($(this).text() == cat_current){
					$(this).addClass('current');
				}
			});
		}
		function showEmotions(){
			var category = arguments[0]?arguments[0]:'默认';
			var page = arguments[1]?arguments[1] - 1:0;
			$('#emotions .container').html('');
			$('#emotions .page').html('');
			cat_current = category;
			for(var i = page * 72; i < (page + 1) * 72 && i < emotions[category].length; ++i){
				$('#emotions .container').append($('<a href="javascript:void(0);" alt="' + emotions[category][i].name + '" title="' + emotions[category][i].name + '"><img src="' +context+ emotions[category][i].icon + '" alt="' + emotions[category][i].name + '" width="22" height="22" /></a>'));
			}
			$('#emotions .container a').click(function(){
				target.insertText($(this).attr('alt'));
				$('#emotions').remove();
			});
			for(var i = 1; i < emotions[category].length / 72 + 1; ++i){
				$('#emotions .page').append($('<a href="javascript:void(0);"' + (i == page + 1?' class="current"':'') + '>' + i + '</a>'));
			}
			$('#emotions .page a').click(function(){
				showEmotions(category, $(this).text());
			});
			$('#emotions .categorys a.current').removeClass('current');
			$('#emotions .categorys a').each(function(){
				if($(this).text() == category){
					$(this).addClass('current');
				}
			});
		}
	}
})(jQuery);
